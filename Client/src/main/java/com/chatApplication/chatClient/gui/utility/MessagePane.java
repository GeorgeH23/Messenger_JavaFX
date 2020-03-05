package com.chatApplication.chatClient.gui.utility;

import com.chatApplication.chatClient.gui.controllers.MessagePaneController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;

public class MessagePane {

    private FXMLLoader fxmlLoader;
    private Parent root;
    private MessagePaneController controller;
    private Stage stage;
    private String login;

    public MessagePane(String login) {
        this.fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/views/messagePaneView.fxml"));
        try {
            this.root = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.controller = fxmlLoader.getController();
        //root.getStylesheets().add(getClass().getResource("/utils/css/messagePaneStyling.css").toString());
        this.stage = new Stage();
        this.login = login;

        constructPane();
    }

    public void constructPane() {

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(true);
        stage.setTitle('"' + login + '"');
        stage.setScene(new Scene(root));
        stage.show();

        controller.inititializePane('"' + login + '"');

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            // Do whatever you want
            controller.resize();
        });
    }

    public MessagePaneController getController() {
        return controller;
    }

    public Stage getStage() {
        return stage;
    }
}

