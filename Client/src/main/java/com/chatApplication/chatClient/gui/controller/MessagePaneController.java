package com.chatApplication.chatClient.gui.controller;

import com.chatApplication.chatClient.gui.ChatManager;
import com.chatApplication.chatClient.gui.view.Theme;
import com.chatApplication.chatClient.gui.view.ViewFactory;
import com.chatApplication.chatClient.gui.model.handlers.AudioHandler;
import com.chatApplication.chatClient.gui.model.handlers.ImageHandler;
import com.chatApplication.chatClient.gui.model.MessageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MessagePaneController extends BaseController {

    private ViewFactory viewFactory;
    private ChatManager chatManager;
    private String myself;
    private double initialX;
    private double initialY;
    private AudioHandler audio;
    private ImagePattern userImage;
    private ImageHandler imageHandler;

    @FXML
    private Button btnMinimize;
    @FXML
    private Button btnClose;
    @FXML
    public Label userID;
    @FXML
    public AnchorPane rootPane;
    @FXML
    private TextArea txtMsg;
    @FXML
    private AnchorPane titleBar;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox chatBox;

    public MessagePaneController() {
        super();
        this.viewFactory = super.viewFactory;
        this.chatManager = super.chatManager;
        this.myself = chatManager.getLoggedUserLogin();
        this.imageHandler = ImageHandler.getInstance();
    }

    @FXML
    private void initialize() {
        addDraggableNode(titleBar);

        audio = AudioHandler.getInstance();
        audio.load("/utils/sounds/sent.wav", "sent");
        // Automatically scroll down to the last message in the conversation.
        chatBox.heightProperty().addListener((observable, oldValue, newValue) -> {
                    chatBox.layout();
                    scrollPane.setVvalue(1.0d);
                }
        );
    }

    @FXML
    public final void sendAction() {
        String sendTo = userID.getText();
        String message = txtMsg.getText().trim();

        if (chatManager.sendMessage(sendTo,message)) {
            createMessage(message, MessageType.SENT);
            audio.play("sent", 0);
            txtMsg.clear();
        }
    }

    private void createMessage(String message, MessageType messageType) {
        Text text = new Text(message);
        text.setFill(Color.WHITE);
        text.getStyleClass().add("message");
        TextFlow tempFlow = new TextFlow();
        Text txtName = new Text();
        Circle img = new Circle(42, 42, 30);

        if (messageType.equals(MessageType.SENT)) {
            txtName = new Text(getCurrentLocalDateTimeStamp() + "\n" + myself + "\n");

            try {
                img.setFill(imageHandler.getCurrentLoggedUserImage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (messageType.equals(MessageType.RECEIVED)) {
            txtName = new Text(getCurrentLocalDateTimeStamp() + "\n" + userID.getText() + "\n");

            try {
                if (userImage != null) {
                    img.setFill(userImage);
                } else {
                    img.setFill(imageHandler.getUnknownUserImage());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        txtName.getStyleClass().add("txtName");
        tempFlow.getChildren().add(txtName);
        tempFlow.getChildren().add(text);
        tempFlow.setMaxWidth(270);
        TextFlow flow = new TextFlow(tempFlow);

        HBox hbox = new HBox(12);

        img.getStyleClass().add("imageView");
        if (messageType.equals(MessageType.SENT)) {
            text.setFill(Color.WHITE);
            tempFlow.getStyleClass().add("tempFlow");
            flow.getStyleClass().add("textFlow");
            hbox.setAlignment(Pos.BOTTOM_RIGHT);
            hbox.getChildren().add(flow);
            hbox.getChildren().add(img);
        } else if (messageType.equals(MessageType.RECEIVED)) {
            tempFlow.getStyleClass().add("tempFlowFlipped");
            flow.getStyleClass().add("textFlowFlipped");
            chatBox.setAlignment(Pos.TOP_LEFT);
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.getChildren().add(img);
            hbox.getChildren().add(flow);
        }
        hbox.getStyleClass().add("hbox");
        Platform.runLater(() -> chatBox.getChildren().addAll(hbox));
    }

    // This method is responsible for closing the application when the "Exit" button is pressed
    @FXML
    public final void closeAction() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        viewFactory.hideStage(stage);
    }

    // This method is responsible for minimizing the application window when the "Minimize" button is pressed
    @FXML
    public final void minimizeAction() {
        Stage stage = (Stage) btnMinimize.getScene().getWindow();
        viewFactory.minimizeStage(stage);
    }

    // This method is a custom implementation used for enabling the movement of the stages around the screen.
    private void addDraggableNode(final Node node) {
        node.setOnMousePressed(me -> {
            if (me.getButton() != MouseButton.MIDDLE) {
                initialX = me.getSceneX();
                initialY = me.getSceneY();
            }
        });
        node.setOnMouseDragged(me -> {
            if (me.getButton() != MouseButton.MIDDLE) {
                node.getScene().getWindow().setX(me.getScreenX() - initialX);
                node.getScene().getWindow().setY(me.getScreenY() - initialY);
            }
        });
    }

    private String getCurrentLocalDateTimeStamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public final void onMessage(String fromLogin, String msgBody) {
        if (fromLogin.replaceAll("\\p{Punct}", "").equals(userID.getText())) {
            createMessage(msgBody, MessageType.RECEIVED);
        }
    }

    public final void resize() {
        txtMsg.setPrefSize( btnClose.getScene().getWindow().getWidth() - 220, 50);
        titleBar.setPrefWidth(rootPane.getScene().getWindow().getWidth()- 15);

    }

    public final void setUserID(String userId) {
        userID.setText(userId);
    }

    public void setUserImage(ImagePattern userImage) {
        this.userImage = userImage;
    }
}
