package com.chatApplication.chatClient.gui.controller.services;

import com.chatApplication.chatClient.muc.ChatClient;
import com.chatApplication.dataModel.DataSource;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class PictureChangeService extends Service<Void> {

    private DataSource dataSource;
    private ChatClient chatClient;
    private String clientName;
    private String path;

    public PictureChangeService(ChatClient chatClient, String clientName, String path) {
        this.dataSource = new DataSource();
        this.chatClient = chatClient;
        this.clientName = clientName;
        this.path = path;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                dataSource.open();
                dataSource.updateUserPicture(clientName, path);
                dataSource.close();

                chatClient.pictureChange();
                return null;
            }
        };
    }
}
