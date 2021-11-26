package com.chatApplication.chatClient.gui.view;

import com.chatApplication.chatClient.gui.ChatManager;
import com.chatApplication.chatClient.gui.controller.BaseController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;

public class ViewInitializer {

    private ChatManager chatManager;
    private ViewFactory viewFactory;

    public ViewInitializer(ChatManager chatManager, ViewFactory viewFactory) {
        this.chatManager = chatManager;
        this.viewFactory = viewFactory;
    }

    public Stage initializeStage(String fxmlName, String stageTitle){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlName));

        Parent parent;
        try {
            parent = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        String controllerName = fxmlName.substring(7,(fxmlName.length()-5));
        BaseController controller = fxmlLoader.getController();
        if (controllerName.equals("messagePaneView")) {
            viewFactory.getControllers().put(stageTitle, controller);
        } else {
            viewFactory.getControllers().put(controllerName, controller);
        }
        Scene scene = new Scene(parent);
        applyCurrentStylesToScene(scene, stageTitle);
        Stage stage = new Stage();
        stage.setTitle(stageTitle);
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();
        stage.toFront();
        return stage;
    }

    public void applyCurrentStylesToScene(Scene scene, String sceneName) {
        scene.getStylesheets().clear();
        if (sceneName.equals("LoginWindow") || sceneName.equals("MainWindow") || sceneName.equals("CreateAccount") || sceneName.equals("ImageCroppingHandler")) {
            scene.getStylesheets().add(getClass().getResource(Theme.getCssPath(chatManager.getTheme()) + sceneName + "Styling.css").toExternalForm());
        } else {
            scene.getStylesheets().add(getClass().getResource(Theme.getCssPath(chatManager.getTheme()) + "MessagePaneStyling.css").toExternalForm());
        }
    }

}
