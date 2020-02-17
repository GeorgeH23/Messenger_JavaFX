package com.chatApplication.chatClient.muc;

public interface MessageListener {

    public void onMessage(String fromLogin, String msgBody);

}
