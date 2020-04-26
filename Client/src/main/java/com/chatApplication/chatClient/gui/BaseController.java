package com.chatApplication.chatClient.gui;

public abstract class BaseController {

    protected ViewFactory viewFactory;
    protected ChatManager chatManager;

    public BaseController () {
        this.viewFactory = ViewFactory.getInstance();
        this.chatManager = ChatManager.getInstance();
    }
    
}
