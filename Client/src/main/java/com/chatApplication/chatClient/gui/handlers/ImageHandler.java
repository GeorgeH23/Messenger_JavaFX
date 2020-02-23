package com.chatApplication.chatClient.gui.handlers;

import com.chatApplication.dataModel.DataSource;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class ImageHandler {

    private Image availableStatusImage;
    private Image busyStatusImage;
    private Image awayStatusImage;
    private Image dndStatusImage;
    private Image unknownStatusImage;
    private Image currentLoggedUserImage;

    private static ImageHandler instance = new ImageHandler();

    private ImageHandler() {
    }

    public static ImageHandler getInstance() {
        return instance;
    }

    public void loadStatusImages() {
        try {
            System.out.println("loading images now");
            availableStatusImage = new Image(getClass().getResource("/utils/images/icons/ok.png").toURI().toURL().toString(), true);
            busyStatusImage = new Image(getClass().getResource("/utils/images/icons/busy.png").toURI().toURL().toString(),true);
            awayStatusImage = new Image(getClass().getResource("/utils/images/icons/away.png").toURI().toURL().toString(), true);
            dndStatusImage = new Image(getClass().getResource("/utils/images/icons/dnd.png").toURI().toURL().toString(), true);
            unknownStatusImage = new Image(getClass().getResource("/utils/images/icons/unknown.png").toURI().toURL().toString(), true);
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void loadCurrentLoggedUserImage(String userName) {
        DataSource.getInstance().queryUserPicture(userName);

        try {
            File file = new File(System.getProperty("user.home") + File.separator + "Documents" + File.separator +
                    "MessengerApplication" + File.separator + userName + ".jpg");
            if (file.isFile()) {
                currentLoggedUserImage = new Image(file.toURI().toURL().toString());
            } else {
                currentLoggedUserImage = new Image(getClass().getResource("/utils/images/users/user.png").toURI().toURL().toString());
            }
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public ImagePattern getAvailableStatusImage() {
        return new ImagePattern(availableStatusImage);
    }

    public ImagePattern getBusyStatusImage() {
        return new ImagePattern(busyStatusImage);
    }

    public ImagePattern getAwayStatusImage() {
        return new ImagePattern(awayStatusImage);
    }

    public ImagePattern getDndStatusImage() {
        return new ImagePattern(dndStatusImage);
    }

    public ImagePattern getUnknownStatusImage() {
        return new ImagePattern(unknownStatusImage);
    }

    public ImagePattern getCurrentLoggedUserImage() {
        return new ImagePattern(currentLoggedUserImage);
    }
}
