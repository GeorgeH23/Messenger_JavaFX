package com.chatApplication.chatClient.gui.utility;

import com.chatApplication.chatClient.gui.handlers.ImageHandler;
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
        setUserImage(picturePath);
        setStatusImage(userStatus);
    }

    public final String getLogin() {
        return login;
    }

    public final void setStatusImage(String newStatus) {
        switch (newStatus) {
            case "available" :
                this.statusImage.setValue(ImageHandler.getInstance().getAvailableStatusImage());
                break;
            case "busy" :
                this.statusImage.setValue(ImageHandler.getInstance().getBusyStatusImage());
                break;
            case "away" :
                this.statusImage.setValue(ImageHandler.getInstance().getAwayStatusImage());
                break;
            case "dnd" :
                this.statusImage.setValue(ImageHandler.getInstance().getDndStatusImage());
                break;
            default:
                this.statusImage.setValue(ImageHandler.getInstance().getUnknownStatusImage());
                break;
        }
    }

    public final void setUserImage(String path) {
        try {
            Image image = new Image(new File(path).toURI().toString(), true);
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
