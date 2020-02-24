package com.chatApplication.chatClient.gui.controllers;

import com.chatApplication.chatClient.gui.handlers.AudioHandler;
import com.chatApplication.chatClient.gui.handlers.ImageHandler;
import com.chatApplication.chatClient.gui.utility.ChatUser;
import com.chatApplication.chatClient.gui.utility.FileChooserGenerator;
import com.chatApplication.chatClient.gui.utility.MessagePane;
import com.chatApplication.chatClient.gui.utility.UserListViewCell;
import com.chatApplication.chatClient.muc.*;
import com.chatApplication.dataModel.DataSource;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import tray.animations.AnimationType;
import tray.notification.NotificationType;
import tray.notification.TrayNotification;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MainWindowController implements UserStatusListener, MessageListener, UserAvailabilityListener, PictureChangeListener {

    private static final ChatClient CHAT_CLIENT = ChatClient.getInstance();
    private final Map<String, MessagePane> messagePanes = new HashMap<>();
    private ObservableList<ChatUser> loggedClientsList;
    private long startTime;
    private double initialX;
    private double initialY;
    private AudioHandler audio;

    @FXML
    private ListView<ChatUser> clients;
    @FXML
    private TitledPane titledPane;
    @FXML
    private AnchorPane titleBar;
    @FXML
    private Circle loggedClientPicture;
    @FXML
    private Label loggedClientName;
    @FXML
    private MenuButton statusMenu;
    @FXML
    private Circle loggedUserStatusLight;
    @FXML
    private Pane changeImagePane;
    @FXML
    private Arc changeImageArc;
    @FXML
    private ImageView changeImageIcon;

    public void initialize() {

        audio = AudioHandler.getInstance();
        audio.load("/utils/sounds/offline.wav", "offline");
        audio.load("/utils/sounds/online.wav", "online");
        audio.load("/utils/sounds/message.wav", "message");
        audio.load("/utils/sounds/click.wav", "click");
        addDraggableNode(titleBar);
        changeImageArc.setStroke(Color.TRANSPARENT);
        changeImageArc.setVisible(false);
        changeImageIcon.setVisible(false);

        changeImagePane.setOnMouseEntered(mouseEvent -> {
            changeImageArc.setVisible(true);
            changeImageIcon.setVisible(true);
        });
        changeImagePane.setOnMouseExited(mouseEvent -> {
            changeImageArc.setVisible(false);
            changeImageIcon.setVisible(false);
        });

        startTime = System.currentTimeMillis();
        titledPane.setEffect(new DropShadow(10, Color.BLUE));
        loggedClientsList = FXCollections.observableArrayList(chatUser -> new Observable[] {chatUser.getStatusImage(), chatUser.getUserImage()});
        clients.setItems(loggedClientsList);
        clients.getItems().sort(Comparator.comparing(o -> o.getLogin().toLowerCase()));
        clients.setCellFactory(userListView -> new UserListViewCell());
        clients.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    @Override
    public void onMessage(String fromLogin, String msgBody) {

        String processedMsg = msgBody.replaceAll("##NL##", "\n");
        String login = fromLogin.replaceAll("\\p{Punct}", "");

        Platform.runLater(() -> {
            audio.play("message", 0);
            showNotification(fromLogin, "message");

            if (!messagePanes.containsKey(login)) {
                try {
                    createNewMessagePane(login);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            MessagePaneController messagePaneController = messagePanes.get(login).getController();
            messagePaneController.setUserID(login);
            for (ChatUser user : loggedClientsList) {
                if (user.getLogin().equals(login)) {
                    messagePaneController.setUserImage(user.getUserImage().getValue());
                    break;
                }
            }
            messagePaneController.onMessage(fromLogin, processedMsg);
            messagePanes.get(login).getStage().show();
        });
    }

    @Override
    public void online(String login, String userStatus) {
        String username = login.replaceAll("\\p{Punct}", "");
        long timeDifference = System.currentTimeMillis() - startTime;
        String path = DataSource.getInstance().queryUserPicture(username);

        Platform.runLater(() -> {
            loggedClientsList.add(new ChatUser(username, path, userStatus));
            clients.getItems().sort(Comparator.comparing(o -> o.getLogin().toLowerCase()));
            showNotification(login, "logon", timeDifference);
        });
    }

    @Override
    public void offline(String login) {

        Platform.runLater(() -> {
            String username = login.replaceAll("\\p{Punct}", "");
            ChatUser user = null;
            for (ChatUser listUser : loggedClientsList) {
                if (listUser.getLogin().equals(username)) {
                    user = listUser;
                }
            }
            loggedClientsList.remove(user);

            showNotification(login, "logoff");
        });
    }

    @Override
    public void availabilityStatus(String login, String newStatus) {
        Platform.runLater(() -> {
            String username = login.replaceAll("\\p{Punct}", "");
            for (ChatUser user : loggedClientsList) {
                if (user.getLogin().equals(username)) {
                    user.setStatusImage(newStatus);
                    break;
                }
            }
        });
    }

    @Override
    public void onPictureChanged(String login) {
        String username = login.replaceAll("\\p{Punct}", "");
        String path = DataSource.getInstance().queryUserPicture(username);
        for (ChatUser user : loggedClientsList) {
            if (user.getLogin().equals(username)) {
                user.setUserImage(path);
            }
        }
    }

    @FXML
    private void handleClickListView(MouseEvent mouseEvent) throws IOException {

        // If the item on the loggedClientsList is double clicked.
        if ((mouseEvent.getButton() == MouseButton.PRIMARY) && (mouseEvent.getClickCount() > 1) && (mouseEvent.getTarget() != null)) {
            String login = clients.getSelectionModel().getSelectedItem().getLogin();
            audio.play("click", 0);

            // If a message pane is already open for the current login bring it to front.
            if (login != null) {
                if (messagePanes.containsKey(login)) {
                    Stage stage = messagePanes.get(login).getStage();
                    stage.show();
                    stage.toFront();
                    stage.setIconified(false);
                    return;
                }
                // Otherwise create a new message pane for the current login.
                createNewMessagePane(login);
            }
        }
    }

    @FXML
    private void closeAction() {
        for (MessagePane pane : messagePanes.values()) {
            pane.getStage().close();
        }
        CHAT_CLIENT.removeUserStatusListener(this);
        CHAT_CLIENT.removeMessageListener(this);
        showNotification("", "goodbye");
        titleBar.getScene().getWindow().hide();
    }

    @FXML
    private void minimizeAction() {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void changeStatusToAvailable() throws IOException {
        CHAT_CLIENT.availabilityChange("available");
        statusMenu.setText("Available");
        setLoggedUserStatusLight(ImageHandler.getInstance().getAvailableStatusImage());
    }

    @FXML
    private void changeStatusToBusy() throws IOException {
        CHAT_CLIENT.availabilityChange("busy");
        statusMenu.setText("Busy");
        setLoggedUserStatusLight(ImageHandler.getInstance().getBusyStatusImage());
    }

    @FXML
    private void changeStatusToDoNotDisturb() throws IOException {
        CHAT_CLIENT.availabilityChange("dnd");
        statusMenu.setText("Do Not Disturb");
        setLoggedUserStatusLight(ImageHandler.getInstance().getDndStatusImage());
    }

    @FXML
    private void changeStatusToAway() throws IOException {
        CHAT_CLIENT.availabilityChange("away");
        statusMenu.setText("Away");
        setLoggedUserStatusLight(ImageHandler.getInstance().getAwayStatusImage());
    }

    @FXML
    private void changePicture() throws IOException {
        File newImage = FileChooserGenerator.showOpenFile(loggedClientPicture.getScene().getWindow());
        if (newImage != null) {
            DataSource.getInstance().updateUserPicture(this.loggedClientName.getText(), newImage.getAbsolutePath());
            try {
                Image image = new Image(newImage.toURI().toURL().toString(), true);
                image.progressProperty().addListener((obs, ov, nv) -> {
                    if (nv.equals(1.0)) {
                        ImageHandler.getInstance().changeCurrentLoggedUserImage(image);
                        loggedClientPicture.setFill(ImageHandler.getInstance().getCurrentLoggedUserImage());
                    }
                });
            } catch (MalformedURLException e) {
                setLoggedClientPicture();
                e.printStackTrace();
            }
            CHAT_CLIENT.pictureChange();
        }
    }

    // This function is used for moving the MessagePanes on the screen.
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

    private void showNotification(String login, String notificationType, long... time) {

        TrayNotification tray = new TrayNotification();
        AnimationType animation = AnimationType.POPUP;
        String title = null;
        String message = null;

        // Set the online/login notification depending on the login time.
        if (notificationType.equals("logon")) {
            title = "Logon notification";
            if (time[0] > 200) {
                message = login + " just logged in.";
                audio.play("online", 0);
            } else {
                return;
                //message = login + " is online.";
            }
            tray.setNotificationType(NotificationType.SUCCESS);

            // Set the logoff notification.
        } else if (notificationType.equals("logoff")) {
            title = "Logoff notification";
            message = login + " just logged off.";
            tray.setNotificationType(NotificationType.ERROR);
            audio.play("offline", 0);
            // Set the new message notification.
        } else if (notificationType.equals("message")) {
            title = "New message notification";
            message = "You have a new message from " + login + ".";
            tray.setNotificationType(NotificationType.NOTICE);
        } else if (notificationType.equals("welcome")) {
            title = "Welcome \"" + this.loggedClientName.getText() + "\"!";
            message = "It's nice to see you back online!";
            tray.setNotificationType(NotificationType.SUCCESS);
        } else if (notificationType.equals("goodbye")) {
            title = "Goodbye \"" + this.loggedClientName.getText() + "\"!";
            message = "We hope we'll see you back online soon!";
            tray.setNotificationType(NotificationType.NOTICE);
        }

        // Display the notification on the screen.
        tray.setTitle(title);
        tray.setMessage(message);
        tray.setAnimationType(animation);
        tray.showAndDismiss(Duration.seconds(3));
    }

    private void createNewMessagePane(String login) throws IOException {

        // Create a new MessagePane window.
        MessagePane pane = new MessagePane(login);

        // Add the new MessagePane window to the loggedClientsList of opened MessagePane windows.
        messagePanes.put(login, pane);
        System.out.println(messagePanes.keySet());
    }

    private void setLoggedUserStatusLight(ImagePattern path) {
        loggedUserStatusLight.setFill(path);
    }

    public final void removeListeners() {
        CHAT_CLIENT.removeUserStatusListener(this);
        CHAT_CLIENT.removeMessageListener(this);
        CHAT_CLIENT.removeUserAvailabilityListener(this);
        CHAT_CLIENT.removePictureChangeListener(this);
    }

    public final void addListeners() {
        CHAT_CLIENT.addUserStatusListener(this);
        CHAT_CLIENT.addMessageListener(this);
        CHAT_CLIENT.addUserAvailabilityListener(this);
        CHAT_CLIENT.addPictureChangeListener(this);
    }

    public final void setLoggedClientName(String name) {
        loggedClientName.setText(name);
        showNotification("", "welcome");
    }

    public final void setLoggedClientPicture() {
        loggedClientPicture.setFill(ImageHandler.getInstance().getCurrentLoggedUserImage());
    }

    public final void setLoggedClientStatus(String loggedUserStatus) {
        switch (loggedUserStatus) {
            case "available" :
                statusMenu.setText("Available");
                setLoggedUserStatusLight(ImageHandler.getInstance().getAvailableStatusImage());
                break;
            case "busy" :
                statusMenu.setText("Busy");
                setLoggedUserStatusLight(ImageHandler.getInstance().getBusyStatusImage());
                break;
            case "away" :
                statusMenu.setText("Away");
                setLoggedUserStatusLight(ImageHandler.getInstance().getAwayStatusImage());
                break;
            case "dnd" :
                statusMenu.setText("Do Not Disturb");
                setLoggedUserStatusLight(ImageHandler.getInstance().getDndStatusImage());
                break;
            default:
                statusMenu.setText("Unknown Status");
                setLoggedUserStatusLight(ImageHandler.getInstance().getUnknownStatusImage());
                break;
        }
    }
}