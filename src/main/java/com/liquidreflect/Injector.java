package com.liquidreflect;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.liquidreflect.jll.Jli;
import com.liquidreflect.mapping.Mapping;
import com.liquidreflect.mapping.MappingApplier;
import com.liquidreflect.protocol.http.handlers.Finish;
import com.liquidreflect.protocol.http.handlers.GetMethod;
import com.liquidreflect.util.HttpUtil;
import com.liquidreflect.util.LibraryWriter;
import com.liquidreflect.util.Logger;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import static com.sun.jna.platform.win32.WinUser.GW_CHILD;
import static com.sun.jna.platform.win32.WinUser.GW_HWNDNEXT;

public class Injector {
    public static HttpServer server;
    public static void inject(Stage stage, Logger logger){
        File client = null;

        if(System.getProperty("liquidreflect.client.path") == null){
            String owner = "KotlinHaters";
            String repo = "LiquidReflect";
            try {
                logger.info("Fetching client...");
                MainApp.progressBar2.setProgress(0.1);

                MainApp.progressBar2.setVisible(true);

                String info = HttpUtil.getFromURL(new URL("https://api.github.com/repos/" + owner + "/" + repo + "/actions/workflows"));
                MainApp.progressBar2.setProgress(0.4);

                JsonObject object = JsonParser.parseString(info).getAsJsonObject().get("workflows").getAsJsonArray().get(0).getAsJsonObject();

                String wf = HttpUtil.getFromURL(new URL("https://api.github.com/repos/"+owner+"/"+repo+"/actions/workflows/"+object.get("id").getAsString()+"/runs"));
                MainApp.progressBar2.setProgress(0.7);

                JsonObject object2 = JsonParser.parseString(wf).getAsJsonObject().get("workflow_runs").getAsJsonArray().get(0).getAsJsonObject();

                String jar = HttpUtil.getFromURL(new URL("https://api.github.com/repos/"+owner+"/"+repo+"/actions/runs/"+ object2.get("id").getAsString()+"/artifacts"));
                MainApp.progressBar2.setProgress(1);

                JsonObject object3 = JsonParser.parseString(jar).getAsJsonObject().get("artifacts").getAsJsonArray().get(0).getAsJsonObject();

                String fileName = object2.get("head_sha").getAsString();

                String downloadLink = object3.get("archive_download_url").getAsString();

                File path = new File(System.getenv("USERPROFILE") + File.separator + ".liquidreflect" + File.separator + "builds" + File.separator + fileName);
                File clientJar = new File(System.getenv("USERPROFILE") + File.separator + ".liquidreflect" + File.separator + "builds" + File.separator + fileName + ".jar");

                new File(System.getenv("USERPROFILE") + File.separator + ".liquidreflect" + File.separator + "builds").mkdir();

                if(!path.exists()){
                    logger.info("Nightly update found, Downloading...");
                    MainApp.progressBar2.setProgress(0);

                    try {
                        URL url = new URL(downloadLink);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestProperty("Authorization", "Bearer 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");
                        int fileSize = connection.getContentLength();
                        InputStream in = connection.getInputStream();
                        FileOutputStream out = new FileOutputStream(path);

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        long totalBytesRead = 0;

                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;

                            double progress = (double) totalBytesRead / fileSize;
                            Platform.runLater(() -> MainApp.progressBar2.setProgress(progress));
                        }

                        in.close();
                        out.close();
                        logger.info("Download completed.");

                        MainApp.progressBar2.setVisible(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.warn("Download failed: " + e.getMessage());
                    }
                }

                MainApp.progressBar1.setProgress(0.1);

                JarFile zipFile = new JarFile(path);

                byte[] sb = readInputStream(zipFile.getInputStream(new JarEntry("LiquidReflect-Build-jar-with-dependencies.jar")));

                FileOutputStream fos = new FileOutputStream(clientJar);
                fos.write(sb);
                fos.close();

                client = clientJar;
            }catch (Exception e){
                stage.close();
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,e.getMessage(),"Failed to Load",JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

        }else {
            client = new File(System.getProperty("liquidreflect.client.path"));

            logger.info("Use custom client path.");

            if(!client.exists()){
                stage.close();
                JOptionPane.showMessageDialog(null,"Client " + client.getAbsolutePath() + " is not exist.","Failed to Load",JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }

        MainApp.progressBar1.setProgress(0.2);

        if(client.exists()){
            ClientInstance instance = getInstances()[0];

            if(instance.type == ClientType.FORGE){
                MainApp.mapping = new Mapping("mcp-srg.srg");
                MappingApplier applier = new MappingApplier(MainApp.mapping);

                try {
                    logger.info("Mapping Client...");

                    byte[] sb = readInputStream(MainApp.class.getResourceAsStream("/dummy.jar"));

                    FileOutputStream fos = new FileOutputStream("dummy.jar");
                    fos.write(sb);
                    fos.close();

                    Injector.addSystemClasspath();
                    Injector.addToClasspath(client);
                    Injector.addToClasspath(new File("dummy.jar"));
                    Injector.applyMapping(applier,client,  new File(System.getenv("USERPROFILE") + File.separator + ".liquidreflect" + File.separator + "release.jar"));
                }catch (Exception e){
                    e.printStackTrace();
                }
            } else if (instance.type == ClientType.MCP) {
                byte[] sb;
                try {
                    sb = readInputStream(Files.newInputStream(client.toPath()));

                    FileOutputStream fos = new FileOutputStream(System.getenv("USERPROFILE") + File.separator + ".liquidreflect" + File.separator + "release.jar");
                    fos.write(sb);
                    fos.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            MainApp.progressBar2.setVisible(false);
            try {
                server = initServer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            byte[] sb = readInputStream(MainApp.class.getResourceAsStream("/LiquidReflect.dll"));

            try {
                FileOutputStream fos = new FileOutputStream("LiquidReflect.dll");
                fos.write(sb);
                fos.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            MainApp.progressBar1.setProgress(0.5);
            boolean injected = Jli.inject(instance.pid,new File("LiquidReflect.dll").getAbsolutePath());
            MainApp.progressBar1.setProgress(0.8);
            System.out.println(injected);
        }
    }

    private static HttpServer initServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(6666), 0);
        server.createContext("/api/getMethod", new GetMethod());
        server.createContext("/api/close", new Finish());
        server.start();
        return server;
    }

    public static void addSystemClasspath() throws Exception {
        final File javaHome = new File(System.getProperty("java.home"));

        if (Integer.parseInt(System.getProperty("java.version").split("\\.")[0]) <= 8) {
            final File libDir = new File(javaHome, "lib");
            for(File file : libDir.listFiles()){
                if(file.getName().endsWith(".jar"))
                    addToClasspath(file);
            }
        }
    }

    public static void addToClasspath(File file) throws Exception {
        JarFile inputJar = new JarFile(file);
        Enumeration<JarEntry> entries = inputJar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();

            InputStream inputStream = inputJar.getInputStream(entry);
            if (entry.getName().endsWith(".class")) { // 处理班级
                ClassNode classNode = new ClassNode();

                ClassReader classReader = new ClassReader(inputStream);
                classReader.accept(classNode, ClassReader.EXPAND_FRAMES | ClassReader.SKIP_DEBUG); // 跳过反虫子

                MainApp.classpath.put(classNode.name,classNode);
            }
        }
    }

    public static void applyMapping(MappingApplier applier, File inputFile, File outputFile) throws Exception{

        JarFile inputJar = new JarFile(inputFile);
        JarOutputStream outputJar = new JarOutputStream(Files.newOutputStream(outputFile.toPath()));

        Enumeration<JarEntry> entries = inputJar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();

            InputStream inputStream = inputJar.getInputStream(entry);
            if (entry.getName().endsWith(".class")) { // 处理班级
                ClassNode classNode = new ClassNode();

                ClassReader classReader = new ClassReader(inputStream);
                classReader.accept(classNode, ClassReader.EXPAND_FRAMES | ClassReader.SKIP_DEBUG); // 跳过反虫子

                applier.apply(classNode);

                ClassWriter classWriter = new LibraryWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(classWriter);

                byte[] transformedClassBytes = classWriter.toByteArray();
                JarEntry newEntry = new JarEntry(entry.getName());
                outputJar.putNextEntry(newEntry);
                outputJar.write(transformedClassBytes);
            } else { // 如果不是班级直接写出
                JarEntry newEntry = new JarEntry(entry.getName());
                outputJar.putNextEntry(newEntry);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputJar.write(buffer, 0, bytesRead);
                }
            }
        }
        inputJar.close();
        outputJar.close();
    }

    static final User32 user32 = User32.INSTANCE;
    static class ClientInstance {
        @Override
        public String toString() {
            return title;
        }

        public final int pid;
        public final String title;
        public ClientType type = ClientType.MCP;

        public ClientInstance(int pid, String title) {
            this.pid = pid;
            this.title = title;
            try {
                String cli = getCli(pid);
                if(cli.contains("minecraftforge"))
                    type = ClientType.FORGE;
                if(cli.contains("lunar-emote.jar"))
                    type = ClientType.MCP;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public enum ClientType{
        FORGE,
        MCP
    }

    public static String getCli(int pid) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("wmic", "process", "where", "ProcessId=" + pid, "get", "CommandLine");
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder commandLine = new StringBuilder();
        String line;
        reader.readLine();
        while ((line = reader.readLine()) != null) {
            commandLine.append(line.trim()).append("\n");
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("Error: Failed to get process command line.");
        }
        return commandLine.toString().trim();
    }
    public static ClientInstance[] getInstances() {
        ArrayList<ClientInstance> mc = new ArrayList<>();
        WinDef.HWND hwnd = user32.GetWindow(user32.GetDesktopWindow(), new WinDef.DWORD(GW_CHILD));
        IntByReference ptr = new IntByReference();
        do {
            hwnd = user32.GetWindow(hwnd, new WinDef.DWORD(GW_HWNDNEXT));
            char[] charArray = new char[1024];
            int length = user32.GetClassName(hwnd, charArray, charArray.length);
            if (length != charArray.length) {
                char[] arr = new char[length];
                System.arraycopy(charArray, 0, arr, 0, length);
                charArray = arr;
            }
            String className = new String(charArray);
            if (!className.equals("LWJGL"))
                continue;
            charArray = new char[user32.GetWindowTextLength(hwnd) + 1];
            user32.GetWindowText(hwnd, charArray, charArray.length);
            String title = new String(charArray);
            user32.GetWindowThreadProcessId(hwnd, ptr);
            mc.add(new ClientInstance(ptr.getValue(), title));
        } while (hwnd != null);
        return mc.toArray(new ClientInstance[0]);
    }

    public static byte[] readInputStream(InputStream inputStream){
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        try {
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return buffer.toByteArray();
    }

}
