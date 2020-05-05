package com.chatApplication.chatClient.gui.controller;

import com.chatApplication.chatClient.gui.ChatManager;
import com.chatApplication.chatClient.gui.controller.enums.CreateAccountResult;
import com.chatApplication.chatClient.gui.view.ViewFactory;
import com.chatApplication.chatClient.gui.controller.services.CreateAccountService;
import com.chatApplication.chatClient.gui.model.FileChooserGenerator;
import com.chatApplication.chatClient.gui.model.handlers.ImageCroppingHandler;
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
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CreateAccountController extends BaseController implements Initializable {

    private ViewFactory viewFactory;
    private ChatManager chatManager;
    private ImageCroppingHandler imageCroppingHandler;
    private List<String> currentShowingWarnings;
    private double initialX;
    private double initialY;

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
    @FXML
    private Button btnCreate;
    @FXML
    private Button btnBrowse;

    public CreateAccountController() {
        super();
        this.viewFactory = super.viewFactory;
        this.chatManager = super.chatManager;
        this.imageCroppingHandler = ImageCroppingHandler.getInstance();
    }

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
    public final void createAccount() {
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

        btnBrowse.setDisable(true);
        btnCreate.setDisable(true);
        CreateAccountService service = chatManager.createNewAccount(txtUsername.getText(), txtPassword.getText(), txtContact.getText(), txtPath.getText());
        service.setOnSucceeded(event -> {
            btnBrowse.setDisable(false);
            btnCreate.setDisable(false);
            CreateAccountResult response = service.getValue();
            switch (response) {
                case SUCCESS:
                    setupSuccessLabel("Account Successfully Created!","-fx-text-fill: #02d30d; -fx-background-color: transparent;");
                    break;
                case FAILED_BY_USERNAME:
                    setupSuccessLabel("Username Exists!","-fx-text-fill: black;");
                    break;
                case FAILED_BY_UNEXPECTED_ERROR:
                    setupSuccessLabel("Error!","-fx-text-fill: red; -fx-background-color: white;");
                    break;
            }
        });
    }

    @FXML
    public final void btnBrowseAction() {
        File file = FileChooserGenerator.showOpenFile(txtPath.getScene().getWindow());
        if(file != null) {
            Stage stage = viewFactory.showImageCroppingWindow();
            imageCroppingHandler.startPictureCropper(file.getAbsolutePath());

            stage.setOnHiding(windowEvent -> {
                String path = imageCroppingHandler.getCroppedImagePath();
                System.out.println(path);
                txtPath.setText(path);
            });
        }
    }

    // This method is responsible for closing the application when the "Exit" button is pressed
    @FXML
    public final void closeAction() {
        Stage stage = (Stage)txtPassword.getScene().getWindow();
        viewFactory.closeStage(stage);
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

    private void setupSuccessLabel(String text, String style) {
        successNote.setText(text);
        successNote.setStyle(style);
        successNote.setVisible(true);
        PauseTransition visible = new PauseTransition(Duration.seconds(3.0));
        visible.setOnFinished(event -> {
            successNote.setVisible(false);
            if (text.equalsIgnoreCase("Account Successfully Created!")) {
                Stage stage = (Stage) txtPassword.getScene().getWindow();
                viewFactory.closeStage(stage);
            }
        });
        visible.play();
    }
}
