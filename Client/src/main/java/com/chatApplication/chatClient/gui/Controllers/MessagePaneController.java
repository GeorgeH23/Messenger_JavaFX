package com.chatApplication.chatClient.gui.Controllers;

import com.chatApplication.chatClient.gui.AudioHandler;
import com.chatApplication.chatClient.gui.MessageType;
import com.chatApplication.chatClient.muc.ChatClient;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MessagePaneController {

    private static final ChatClient CHAT_CLIENT = ChatClient.getInstance();
    private String myself = ChatClient.getInstance().getThisClientLogin();
    private double initialX;
    private double initialY;
    private AudioHandler audio;

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
    private Button btnSend;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox chatBox;
    @FXML
    private Button btnMinimize;

    @FXML
    private void initialize() {

        audio = AudioHandler.getInstance();
        audio.load("utils/sounds/sent.wav", "sent");

        // Automatically scroll down to the last message in the conversation.
        chatBox.heightProperty().addListener(
                (observable, oldValue, newValue) -> {
                    chatBox.layout();
                    scrollPane.setVvalue( 1.0d );
                }
        );

    }

    public void inititializePane(String paneTitle){
        addDraggableNode(titleBar);
        userID.setText(paneTitle.replaceAll("\\p{Punct}", ""));
        txtMsg.deselect();
        btnSend.requestFocus();
    }

    public void resize(){
        txtMsg.setPrefSize( btnClose.getScene().getWindow().getWidth() - 220, 50);
        titleBar.setPrefWidth(rootPane.getScene().getWindow().getWidth()- 15);

    }

    @FXML
    public void closeAction() {
        //Runtime.getRuntime().exit(0);
        ((Stage)btnClose.getScene().getWindow()).hide();
    }

    @FXML
    public void minimizeAction() {
        Stage stage = (Stage) btnMinimize.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    public void sendAction(){

        String sendTo = userID.getText();
        String message = txtMsg.getText().trim();

        String processedMsg = message.replaceAll("\n", "##NL##");

        // Send the message only if it contains at leas one ASCII character.
        if (processedMsg.matches(".*\\w.*")) {
            try {
                CHAT_CLIENT.msg(sendTo, processedMsg);
            } catch (IOException e) {
                e.printStackTrace();
            }

            createMessage(message, MessageType.SENT);
            audio.play("sent", 0);

            txtMsg.clear();
        }
    }

    // This function is used for moving the MessagePanes on the screen.
    private void addDraggableNode(final Node node) {

        node.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {

                if (me.getButton() != MouseButton.MIDDLE) {
                    initialX = me.getSceneX();
                    initialY = me.getSceneY();
                }
            }
        });

        node.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                if (me.getButton() != MouseButton.MIDDLE) {
                    node.getScene().getWindow().setX(me.getScreenX() - initialX);
                    node.getScene().getWindow().setY(me.getScreenY() - initialY);
                }
            }
        });
    }

    public void onMessage(String fromLogin, String msgBody) {

        if (fromLogin.replaceAll("\\p{Punct}", "").equals(userID.getText())) {

            createMessage(msgBody, MessageType.RECEIVED);

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
                File file = new File(System.getProperty("user.home") + File.separator + "Documents" + File.separator +
                        "MessengerApplication" + File.separator + myself + ".jpg");
                if (file.isFile()) {
                    img.setFill(new ImagePattern(new Image(file.toURI().toURL().toString())));
                } else {
                    String filePath = new File(getClass().getClassLoader().getResource("utils/images/users/user.png").getFile()).toURI().toString();
                    img.setFill(new ImagePattern(new Image(filePath)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (messageType.equals(MessageType.RECEIVED)) {
            txtName = new Text(getCurrentLocalDateTimeStamp() + "\n" + userID.getText() + "\n");

            try {
                File file = new File(System.getProperty("user.home") + File.separator + "Documents" + File.separator +
                        "MessengerApplication" + File.separator + userID.getText() + ".jpg");
                if (file.isFile()) {
                    img.setFill(new ImagePattern(new Image(file.toURI().toURL().toString())));
                } else {
                    String filePath = new File(getClass().getClassLoader().getResource("utils/images/users/user.png").getFile()).toURI().toString();
                    img.setFill(new ImagePattern(new Image(filePath)));
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

    void setUserID(String userId){
        userID.setText(userId);
    }

    private String getCurrentLocalDateTimeStamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
