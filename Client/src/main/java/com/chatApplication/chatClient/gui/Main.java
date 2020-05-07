package com.chatApplication.chatClient.gui;

import com.chatApplication.chatClient.gui.model.handlers.AudioHandler;
import com.chatApplication.chatClient.gui.model.handlers.ImageHandler;
import com.chatApplication.chatClient.gui.view.Theme;
import com.chatApplication.chatClient.gui.view.ViewFactory;
import javafx.application.Application;
import javafx.stage.Stage;
import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {

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

        ImageHandler.getInstance().loadStatusImages();
        ViewFactory.getInstance().showLoginWindow();

        AudioHandler audio = AudioHandler.getInstance();
        audio.load("/utils/sounds/type.wav", "start");
        audio.play("start", 0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
