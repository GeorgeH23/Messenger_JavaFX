package com.chatApplication.chatClient.gui;

import com.chatApplication.chatClient.gui.controller.MainWindowController;
import com.chatApplication.chatClient.gui.controller.services.*;
import com.chatApplication.chatClient.gui.model.ChatUser;
import com.chatApplication.chatClient.gui.model.SettingsManager;
import com.chatApplication.chatClient.gui.view.Theme;
import com.chatApplication.chatClient.muc.*;
import com.chatApplication.common.NewUser;
import com.chatApplication.common.PasswordHasher;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class ChatManager implements UserAvailabilityListener, UserStatusListener, MessageListener, PictureChangeListener {

    private static ChatManager instance;
    private final ChatClient chatClient;
    private PasswordHasher passwordHasher;
    private MainWindowController controller;
    private ObservableList<ChatUser> loggedClientsList;
    private String loggedUserLogin;
    private String loggedUserStatus;
    private Theme theme;

    private ChatManager() {
        this.loggedClientsList = FXCollections.observableArrayList(chatUser -> new Observable[] {chatUser.getStatusImage(), chatUser.getUserImage()});
        this.chatClient = ChatClient.getInstance();
        this.passwordHasher = PasswordHasher.getInstance();
        this.theme = SettingsManager.loadPreferredTheme();
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
        Platform.runLater(() -> {
            String username = login.replaceAll("\\p{Punct}", "");
            OnPictureChangedService service = new OnPictureChangedService(username);
            service.start();
            service.setOnSucceeded(event -> {
                String path = service.getValue();
                for (ChatUser user : loggedClientsList) {
                    if (user.getLogin().equals(username)) {
                        user.setUserImage(path);
                    }
                }
            });
        });
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
            OnlineService onlineService = new OnlineService(username);
            onlineService.start();
            onlineService.setOnSucceeded(event -> {
                for (ChatUser chatUser : loggedClientsList) {
                    if (chatUser.getLogin().equals(username)) {
                        String path = onlineService.getValue();
                        chatUser.setUserImage(path);
                        break;
                    }
                }
            });

            ChatUser chatUser = new ChatUser(username, "", userStatus);
            boolean isInList = false;
            for (ChatUser user : loggedClientsList) {
                if (user.getLogin().equals(chatUser.getLogin())) {
                    isInList = true;
                    break;
                }
            }
            if (!isInList) {
                loggedClientsList.add(chatUser);
            }
            controller.online(login);
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

    public LoginService attemptToLogin(String username, String password) {
        LoginService loginService = new LoginService(this, chatClient, username, password);
        loginService.start();
        return loginService;
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

    public PictureChangeService pictureChange(String clientName, String path) {
        PictureChangeService service = new PictureChangeService(chatClient, clientName, path);
        service.start();
        return service;
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

    public CreateAccountService createNewAccount(String username, String password, String contactNumber, String picturePath) {
        try {
            password = passwordHasher.generateHash(password);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        NewUser newUser = new NewUser(username, password, contactNumber, picturePath);
        CreateAccountService createAccountService = new CreateAccountService(newUser);
        createAccountService.start();

        return createAccountService;
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

    public void setLoggedUserLogin(String loggedUserLogin) {
        this.loggedUserLogin = loggedUserLogin;
    }

    public void setLoggedUserStatus(String loggedUserStatus) {
        this.loggedUserStatus = loggedUserStatus;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }
}
