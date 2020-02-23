package com.chatApplication.chatClient.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
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

    public void setUserImage(Image image) {
        System.out.println(pictureCircle.toString());
        pictureCircle.setFill(new ImagePattern(image));
    }

    public void setStatusImage(ImagePattern image) {
        statusCircle.setFill(image);
    }

    public HBox gethBox() {
        return hBox;
    }
}
