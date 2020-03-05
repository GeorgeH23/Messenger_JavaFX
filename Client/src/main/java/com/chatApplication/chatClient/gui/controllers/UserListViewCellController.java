package com.chatApplication.chatClient.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import java.io.IOException;

public class UserListViewCellController {
    @FXML
    private HBox hBox;
    @FXML
    private Circle pictureCircle;
    @FXML
    private Circle statusCircle;


    public UserListViewCellController() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/userListViewCellView.fxml"));
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final void setUserImage(ImagePattern image) {
        pictureCircle.setFill(image);
    }

    public final void setStatusImage(ImagePattern image) {
        statusCircle.setFill(image);
    }

    public final HBox gethBox() {
        return hBox;
    }
}
