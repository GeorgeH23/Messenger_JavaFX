package com.chatApplication.chatClient.gui.controllers;

import com.chatApplication.chatClient.gui.BaseController;
import com.chatApplication.chatClient.gui.ChatManager;
import com.chatApplication.chatClient.gui.ViewFactory;
import com.chatApplication.chatClient.gui.handlers.AudioHandler;
import com.chatApplication.chatClient.gui.handlers.ImageCroppingHandler;
import com.chatApplication.chatClient.gui.handlers.ImageHandler;
import com.chatApplication.chatClient.gui.utility.ChatUser;
import com.chatApplication.chatClient.gui.utility.FileChooserGenerator;
import com.chatApplication.chatClient.gui.utility.UserListViewCell;
import javafx.application.Platform;
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

public class MainWindowController extends BaseController {

    private long startTime;
    private double initialX;
    private double initialY;
    private AudioHandler audio;
    private String path;
    private ViewFactory viewFactory;
    private ChatManager chatManager;
    private ImageCroppingHandler imageCroppingHandler;

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

    public MainWindowController() {
        super();
        this.chatManager = super.chatManager;
        this.viewFactory = super.viewFactory;
        this.imageCroppingHandler = ImageCroppingHandler.getInstance();
    }

    public void initialize() {
        addDraggableNode(titleBar);
        audio = AudioHandler.getInstance();
        audio.load("/utils/sounds/online.wav", "online");
        audio.load("/utils/sounds/offline.wav", "offline");
        audio.load("/utils/sounds/message.wav", "message");
        audio.load("/utils/sounds/click.wav", "click");
        audio.load("/utils/sounds/chimeup.wav", "login");
        audio.load("/utils/sounds/door.wav", "logoff");
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
        clients.setItems(chatManager.getLoggedClientsList());
        clients.getItems().sort(Comparator.comparing(o -> o.getLogin().toLowerCase()));
        clients.setCellFactory(userListView -> new UserListViewCell());
        clients.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    public void onMessage(String fromLogin, String message) {
        Platform.runLater(() -> {
            viewFactory.showMessagePaneWindow(fromLogin);
            MessagePaneController controller = (MessagePaneController) viewFactory.getController(fromLogin);
            for (ChatUser user : chatManager.getLoggedClientsList()) {
                if (user.getLogin().equals(fromLogin)) {
                    controller.setUserImage(user.getUserImage().getValue());
                    break;
                }
            }
            controller.onMessage(fromLogin, message);
            showNotification(fromLogin, "message");
        });
    }

    public void online(String login) {
        long timeDifference = System.currentTimeMillis() - startTime;
        clients.getItems().sort(Comparator.comparing(o -> o.getLogin().toLowerCase()));
        showNotification(login, "logon", timeDifference);
    }

    public void offline(String login) {
        showNotification(login, "logoff");
    }

    @FXML
    private void handleClickListView(MouseEvent mouseEvent) throws IOException {
        // If the item on the loggedClientsList is double clicked.
        if ((mouseEvent.getButton() == MouseButton.PRIMARY) && (mouseEvent.getClickCount() > 1) && (mouseEvent.getTarget() != null)) {
            String login = clients.getSelectionModel().getSelectedItem().getLogin();
            audio.play("click", 0);
            viewFactory.showMessagePaneWindow(login);
        }
    }

    @FXML
    private void minimizeAction() {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        viewFactory.minimizeStage(stage);
    }

    @FXML
    private void closeAction() {
        showNotification("", "goodbye");
        Stage stage = (Stage) titleBar.getScene().getWindow();
        viewFactory.closeStage(stage);
        chatManager.removeListeners();
        chatManager.logOffClient();
    }

    @FXML
    private void changeStatusToAvailable() {
        chatManager.changeLoggedUserStatus("available");
        statusMenu.setText("Available");
        setLoggedUserStatusLight(ImageHandler.getInstance().getStatusImage("available"));
    }

    @FXML
    private void changeStatusToBusy() {
        chatManager.changeLoggedUserStatus("busy");
        statusMenu.setText("Busy");
        setLoggedUserStatusLight(ImageHandler.getInstance().getStatusImage("busy"));
    }

    @FXML
    private void changeStatusToDoNotDisturb() {
        chatManager.changeLoggedUserStatus("dnd");
        statusMenu.setText("Do Not Disturb");
        setLoggedUserStatusLight(ImageHandler.getInstance().getStatusImage("dnd"));
    }

    @FXML
    private void changeStatusToAway() {
        chatManager.changeLoggedUserStatus("away");
        statusMenu.setText("Away");
        setLoggedUserStatusLight(ImageHandler.getInstance().getStatusImage("away"));
    }

    @FXML
    private void changePicture() {
        File newImage = FileChooserGenerator.showOpenFile(loggedClientPicture.getScene().getWindow());

        if (newImage != null) {
            Stage stage = viewFactory.showImageCroppingWindow();
            imageCroppingHandler.pictureCropper(newImage.getAbsolutePath());
            stage.setOnHiding(windowEvent -> {
                this.path = imageCroppingHandler.getCroppedImagePath();
                System.out.println(this.path);
                if (this.path != null) {
                    chatManager.pictureChange(loggedClientName.getText(), path);
                    try {
                        File file = new File(path);
                        file.deleteOnExit();

                        Image image = new Image(file.toURI().toURL().toString(), true);
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
                }
            });

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
        switch (notificationType) {
            case "logon":
                title = "Logon notification";
                if (time[0] > 500) {
                    message = login + " just logged in.";
                    audio.play("online", 0);
                } else {
                    return;
                    //message = login + " is online.";
                }
                tray.setNotificationType(NotificationType.SUCCESS);
                break;
            // Set the logoff notification.
            case "logoff":
                title = "Logoff notification";
                message = login + " just logged off.";
                tray.setNotificationType(NotificationType.ERROR);
                audio.play("offline", 0);
                break;
            // Set the new message notification.
            case "message":
                title = "New message notification";
                message = "You have a new message from " + login + ".";
                tray.setNotificationType(NotificationType.NOTICE);
                audio.play("message", 0);
                break;
            // Set the welcome notification.
            case "welcome":
                title = "Welcome \"" + this.loggedClientName.getText() + "\"!";
                message = "It's nice to see you back online!";
                tray.setNotificationType(NotificationType.SUCCESS);
                audio.play("login", 0);
                break;
            // Set the good bye notification.
            case "goodbye":
                title = "Goodbye \"" + this.loggedClientName.getText() + "\"!";
                message = "We hope we'll see you back online soon!";
                tray.setNotificationType(NotificationType.NOTICE);
                audio.play("logoff", 0);
                break;
        }

        // Display the notification on the screen.
        tray.setTitle(title);
        tray.setMessage(message);
        tray.setAnimationType(animation);
        tray.showAndDismiss(Duration.seconds(3));
    }

    public void setLoggedUserStatusLight(ImagePattern path) {
        loggedUserStatusLight.setFill(path);
    }

    public final void setLoggedClientName(String name) {
        loggedClientName.setText(name);
        showNotification("", "welcome");
    }

    public final void setLoggedClientPicture() {
        loggedClientPicture.setFill(ImageHandler.getInstance().getCurrentLoggedUserImage());
    }

    public final void setLoggedClientStatus(String loggedUserStatusText) {
        switch (loggedUserStatusText) {
            case "available" :
                loggedUserStatusText = "Available";
                break;
            case "busy" :
                loggedUserStatusText = "Busy";
                break;
            case "away" :
                loggedUserStatusText = "Away";
                break;
            case "dnd" :
                loggedUserStatusText = "Do Not Disturb";
                break;
            default:
                loggedUserStatusText = "Unknown Status";
                break;
        }
        statusMenu.setText(loggedUserStatusText);
    }
}