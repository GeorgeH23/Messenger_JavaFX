package com.chatApplication.chatClient.gui.controller.services;

import com.chatApplication.chatClient.gui.controller.CreateAccountResult;
import com.chatApplication.common.NewUser;
import com.chatApplication.dataModel.DataSource;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class CreateAccountService extends Service<CreateAccountResult> {

    private NewUser newUser;
    private DataSource dataSource;

    public CreateAccountService(NewUser newUser) {
        this.newUser = newUser;
        this.dataSource = new DataSource();
    }

    @Override
    protected Task<CreateAccountResult> createTask() {
        return new Task<>() {
            @Override
            protected CreateAccountResult call() {
                dataSource.open();
                String response = dataSource.insertNewUser(newUser);
                dataSource.close();
                if (response.equals("New Account Created")) {
                    System.out.println("New Account Created");
                    return CreateAccountResult.SUCCESS;
                } else if (response.equals("Username exists")) {
                    System.out.println("Username exists");
                    return CreateAccountResult.FAILED_BY_USERNAME;
                } else {
                    return CreateAccountResult.FAILED_BY_UNEXPECTED_ERROR;
                }
            }
        };
    }
}
