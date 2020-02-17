package com.chatApplication.gui;

import com.chatApplication.chatServer.Server;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static Controller controller;
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Server");

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/fxmlFiles/MainWindow.fxml"));
        Parent parent = fxmlLoader.load();
        controller = fxmlLoader.getController();

        Scene root = new Scene(parent);
        // Add CSS stylesheet for the main window
        primaryStage.setScene(root);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            System.exit(0);
        });

        int port = 8818;
        Server server = new Server(port);
        server.start();
    }

    public static Controller getController() {
        return controller;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
