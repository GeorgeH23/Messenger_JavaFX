package com.chatApplication.chatClient.gui.view;

import com.chatApplication.chatClient.gui.ChatManager;
import com.chatApplication.chatClient.gui.controller.BaseController;
import com.chatApplication.chatClient.gui.controller.MessagePaneController;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;

public class ViewFactory {

    private static ViewFactory viewFactory;
    private final ViewInitializer viewInitializer;
    private ChatManager chatManager;
    private final Map<String, Stage> activeStages;
    private final Map<String, Stage> messagePanes;
    private final Map<String, BaseController> controllers;

    private ViewFactory() {
        this.chatManager = ChatManager.getInstance();
        this.activeStages = new HashMap<>();
        this.controllers = new HashMap<>();
        this.messagePanes = new HashMap<>();
        this.viewInitializer = new ViewInitializer(chatManager, this);
    }

    public static ViewFactory getInstance() {
        if (viewFactory == null) {
            viewFactory = new ViewFactory();
        }
        return viewFactory;
    }

    public void showLoginWindow() {
        String fxmlName = "/views/loginWindowView.fxml";
        Stage stage = viewInitializer.initializeStage(fxmlName, "LoginWindow");
        activeStages.put("LoginWindow", stage);
    }

    public Stage showMainWindow() {
        String fxmlName = "/views/mainWindowView.fxml";
        Stage mainStage = viewInitializer.initializeStage(fxmlName, "MainWindow");
        activeStages.put("MainWindow", mainStage);
        mainStage.setOnHiding(event -> {
            if (activeStages.containsKey("ImageCroppingHandler")) {
                Stage imageCroppingStage = activeStages.get("ImageCroppingHandler");
                closeStage(imageCroppingStage);
            }
            for (Stage stage : messagePanes.values()) {
                closeStage(stage);
            }
            Stage stage = activeStages.get("LoginWindow");
            showStage(stage);
        });
        return mainStage;
    }

    public void showCreateAccountWindow() {
        String fxmlName = "/views/createAccountView.fxml";
        Stage stage = viewInitializer.initializeStage(fxmlName, "CreateAccount");
        activeStages.put("CreateAccount", stage);
        stage.setOnHiding(event -> {
            if (activeStages.containsKey("ImageCroppingHandler")) {
                Stage imageCroppingStage = activeStages.get("ImageCroppingHandler");
                closeStage(imageCroppingStage);
            }
            Stage loginStage = activeStages.get("LoginWindow");
            showStage(loginStage);
        });
    }

    public void showMessagePaneWindow(String paneId) {
        if (messagePanes.containsKey(paneId)) {
            Stage stage = messagePanes.get(paneId);
            stage.show();
            stage.toFront();
        } else {
            String fxmlName = "/views/messagePaneView.fxml";
            Stage stage = viewInitializer.initializeStage(fxmlName, paneId);
            MessagePaneController controller = (MessagePaneController) controllers.get(paneId);
            controller.setUserID(paneId);

            stage.widthProperty().addListener((obs, oldVal, newVal) -> {
                // Do whatever you want
                controller.resize();
            });
            messagePanes.put(paneId, stage);
        }
    }

    public Stage showImageCroppingWindow() {
        String fxmlName = "/views/imageCroppingHandlerView.fxml";
        Stage stage = viewInitializer.initializeStage(fxmlName, "ImageCroppingHandler");
        activeStages.put("ImageCroppingHandler", stage);
        return stage;
    }

    public void updateStyles() {
        for (Stage stage : activeStages.values()) {
            Scene scene = stage.getScene();
            String sceneName = stage.getTitle();
            viewInitializer.applyCurrentStylesToScene(scene, sceneName);
        }
        for (Stage stage : messagePanes.values()) {
            Scene scene = stage.getScene();
            String sceneName = stage.getTitle();
            viewInitializer.applyCurrentStylesToScene(scene, sceneName);
        }
    }

    public void closeStage(Stage stageToClose) {
        if (activeStages.containsKey(stageToClose.getTitle())) {
            activeStages.get(stageToClose.getTitle()).close();
            activeStages.remove(stageToClose.getTitle());
        }
        if (messagePanes.containsKey(stageToClose.getTitle())) {
            messagePanes.get(stageToClose.getTitle()).close();
        }
    }

    public void hideStage(Stage stageToHide) {
        if (activeStages.containsKey(stageToHide.getTitle())) {
            activeStages.get(stageToHide.getTitle()).hide();
            return;
        }
        if (messagePanes.containsKey(stageToHide.getTitle())) {
            messagePanes.get(stageToHide.getTitle()).hide();
        }
    }

    public void showStage(Stage stageToShow) {
        if (activeStages.containsKey(stageToShow.getTitle())) {
            activeStages.get(stageToShow.getTitle()).show();
            return;
        }
        if (messagePanes.containsKey(stageToShow.getTitle())) {
            messagePanes.get(stageToShow.getTitle()).show();
        }
    }

    public void minimizeStage(Stage stageToMinimize) {
        if (activeStages.containsKey(stageToMinimize.getTitle())) {
            activeStages.get(stageToMinimize.getTitle()).setIconified(true);
            return;
        }
        if (messagePanes.containsKey(stageToMinimize.getTitle())) {
            messagePanes.get(stageToMinimize.getTitle()).setIconified(true);
        }
    }

    public BaseController getController(String controllerName) {
        return this.controllers.get(controllerName);
    }

    public Map<String, BaseController> getControllers() {
        return this.controllers;
    }
}
