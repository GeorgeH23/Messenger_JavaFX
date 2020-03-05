package com.chatApplication.chatClient.gui.controllers;

import com.chatApplication.chatClient.gui.handlers.ImageCroppingHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class ImageCroppingHandlerController implements Initializable {

    private double initialX;
    private double initialY;
    @FXML
    private AnchorPane titleBar;
    @FXML
    private Button btnMinimize;
    @FXML
    private BorderPane root;
    @FXML
    private Group imageLayer;
    @FXML
    private Button cropButton;
    @FXML
    private ImageView imageView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cropButton.setOnAction(actionEvent -> {
            String response = ImageCroppingHandler.getInstance().cropAction();
            if (response.equals("NOT OK")) {
                System.out.println(response);
            }
        });
        addDraggableNode(titleBar);
    }

    // This method is responsible for closing the application when the "Exit" button is pressed
    @FXML
    public final void closeAction() {
        Stage stage = (Stage) btnMinimize.getScene().getWindow();
        stage.close();
    }

    // This method is responsible for minimizing the application window when the "Minimize" button is pressed
    @FXML
    public final void minimizeAction() {
        Stage stage = (Stage) btnMinimize.getScene().getWindow();
        stage.setIconified(true);
    }
    // This method is a custom implementation used for enabling the movement of the "MessagePanes" around the screen.
    private void addDraggableNode(final Node node) {

        node.setOnMousePressed(event -> {
            if (event.getButton() != MouseButton.MIDDLE) {
                initialX = event.getSceneX();
                initialY = event.getSceneY();
            }});

        node.setOnMouseDragged(event -> {
            if (event.getButton() != MouseButton.MIDDLE) {
                node.getScene().getWindow().setX(event.getScreenX() - initialX);
                node.getScene().getWindow().setY(event.getScreenY() - initialY);
            }
        });
    }


    public BorderPane getRoot() {
        return root;
    }

    public Group getImageLayer() {
        return imageLayer;
    }

    public ImageView getImageView() {
        return imageView;
    }
}
