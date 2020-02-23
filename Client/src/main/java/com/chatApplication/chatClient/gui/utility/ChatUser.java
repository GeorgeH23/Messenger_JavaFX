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
    private final ObjectProperty<Image> userImage;

    public ChatUser(final String login, final String picturePath, String userStatus) {
        this.login = login;
        this.statusImage = new SimpleObjectProperty<>();
        this.userImage = new SimpleObjectProperty<>();
        this.userImage.setValue(new Image(new File(picturePath).toURI().toString()));
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

    public final ObjectProperty<ImagePattern> getStatusImage() {
        return statusImage;
    }

    public final ObjectProperty<Image> getUserImage() {
        return userImage;
    }
}
