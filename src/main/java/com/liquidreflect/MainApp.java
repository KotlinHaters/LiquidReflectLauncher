package com.liquidreflect;

import com.liquidreflect.mapping.Mapping;
import com.liquidreflect.mapping.NodeProvider;
import com.liquidreflect.util.Logger;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.objectweb.asm.tree.ClassNode;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MainApp extends Application {

    public static ProgressBar progressBar1;
    public static ProgressBar progressBar2;
    public static Map<String,ClassNode> classpath = new ConcurrentHashMap<>();
    public static NodeProvider nodeProvider = name -> classpath.get(name);
    public static Mapping mapping = new Mapping(""); // Empty Mapping

    @Override
    public void start(Stage primaryStage) {
        if(Injector.getInstances().length == 0){
            JOptionPane.showMessageDialog(null,"Open a minecraft instance for continue inject.","No minecraft found",JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        AnchorPane root = new AnchorPane();
        Scene scene = new Scene(root, 600, 332);

        ImageView imageView = new ImageView(new Image(MainApp.class.getResourceAsStream("/background.png")));
        imageView.setFitWidth(600);
        imageView.setFitHeight(344);
        imageView.setPickOnBounds(true);
        root.getChildren().add(imageView);

        progressBar1 = new ProgressBar();
        progressBar1.setLayoutX(200);
        progressBar1.setLayoutY(227);
        progressBar1.setPrefWidth(200);
        progressBar1.setProgress(0);

        progressBar2 = new ProgressBar();
        progressBar2.setLayoutX(200);
        progressBar2.setLayoutY(256);
        progressBar2.setPrefWidth(200);
        progressBar2.setProgress(0);
        progressBar2.setVisible(false);

        root.getChildren().addAll(progressBar1, progressBar2);

        primaryStage.setScene(scene);
        primaryStage.setTitle("");
        primaryStage.show();

        new Thread(() -> Injector.inject(primaryStage,new Logger())).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
