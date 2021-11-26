package com.chatApplication.chatClient.muc;

public interface UserStatusListener {

    void online(String login, String userStatus);

    void offline(String login);
}
