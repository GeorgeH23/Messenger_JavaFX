package com.chatApplication.chatClient.gui.controllers;

import com.chatApplication.chatClient.gui.*;
import com.chatApplication.dataModel.DataSource;
import com.chatApplication.chatClient.muc.ChatClient;
import com.chatApplication.chatClient.muc.MessageListener;
import com.chatApplication.chatClient.muc.UserAvailabilityListener;
import com.chatApplication.chatClient.muc.UserStatusListener;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import tray.animations.AnimationType;
import tray.notification.NotificationType;
import tray.notification.TrayNotification;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MainWindowController implements UserStatusListener, MessageListener, UserAvailabilityListener {

    private static final ChatClient CHAT_CLIENT = ChatClient.getInstance();
    private final Map<String, MessagePane> messagePanes = new HashMap<>();
    private ObservableList<ChatUser> list;
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

    public void initialize() {

        audio = AudioHandler.getInstance();
        audio.load("/utils/sounds/offline.wav", "offline");
        audio.load("/utils/sounds/online.wav", "online");
        audio.load("/utils/sounds/message.wav", "message");
        audio.load("/utils/sounds/click.wav", "click");

        addDraggableNode(titleBar);

        startTime = System.currentTimeMillis();

        titledPane.setEffect(new DropShadow(10, Color.BLUE));

        list = FXCollections.observableArrayList( chatUser -> new Observable[] {chatUser.getStatusImage(), chatUser.getUserImage()});
        clients.setItems(list);
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
                    createMessagePane(login);
                    messagePanes.get(login).getController().setUserID(login);
                    messagePanes.get(login).getController().onMessage(fromLogin, processedMsg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                messagePanes.get(login).getStage().show();
                messagePanes.get(login).getController().setUserID(login);
                messagePanes.get(login).getController().onMessage(fromLogin, processedMsg);
            }
        });
    }

    @Override
    public void online(String login, String userStatus) {
        String username = login.replaceAll("\\p{Punct}", "");
        long timeDifference = System.currentTimeMillis() - startTime;
        String path = DataSource.getInstance().queryUserPicture(username);

        Platform.runLater(() -> {
            list.add(new ChatUser(username, path, userStatus));
            clients.getItems().sort(Comparator.comparing(o -> o.getLogin().toLowerCase()));
            showNotification(login, "logon", timeDifference);
        });
    }

    @Override
    public void offline(String login) {

        Platform.runLater(() -> {
            String username = login.replaceAll("\\p{Punct}", "");
            ChatUser user = null;
            for (ChatUser listUser : list) {
                if (listUser.getLogin().equals(username)) {
                    user = listUser;
                }
            }
            list.remove(user);

            showNotification(login, "logoff");
        });
    }

    @Override
    public void availabilityStatus(String login, String newStatus) {
        Platform.runLater(() -> {
            String username = login.replaceAll("\\p{Punct}", "");
            for (ChatUser user : list) {
                if (user.getLogin().equals(username)) {
                    user.setStatusImage(newStatus);
                    break;
                }
            }
        });
    }

    @FXML
    private void handleClickListView(MouseEvent mouseEvent) throws IOException {

        // If the item on the list is double clicked.
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
                createMessagePane(login);
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
        }

        // Display the notification on the screen.
        tray.setTitle(title);
        tray.setMessage(message);
        tray.setAnimationType(animation);
        tray.showAndDismiss(Duration.seconds(3));
    }

    private void createMessagePane(String login) throws IOException {

        // Create a new MessagePane window.
        MessagePane pane = new MessagePane(login);

        // Add the new MessagePane window to the list of opened MessagePane windows.
        messagePanes.put(login, pane);
        System.out.println(messagePanes.keySet());
    }

    public void removeListeners() {
        CHAT_CLIENT.removeUserStatusListener(this);
        CHAT_CLIENT.removeMessageListener(this);
        CHAT_CLIENT.removeUserAvailabilityListener(this);
    }

    public void addListeners() {
        CHAT_CLIENT.addUserStatusListener(this);
        CHAT_CLIENT.addMessageListener(this);
        CHAT_CLIENT.addUserAvailabilityListener(this);
    }

    public void setLoggedClientName(String name) {
        loggedClientName.setText(name);
    }

    public void setLoggedClientPicture() {
        loggedClientPicture.setFill(ImageHandler.getInstance().getCurrentLoggedUserImage());
    }

    public void setLoggedClientStatus(String loggedUserStatus) {
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

    private void setLoggedUserStatusLight(ImagePattern path) {
        loggedUserStatusLight.setFill(path);
    }
}