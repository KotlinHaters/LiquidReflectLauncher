package com.liquidreflect.mapping;

import com.liquidreflect.MainApp;
import com.liquidreflect.util.LibraryWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Mapping {
    private final String name;
    private final Map<String, String> methods;
    private final Map<String, String> fields;
    private final Map<String, String> classes;

    public Mapping(String name) {
        this.name = name;
        methods = new HashMap<>();
        fields = new HashMap<>();
        classes = new HashMap<>();
        parse(this.name);
    }

    public String getName() {
        return name;
    }

    public String getMethod(String name) {
        return methods.getOrDefault(name, name);
    }

    public String getField(String name) {
        return fields.getOrDefault(name, name);
    }

    public String mapFieldName(String className, String fieldName){
        if(!className.startsWith("net/minecraft")) return fieldName;
        String finalName = fieldName;
        for(String s : LibraryWriter.getSuperClasses(className)){
            finalName = getFieldName(s + "/" + fieldName);
            if(!finalName.equals(fieldName))
                return finalName;
        }
        return finalName;
    }

    public String mapMethodName(String className, String methodName){
        if(!className.startsWith("net/minecraft") || className.startsWith("[L")) return methodName;
        String finalName = methodName;
        for(String s : LibraryWriter.getSuperClasses(className)){
            finalName = getMethodName(s + "/" + methodName);
            if(!finalName.equals(methodName))
                return finalName;
        }
        return finalName;
    }


    public String getMethodName(String name) {
        String fullName = methods.getOrDefault(name, name);
        String[] parts = fullName.split("/");
        return parts[parts.length - 1];
    }

    public String getFieldName(String name) {
        String fullName = fields.getOrDefault(name, name);
        String[] parts = fullName.split("/");
        return parts[parts.length - 1];
    }

    public String getClass(String name) {
        return classes.getOrDefault(name, name);
    }

    private void parse(String name) {
        if (name.isEmpty()) return;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(MainApp.class.getResourceAsStream("/mappings/" + name))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("MD: ")) {
                    String[] parts = line.substring(4).split(" ");
                    if (parts.length == 4) {
                        methods.put(parts[0], parts[2]);
                    }
                } else if (line.startsWith("FD: ")) {
                    String[] parts = line.substring(4).split(" ");
                    if (parts.length == 2) {
                        fields.put(parts[0], parts[1]);
                    }
                } else if (line.startsWith("CL: ")) {
                    String[] parts = line.substring(4).split(" ");
                    if (parts.length == 2) {
                        classes.put(parts[0], parts[1]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
