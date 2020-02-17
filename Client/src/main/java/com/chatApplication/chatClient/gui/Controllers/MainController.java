package com.chatApplication.chatClient.gui.Controllers;

import com.chatApplication.chatClient.gui.AudioHandler;
import com.chatApplication.chatClient.gui.MessagePane;
import com.chatApplication.chatClient.muc.ChatClient;
import com.chatApplication.chatClient.muc.MessageListener;
import com.chatApplication.chatClient.muc.UserStatusListener;
import com.chatApplication.dataModel.DataSource;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainController implements UserStatusListener, MessageListener {

    private static final ChatClient CHAT_CLIENT = ChatClient.getInstance();
    private final Map<String, MessagePane> messagePanes = new HashMap<>();
    private ObservableList<User> list;
    private long startTime;
    private double initialX;
    private double initialY;
    private AudioHandler audio;

    @FXML
    private ListView<User> clients;
    @FXML
    private TitledPane titledPane;
    @FXML
    private AnchorPane titleBar;
    @FXML
    private Circle loggedClientPicture;
    @FXML
    private Label loggedClientName;


    public void initialize() {

        audio = AudioHandler.getInstance();
        audio.load("utils/sounds/offline.wav", "offline");
        audio.load("utils/sounds/online.wav", "online");
        audio.load("utils/sounds/message.wav", "message");
        audio.load("utils/sounds/click.wav", "click");

        addDraggableNode(titleBar);

        startTime = System.currentTimeMillis();

        titledPane.setEffect(new DropShadow(10, Color.BLUE));

        list = FXCollections.observableArrayList();
        clients.setItems(list);
        clients.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // This cell factory is for adding a Status Circle for each user in the ListView.
        clients.setCellFactory(list -> new ListCell<User>() {
            @Override
            protected void updateItem(final User item, final boolean empty) {

                super.updateItem(item, empty);

                if (!empty) {
                    setText(item.getLogin());
                    setGraphic(item.getProfilePicture());
                } else {
                    setText(null);
                    setGraphic(null);
                }
            }
        });
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
    public void online(String login) {
        String username = login.replaceAll("\\p{Punct}", "");
        //System.out.println(login + " onlineeeeeeeeeeeee");
        long timeDifference = System.currentTimeMillis() - startTime;

        String path = DataSource.getInstance().queryUserPicture(username);

        Platform.runLater(() -> {
            User user = new User(username, path);
            list.add(user);
            showNotification(login, "logon", timeDifference);
        });
    }

    @Override
    public void offline(String login) {

        Platform.runLater(() -> {
            String username = login.replaceAll("\\p{Punct}", "");
            User user = null;
            for (User listUser : list) {
                if (listUser.getLogin().equals(username)) {
                    user = listUser;
                }
            }
            list.remove(user);

            showNotification(login, "logoff");
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
                message = login + " is online.";
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
    }

    public void addListeners() {
        CHAT_CLIENT.addUserStatusListener(this);
        CHAT_CLIENT.addMessageListener(this);
    }

    public void setMyPicture(String name) {
        DataSource.getInstance().queryUserPicture(name);
    }

    public void setLoggedClientName(String name) {
        loggedClientName.setText(name);
    }

    public void setLoggedClientPicture(String path) {
        loggedClientPicture.setFill(new ImagePattern(new Image(path)));
    }

    private class User {
        private final String login;
        private final Circle profilePicture;
        /*private Circle statusLight;
        Circle c1 = new Circle();
        Circle c2 = new Circle();*/

        public User(final String login, final String imagePath) {

            this.login = login;
            this.profilePicture = new Circle(41, 42, 18);

            //this.statusLight = new Circle(10, 11, 7);

            String path = new File(imagePath).toURI().toString();
            profilePicture.setFill(new ImagePattern(new Image(path)));
        }

        /*public void statusLightUpdate(String status){
            if (status.equals("available")){
                statusLight.setFill(Color.LIGHTGREEN);
            }
        }*/

        public String getLogin() {
            return login;
        }

        public Circle getProfilePicture() {
            return profilePicture;
        }

       /*public Circle getStatusLight() {
            return statusLight;
        }*/
    }
}