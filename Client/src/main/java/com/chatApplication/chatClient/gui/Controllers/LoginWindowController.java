package com.chatApplication.chatClient.gui.Controllers;

import com.chatApplication.chatClient.gui.AudioHandler;
import com.chatApplication.chatClient.muc.ChatClient;
import com.chatApplication.dataModel.DataSource;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginWindowController implements Initializable {

    private double initialX;
    private double initialY;
    private AudioHandler audio;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        audio = AudioHandler.getInstance();
        audio.load("utils/sounds/chimeup.wav", "login");
        audio.load("utils/sounds/door.wav", "logoff");
        addDraggableNode(titleBar);

        // This is used for disabling the context menu
        txtUsername.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
        txtPassword.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
    }

    @FXML
    public void loginAction() throws IOException{

        if (txtUsername.getText().equals("")) {
            setStyle(txtUsername, 5.0);
        }
        if (txtPassword.getText().equals("")) {
            setStyle(txtPassword, 5.0);
        }

        if (!txtUsername.getText().equals("") && !txtPassword.getText().equals("")) {

            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/fxmlFiles/mainWindow.fxml"));
            Parent parent = fxmlLoader.load();
            MainController controller = fxmlLoader.getController();
            controller.addListeners();
            Stage primaryStage = new Stage();
            // parent.getStylesheets().add(getClass().getResource("/utils/css/fullpackstyling.css").toString());
            primaryStage.setScene(new Scene(parent));
            primaryStage.initStyle(StageStyle.TRANSPARENT);

            if (ChatClient.getInstance().connect()) {

                String serverResponse;
                try {
                    serverResponse = ChatClient.getInstance().login(txtUsername.getText(), txtPassword.getText());
                    if (serverResponse.equalsIgnoreCase("Login OK")) {

                        audio.play("login", 0);
                        txtPassword.getScene().getWindow().hide();
                        primaryStage.show();
                        String loggedUserName = ChatClient.getInstance().getThisClientLogin();
                        controller.setMyPicture(loggedUserName);
                        controller.setLoggedClientName(loggedUserName);

                        File file = new File(System.getProperty("user.home") + File.separator + "Documents" + File.separator +
                                "MessengerApplication" + File.separator + loggedUserName + ".jpg");
                        if (file.isFile()) {
                            controller.setLoggedClientPicture(file.toURI().toURL().toString());
                        } else {
                            String filePath = new File(getClass().getClassLoader().getResource("utils/images/users/user.png").getFile()).toURI().toString();
                            controller.setLoggedClientPicture(filePath);
                        }

                        primaryStage.setOnHiding(event -> {
                            try {
                                ChatClient.getInstance().logoff();
                                audio.play("logoff", 0);
                                txtPassword.clear();
                                ((Stage) txtPassword.getScene().getWindow()).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                    } else if (serverResponse.equalsIgnoreCase("No such username")){
                        controller.removeListeners();
                        showWarning(warningUsername, 3.0);
                    } else if (serverResponse.equalsIgnoreCase("Incorrect Password")) {
                        controller.removeListeners();
                        showWarning(warningPassword, 3.0);
                    } else {
                        controller.removeListeners();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                controller.removeListeners();
            }
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

    @FXML
    public void createAccount(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getButton() == MouseButton.PRIMARY){

            Stage primaryStage = new Stage();
            primaryStage.setTitle("Messenger");
            Scene root = new Scene((FXMLLoader.load(getClass().getResource("/fxmlFiles/createAccountWindow.fxml"))), 420, 450);
            // Add CSS stylesheet for the main window
            // root.getStylesheets().add(getClass().getResource("/utils/css/createAccount.css").toString());
            primaryStage.setScene(root);
            primaryStage.initStyle(StageStyle.TRANSPARENT);

            ((Stage)txtPassword.getScene().getWindow()).hide();

            primaryStage.show();
            primaryStage.setOnHiding(event -> ((Stage)txtPassword.getScene().getWindow()).show());
        }
    }

    @FXML
    public void closeAction() {
        DataSource.getInstance().close();
        Platform.exit();
    }

    @FXML
    public void minimizeAction() {
        Stage stage = (Stage) btnMinimize.getScene().getWindow();
        stage.setIconified(true);
    }

    private void showWarning(Node node, double duration) {
        node.setVisible(true);
        PauseTransition visible = new PauseTransition(Duration.seconds(duration));
        visible.setOnFinished(event -> node.setVisible(false));
        visible.play();
    }

    private void setStyle(Node node, double duration) {
        node.setStyle("-fx-prompt-text-fill: red; -fx-font-weight: bold;");
        PauseTransition visible = new PauseTransition(Duration.seconds(duration));
        visible.setOnFinished(event -> node.setStyle("-fx-prompt-text-fill: gray; -fx-font-weight: normal;"));
        visible.play();
    }
}
