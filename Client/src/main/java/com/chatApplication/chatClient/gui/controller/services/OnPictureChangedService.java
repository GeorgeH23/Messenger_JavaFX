package com.chatApplication.chatClient.gui.controller.services;

import com.chatApplication.dataModel.DataSource;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class OnPictureChangedService extends Service<String> {

    private DataSource dataSource;
    private String userName;

    public OnPictureChangedService(String userName) {
        this.dataSource = new DataSource();
        this.userName = userName;
    }

    @Override
    protected Task<String> createTask() {
        return new Task<>() {
            @Override
            protected String call() throws Exception {
                String path = "";
                dataSource.open();
                path = dataSource.queryUserPicture(userName);
                dataSource.close();

                return path;
            }
        };
    }
}
