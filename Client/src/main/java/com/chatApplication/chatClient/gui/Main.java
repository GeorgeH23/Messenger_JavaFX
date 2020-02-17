package com.chatApplication.chatClient.gui;

import com.chatApplication.dataModel.DataSource;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        String path1 = new File(System.getProperty("user.home")).getAbsolutePath() + File.separator + "Documents" +
                File.separator + "MessengerApplication";
        File directory1 = new File(path1);
        if (!directory1.exists()) {
            directory1.mkdir();
        }

        String path = new File(System.getProperty("user.home")).getAbsolutePath() + File.separator + "Documents" +
                File.separator + "MessengerApplication" + File.separator + "Database";
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdir();
        }

        DataSource.getInstance().open();

        AudioHandler audio = AudioHandler.getInstance();
        audio.load("/utils/sounds/type.wav", "start");
        audio.play("start", 0);

        primaryStage.setTitle("Messenger");
        Scene root = new Scene((FXMLLoader.load(getClass().getResource("/fxmlFiles/loginWindow.fxml"))), 420, 350);
        // Add CSS stylesheet for the main window
        //root.getStylesheets().add(getClass().getResource("/utils/css/Login.css").toString());
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setScene(root);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
