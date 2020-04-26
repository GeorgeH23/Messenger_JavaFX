package com.chatApplication.chatClient.gui.utility;

import com.chatApplication.chatClient.gui.controllers.UserListViewCellController;
import javafx.scene.Node;
import javafx.scene.control.ListCell;

public class UserListViewCell extends ListCell<ChatUser> {

    /*@FXML
    private HBox hBox;
    @FXML
    private Circle pictureCircle;
    @FXML
    private Circle statusCircle;*/

    private final UserListViewCellController controller = new UserListViewCellController();
    private final Node view = controller.getBox();

    @Override
    protected void updateItem(ChatUser chatUser, boolean empty) {
        super.updateItem(chatUser, empty);

        if (empty || chatUser == null) {
            setText(null);
            setGraphic(null);
        } else {

            /*FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/userListViewCellView.fxml"));
            loader.setController(this);
            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            setText(chatUser.getLogin());
            controller.setUserImage(chatUser.getUserImage().getValue());
            controller.setStatusImage(chatUser.getStatusImage().getValue());
            setGraphic(view);

            /*setText(chatUser.getLogin());
            System.out.println(chatUser.getUserImage().getValue());
            pictureCircle.setFill(new ImagePattern(chatUser.getUserImage().getValue()));
            statusCircle.setFill(chatUser.getStatusImage().getValue());
            setGraphic(hBox);*/
        }
    }
}
