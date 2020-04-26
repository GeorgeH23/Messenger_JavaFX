package com.chatApplication.chatClient.gui;

import com.chatApplication.chatClient.gui.controllers.MainWindowController;
import com.chatApplication.chatClient.gui.handlers.ImageHandler;
import com.chatApplication.chatClient.gui.utility.ChatUser;
import com.chatApplication.chatClient.muc.*;
import com.chatApplication.common.NewUser;
import com.chatApplication.common.PasswordHasher;
import com.chatApplication.dataModel.DataSource;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class ChatManager implements UserAvailabilityListener, UserStatusListener, MessageListener, PictureChangeListener {

    private static ChatManager instance;
    private final ChatClient chatClient;
    private DataSource dataSource;
    private PasswordHasher passwordHasher;
    private MainWindowController controller;
    private ObservableList<ChatUser> loggedClientsList;
    private String loggedUserLogin;
    private String loggedUserStatus;

    private ChatManager() {
        this.loggedClientsList = FXCollections.observableArrayList(chatUser -> new Observable[] {chatUser.getStatusImage(), chatUser.getUserImage()});
        this.chatClient = ChatClient.getInstance();
        this.dataSource = DataSource.getInstance();
        this.passwordHasher = PasswordHasher.getInstance();
    }

    public static ChatManager getInstance() {
        if (instance == null) {
            instance = new ChatManager();
        }
        return instance;
    }

    @Override
    public void onMessage(String fromLogin, String msgBody) {
        String processedMsg = msgBody.replaceAll("##NL##", "\n");
        String login = fromLogin.replaceAll("\\p{Punct}", "");

        controller.onMessage(login,processedMsg);
    }

    @Override
    public void onPictureChanged(String login) {
        String username = login.replaceAll("\\p{Punct}", "");
        String path = DataSource.getInstance().queryUserPicture(username);
        for (ChatUser user : loggedClientsList) {
            if (user.getLogin().equals(username)) {
                user.setUserImage(path);
            }
        }
    }

    @Override
    public void availabilityStatus(String login, String newStatus) {
        Platform.runLater(() -> {
            String username = login.replaceAll("\\p{Punct}", "");
            for (ChatUser user : loggedClientsList) {
                if (user.getLogin().equals(username)) {
                    user.setStatusImage(newStatus);
                    break;
                }
            }
        });
    }

    @Override
    public void online(String login, String userStatus) {
        Platform.runLater(() -> {
            String username = login.replaceAll("\\p{Punct}", "");
            String path = DataSource.getInstance().queryUserPicture(username);
            ChatUser chatUser = new ChatUser(username, path, userStatus);
            boolean isInList = false;
            for (ChatUser user : loggedClientsList) {
                if (user.getLogin().equals(chatUser.getLogin())) {
                    isInList = true;
                    break;
                }
            }
            if (!isInList) {
                loggedClientsList.add(chatUser);
                controller.online(login);
            }
        });
    }

    @Override
    public void offline(String login) {
        Platform.runLater(() -> {
            String username = login.replaceAll("\\p{Punct}", "");
            ChatUser user = null;
            for (ChatUser listUser : loggedClientsList) {
                if (listUser.getLogin().equals(username)) {
                    user = listUser;
                }
            }
            loggedClientsList.remove(user);
            controller.offline(login);
        });
    }

    public String attemptToLogin(String username, String password) {
        if (chatClient.connect()) {
            String serverResponse;
            try {
                serverResponse = chatClient.login(username, password);
                if (serverResponse.equalsIgnoreCase("Login OK")) {
                    addListeners();
                    this.loggedUserLogin = chatClient.getThisClientLogin();
                    this.loggedUserStatus = chatClient.getThisClientStatus();
                    ImageHandler.getInstance().loadCurrentLoggedUserImage(loggedUserLogin);
                    return "Login OK";
                } else if (serverResponse.equalsIgnoreCase("No such username")) {
                    return "No such username";
                } else if (serverResponse.equalsIgnoreCase("Incorrect Password")) {
                    return "Incorrect Password";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "Couldn't connect";
    }

    public boolean sendMessage(String sendTo, String message) {
        String processedMsg = message.replaceAll("\n", "##NL##");
        // Send the message only if it contains at leas one ASCII character.
        if (processedMsg.matches(".*\\w.*")) {
            try {
                chatClient.msg(sendTo, processedMsg);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public void logOffClient() {
        try {
            this.chatClient.logoff();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pictureChange(String clientName, String path) {
        dataSource.updateUserPicture(clientName, path);
        try {
            chatClient.pictureChange();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeLoggedUserStatus(String newStatus) {
        try {
            chatClient.availabilityChange(newStatus);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final void removeListeners() {
        chatClient.removeUserStatusListener(this);
        chatClient.removeMessageListener(this);
        chatClient.removeUserAvailabilityListener(this);
        chatClient.removePictureChangeListener(this);
    }

    public final void addListeners() {
        chatClient.addUserStatusListener(this);
        chatClient.addMessageListener(this);
        chatClient.addUserAvailabilityListener(this);
        chatClient.addPictureChangeListener(this);
    }

    public void closeDatabaseConnection() {
        dataSource.close();
    }

    public void openDatabaseConnection() {
        dataSource.open();
    }

    public String createNewAccount(String username, String password, String contactNumber, String picturePath) {
        try {
            password = PasswordHasher.getInstance().generateHash(password);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        NewUser newUser = new NewUser(username, password, contactNumber, picturePath);

        String response = dataSource.insertNewUser(newUser);

        if (response.equals("New Account Created")) {
            dataSource.queryUserPicture(username);
            System.out.println("New Account Created");

            return "Success";
        }
        if (response.equals("Username exists")) {
            System.out.println("Username exists");
            return "Username Exists";
        }
        return "Error";
    }

    public ObservableList<ChatUser> getLoggedClientsList() {
        return this.loggedClientsList;
    }

    public void setController(MainWindowController controller) {
        this.controller = controller;
    }

    public String getLoggedUserStatus() {
        return this.loggedUserStatus;
    }

    public String getLoggedUserLogin() {
        return this.loggedUserLogin;
    }
}
