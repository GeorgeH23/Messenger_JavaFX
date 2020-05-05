package com.chatApplication.chatClient.gui.controller.services;

import com.chatApplication.chatClient.gui.controller.enums.ChatLoginResult;
import com.chatApplication.chatClient.gui.ChatManager;
import com.chatApplication.chatClient.gui.model.handlers.ImageHandler;
import com.chatApplication.chatClient.muc.ChatClient;
import com.chatApplication.dataModel.DataSource;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.IOException;

public class LoginService extends Service<ChatLoginResult> {

    private ChatManager chatManager;
    private ChatClient chatClient;
    private String userName;
    private String password;
    private DataSource dataSource;

    public LoginService(ChatManager chatManager, ChatClient chatClient, String userName, String password) {
        this.chatManager = chatManager;
        this.chatClient = chatClient;
        this.userName = userName;
        this.password = password;
        this.dataSource = new DataSource();
    }

    @Override
    protected Task<ChatLoginResult> createTask() {
        return new Task<>() {
            @Override
            protected ChatLoginResult call() {
                if (chatClient.connect()) {
                    String serverResponse;
                    try {
                        serverResponse = chatClient.login(userName, password);
                        if (serverResponse.equalsIgnoreCase("Login OK")) {
                            chatManager.addListeners();
                            dataSource.open();
                            dataSource.queryUserPicture(userName);
                            dataSource.close();
                            ImageHandler.getInstance().loadCurrentLoggedUserImage(userName);
                            chatManager.setLoggedUserLogin(chatClient.getThisClientLogin());
                            chatManager.setLoggedUserStatus(chatClient.getThisClientStatus());
                            return ChatLoginResult.SUCCESS;
                        } else if (serverResponse.equalsIgnoreCase("No such username")) {
                            return ChatLoginResult.FAILED_BY_USERNAME;
                        } else if (serverResponse.equalsIgnoreCase("Incorrect Password")) {
                            return ChatLoginResult.FAILED_BY_PASSWORD;
                        } else {
                            return ChatLoginResult.FAILED_BY_UNEXPECTED_ERROR;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        dataSource.close();
                    }
                }
                return ChatLoginResult.FAILED_BY_NETWORK;
            }
        };
    }
}
