package com.chatApplication.chatClient.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class UserListViewCell extends ListCell<ChatUser> {

    @FXML
    private HBox hBox;
    @FXML
    private Circle pictureCircle;
    @FXML
    private Circle statusCircle;

    private FXMLLoader loader;

    @Override
    protected void updateItem(ChatUser chatUser, boolean empty) {
        super.updateItem(chatUser, empty);

        if (empty || chatUser == null) {
            setText(null);
            setGraphic(null);
        } else {
            loader = new FXMLLoader(getClass().getResource("/fxmlFiles/listViewCell.fxml"));
            loader.setController(this);
            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            setText(chatUser.getLogin());
            String path = new File(chatUser.getPicturePath()).toURI().toString();
            pictureCircle.setFill(new ImagePattern(new Image(path)));
            String filePath = chatUser.getStatusLightPath();
            try {
                statusCircle.setFill(new ImagePattern(new Image(getClass().getResource(filePath).toURI().toURL().toString())));
            } catch (MalformedURLException | URISyntaxException e) {
                e.printStackTrace();
            }
            setGraphic(hBox);
        }
    }
}
