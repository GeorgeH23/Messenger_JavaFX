package com.chatApplication.chatClient.gui.controllers;

import com.chatApplication.chatClient.gui.BaseController;
import com.chatApplication.chatClient.gui.ChatManager;
import com.chatApplication.chatClient.gui.ViewFactory;
import com.chatApplication.chatClient.gui.handlers.ImageHandler;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginWindowController extends BaseController implements Initializable {

    private double initialX;
    private double initialY;
    private ViewFactory viewFactory;
    private ChatManager chatManager;

    @FXML
    private AnchorPane titleBar;
    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Button btnMinimize;
    @FXML
    private Label warningUsername;
    @FXML
    private Label warningPassword;

    public LoginWindowController() {
        super();
        this.viewFactory = super.viewFactory;
        this.chatManager = super.chatManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addDraggableNode(titleBar);
        // This is used for disabling the context menu
        txtUsername.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
        txtPassword.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
    }

    // This method handles the login process which is triggered by the "Login" button
    @FXML
    public final void loginAction() {
        if (txtUsername.getText().equals("")) {
            setStyle(txtUsername, 5.0);
        }
        if (txtPassword.getText().equals("")) {
            setStyle(txtPassword, 5.0);
        }
        if (!txtUsername.getText().equals("") && !txtPassword.getText().equals("")) {
            String loginAttemptResult = chatManager.attemptToLogin(txtUsername.getText(),txtPassword.getText());
            switch (loginAttemptResult) {
                case "Login OK":
                    txtPassword.clear();
                    viewFactory.showMainWindow();
                    MainWindowController controller = (MainWindowController) viewFactory.getController("mainWindowView");
                    controller.setLoggedClientName(chatManager.getLoggedUserLogin());
                    controller.setLoggedUserStatusLight(ImageHandler.getInstance().getStatusImage(chatManager.getLoggedUserStatus()));
                    controller.setLoggedClientStatus(chatManager.getLoggedUserStatus());
                    controller.setLoggedClientPicture();
                    chatManager.setController(controller);
                    break;
                case "No such username":
                    showWarning(warningUsername, 3.0);
                    break;
                case "Incorrect Password":
                    showWarning(warningPassword, 3.0);
                    break;
            }
        }
    }

    // This method handles the new account creation process
    @FXML
    public final void createAccount(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            Stage stage = (Stage) txtPassword.getScene().getWindow();
            viewFactory.hideStage(stage);
            viewFactory.showCreateAccountWindow();
        }
    }

    // This method is responsible for closing the application when the "Exit" button is pressed
    @FXML
    public final void closeAction() {
        chatManager.closeDatabaseConnection();
        Platform.exit();
        System.exit(0);
    }

    // This method is responsible for minimizing the application window when the "Minimize" button is pressed
    @FXML
    public final void minimizeAction() {
        Stage stage = (Stage) btnMinimize.getScene().getWindow();
        viewFactory.minimizeStage(stage);
    }

    // This method is a custom implementation used for enabling the movement of the "MessagePanes" around the screen.
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

    private void showWarning(Node node, double duration) {
        node.setVisible(true);
        PauseTransition visible = new PauseTransition(Duration.seconds(duration));
        visible.setOnFinished(event -> node.setVisible(false));
        visible.play();
    }

    private void setStyle(Node node, double duration) {
        node.setStyle("-fx-prompt-text-fill: red; -fx-font-weight: bold; -fx-background-radius: 15; -fx-background-color: transparent");
        PauseTransition visible = new PauseTransition(Duration.seconds(duration));
        visible.setOnFinished(event -> node.setStyle("-fx-prompt-text-fill: gray; -fx-font-weight: normal; -fx-background-radius: 15; -fx-background-color: transparent"));
        visible.play();
    }
}
