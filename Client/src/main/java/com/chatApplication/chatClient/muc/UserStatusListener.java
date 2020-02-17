package com.chatApplication.chatClient.muc;

public interface UserStatusListener {

    public void online(String login, String userStatus);

    public void offline(String login);
}
