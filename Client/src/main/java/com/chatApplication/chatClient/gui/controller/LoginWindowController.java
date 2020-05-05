package com.chatApplication.chatClient.gui.controller;

import com.chatApplication.chatClient.gui.ChatManager;
import com.chatApplication.chatClient.gui.controller.enums.ChatLoginResult;
import com.chatApplication.chatClient.gui.view.ViewFactory;
import com.chatApplication.chatClient.gui.model.handlers.ImageHandler;
import com.chatApplication.chatClient.gui.controller.services.LoginService;
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
import javafx.scene.text.Text;
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
    @FXML
    private Button btnLogin;
    @FXML
    private Text createNewAccount;
    @FXML
    private Label errorLabel;

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
            errorLabel.setText("Signing in...");
            btnLogin.setDisable(true);
            createNewAccount.setDisable(true);
            Stage mainStage = viewFactory.showMainWindow();
            viewFactory.hideStage(mainStage);
            MainWindowController controller = (MainWindowController) viewFactory.getController("mainWindowView");
            chatManager.setController(controller);

            LoginService loginService = chatManager.attemptToLogin(txtUsername.getText(), txtPassword.getText());
            loginService.setOnSucceeded(event -> {
                ChatLoginResult loginAttemptResult = loginService.getValue();
                switch (loginAttemptResult) {
                    case SUCCESS:
                        errorLabel.setText("");
                        txtPassword.clear();
                        controller.setLoggedClientName(chatManager.getLoggedUserLogin());
                        controller.setLoggedUserStatusLight(ImageHandler.getInstance().getStatusImage(chatManager.getLoggedUserStatus()));
                        controller.setLoggedClientStatus(chatManager.getLoggedUserStatus());
                        controller.setThemeMenuGraphic(chatManager.getTheme());
                        controller.setLoggedClientPicture();
                        Stage loginStage = (Stage) btnLogin.getScene().getWindow();
                        viewFactory.hideStage(loginStage);
                        viewFactory.showStage(mainStage);
                        break;
                    case FAILED_BY_USERNAME:
                        showWarning(warningUsername, 3.0);
                        break;
                    case FAILED_BY_PASSWORD:
                        showWarning(warningPassword, 3.0);
                        break;
                    case FAILED_BY_NETWORK:
                        errorLabel.setText("Couldn't connect to server!");
                        break;
                    case FAILED_BY_UNEXPECTED_ERROR:
                        errorLabel.setText("Unexpected error!");
                        break;
                }
                btnLogin.setDisable(false);
                createNewAccount.setDisable(false);
                errorLabel.setText("");
            });
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
        Platform.exit();
        System.exit(0);
    }

    // This method is responsible for minimizing the application window when the "Minimize" button is pressed
    @FXML
    public final void minimizeAction() {
        Stage stage = (Stage) btnMinimize.getScene().getWindow();
        viewFactory.minimizeStage(stage);
    }

    // This method is a custom implementation used for enabling the movement of the stages around the screen.
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
