package com.chatApplication.chatClient.gui;

import com.chatApplication.chatClient.gui.controllers.MessagePaneController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ViewFactory {

    private static ViewFactory viewFactory;
    private final Map<String, Stage> activeStages;
    private final Map<String, Stage> messagePanes;
    private final Map<String, BaseController> controllers;

    private ViewFactory() {
        this.activeStages = new HashMap<>();
        this.controllers = new HashMap<>();
        this.messagePanes = new HashMap<>();
    }

    public static ViewFactory getInstance() {
        if (viewFactory == null) {
            viewFactory = new ViewFactory();
        }
        return viewFactory;
    }

    public void showLoginWindow() {
        String fxmlName = "/views/loginWindowView.fxml";
        Stage stage = initializeStage(fxmlName, "loginWindowView");
        activeStages.put("loginWindowView", stage);
    }

    public void showMainWindow() {
        String fxmlName = "/views/mainWindowView.fxml";
        Stage mainStage = initializeStage(fxmlName, "mainWindowView");
        activeStages.put("mainWindowView", mainStage);
        Stage loginStage = activeStages.get("loginWindowView");
        hideStage(loginStage);
        mainStage.setOnHiding(event -> {
            if (activeStages.containsKey("imageCroppingHandler")) {
                Stage imageCroppingStage = activeStages.get("imageCroppingHandler");
                closeStage(imageCroppingStage);
            }
            for (Stage stage : messagePanes.values()) {
                closeStage(stage);
            }
            Stage stage = activeStages.get("loginWindowView");
            showStage(stage);
        });
    }

    public void showCreateAccountWindow() {
        String fxmlName = "/views/createAccountView.fxml";
        Stage stage = initializeStage(fxmlName, "createAccountView");
        activeStages.put("createAccountView", stage);
        stage.setOnHiding(event -> {
            if (activeStages.containsKey("imageCroppingHandler")) {
                Stage imageCroppingStage = activeStages.get("imageCroppingHandler");
                closeStage(imageCroppingStage);
            }
            Stage loginStage = activeStages.get("loginWindowView");
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
            Stage stage = initializeStage(fxmlName, paneId);
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
        Stage stage = initializeStage(fxmlName, "imageCroppingHandler");
        activeStages.put("imageCroppingHandler", stage);
        return stage;
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
            controllers.put(stageTitle, controller);
        } else {
            controllers.put(controllerName, controller);
        }
        Scene scene = new Scene(parent);
        //applyCurrentStylesToScene(scene);
        Stage stage = new Stage();
        stage.setTitle(stageTitle);
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();
        stage.toFront();
        return stage;
    }

    public void closeStage(Stage stageToClose) {
        if (activeStages.containsKey(stageToClose.getTitle())) {
            activeStages.get(stageToClose.getTitle()).close();
            activeStages.remove(stageToClose.getTitle());
        }
        if (messagePanes.containsKey(stageToClose.getTitle())) {
            messagePanes.get(stageToClose.getTitle()).close();
        }
        for (String string : activeStages.keySet()) {
            System.out.println(string);
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
}
