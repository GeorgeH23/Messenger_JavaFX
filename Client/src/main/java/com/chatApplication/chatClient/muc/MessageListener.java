package com.chatApplication.chatClient.muc;

public interface MessageListener {

    void onMessage(String fromLogin, String msgBody);

}
