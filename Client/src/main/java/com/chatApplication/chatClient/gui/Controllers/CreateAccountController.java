package com.chatApplication.chatClient.gui.Controllers;

import com.chatApplication.common.NewUser;
import com.chatApplication.common.PasswordHasher;
import com.chatApplication.dataModel.DataSource;
import javafx.animation.PauseTransition;
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
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CreateAccountController implements Initializable {

    private double initialX;
    private double initialY;
    private List<String> currentShowingWarnings;
    private final DataSource DATA_SOURCE = DataSource.getInstance();

    @FXML
    private AnchorPane titleBar;
    @FXML
    private Button btnMinimize;
    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private PasswordField txtPasswordConfirm;
    @FXML
    private TextField txtContact;
    @FXML
    private TextField txtPath;
    @FXML
    private Label warningContactNumber;
    @FXML
    private Label warningPassword;
    @FXML
    private Label warningUsername;
    @FXML
    private Label successNote;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addDraggableNode(titleBar);

        txtUsername.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
        txtPassword.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
        txtPasswordConfirm.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
        txtContact.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
        txtPath.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);

        currentShowingWarnings = new ArrayList<>();

        // In order to allow only numbers 0-9 to be typed for the contact number
        txtContact.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches("\\d*")) {
                return;
            }
            txtContact.setText(newValue.replaceAll("[^\\d]", ""));
            if (!currentShowingWarnings.contains(warningContactNumber.getId())) {
                currentShowingWarnings.add(warningContactNumber.getId());
                showWarning(warningContactNumber, 3.0, "Should contain only numbers 0-9");
            }
        });

    }

    @FXML
    public void createAccount() {
        if (txtUsername.getText().equals("")){
            if (!currentShowingWarnings.contains(warningUsername.getId())) {
                currentShowingWarnings.add(warningUsername.getId());
                showWarning(warningUsername, 3.0, "Username field can't be empty");
            }
            return;
        }
        if (!txtUsername.getText().equals("") && !txtUsername.getText().matches("^[a-zA-z0-9_-]{5,16}$")){
            if (!currentShowingWarnings.contains(warningUsername.getId())) {
                currentShowingWarnings.add(warningUsername.getId());
                showWarning(warningUsername, 3.0, "Must be 5-16 characters(A-Z,a-z,0-9,_,-)");
            }
            return;
        }
        if (txtPassword.getText().equals("")) {
            currentShowingWarnings.add(warningPassword.getId());
            showWarning(warningPassword, 3.0, "Password can't be empty");
            return;
        }
        if (!txtPassword.getText().equals("") || !txtPasswordConfirm.getText().equals("")) {
            if (!txtPassword.getText().equals(txtPasswordConfirm.getText())) {
                if (!currentShowingWarnings.contains(warningPassword.getId())) {
                    currentShowingWarnings.add(warningPassword.getId());
                    showWarning(warningPassword, 3.0, "Passwords do not match");
                }
                return;
            }
        }
        if (!txtContact.getText().matches("^[0-9]{10,14}$")) {
            if (!currentShowingWarnings.contains(warningContactNumber.getId())) {
                currentShowingWarnings.add(warningContactNumber.getId());
                showWarning(warningContactNumber, 3.0, "Incorrect contact number length");
            }
            return;
        }

        String password = "";
        try {
            password = PasswordHasher.getInstance().generateHash(txtPassword.getText());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        NewUser newUser = new NewUser(txtUsername.getText(), password, txtContact.getText(), txtPath.getText());

        String response = DATA_SOURCE.insertNewUser(newUser);

        if (response.equals("New Account Created")) {
            DATA_SOURCE.queryUserPicture(txtUsername.getText());
            System.out.println("New Account Created");

            successNote.setVisible(true);
            PauseTransition visible = new PauseTransition(Duration.seconds(3.0));
            visible.setOnFinished(event -> {
                successNote.setVisible(false);
                ((Stage) txtPassword.getScene().getWindow()).close();
            });
            visible.play();
        }
        if (response.equals("Username exists")) {
            System.out.println("Username exists");
        }
    }

    @FXML
    public void btnBrowseAction() {
        File file = showOpenFile();
        if(file != null) {
            txtPath.setText(file.getAbsolutePath());
        }
    }

    @FXML
    public void closeAction() {
        ((Stage)txtPassword.getScene().getWindow()).close();
    }

    @FXML
    public void minimizeAction() {
        Stage stage = (Stage) btnMinimize.getScene().getWindow();
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

    private void showWarning(Label node, double duration, String text) {
        node.setText(text);
        node.setVisible(true);
        PauseTransition visible = new PauseTransition(Duration.seconds(duration));
        visible.setOnFinished(event -> {
            node.setVisible(false);
            currentShowingWarnings.remove(node.getId());
        });
        visible.play();
    }

    private File showOpenFile(){
        FileChooser fileChooser=new FileChooser();
        fileChooser.setTitle("Student Photo");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG (*.jpg)", "*.jpg"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG (*.png)", "*.png"));
        return fileChooser.showOpenDialog(txtUsername.getScene().getWindow());
    }
}
