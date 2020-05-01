package com.chatApplication.chatClient.gui.model.utility;

import com.chatApplication.chatClient.gui.model.handlers.ImageHandler;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import java.io.File;

public class ChatUser {

    private final String login;
    private final ObjectProperty<ImagePattern> statusImage;
    private final ObjectProperty<ImagePattern> userImage;

    public ChatUser(final String login, final String picturePath, String userStatus) {
        this.login = login;
        this.statusImage = new SimpleObjectProperty<>();
        this.userImage = new SimpleObjectProperty<>();
        setStatusImage(userStatus);
        this.userImage.setValue(ImageHandler.getInstance().getUnknownUserImage());
    }

    public final String getLogin() {
        return login;
    }

    public final void setStatusImage(String newStatus) {
        this.statusImage.setValue(ImageHandler.getInstance().getStatusImage(newStatus));
    }

    public final void setUserImage(String path) {
        try {
            Image image;
            if (path != null) {
                image = new Image(new File(path).toURI().toString(), true);
            } else {
                image = new Image(getClass().getResource("/utils/images/users/user.png").toURI().toURL().toString());
            }
            image.progressProperty().addListener((obs, ov, nv) -> {
                if (nv.equals(1.0)) {
                    this.userImage.setValue(new ImagePattern(image));
                }
            });
        } catch (Exception e) {
            this.userImage.setValue(ImageHandler.getInstance().getUnknownUserImage());
            e.printStackTrace();
        }
    }

    public final ObjectProperty<ImagePattern> getStatusImage() {
        return statusImage;
    }

    public final ObjectProperty<ImagePattern> getUserImage() {
        return userImage;
    }
}
