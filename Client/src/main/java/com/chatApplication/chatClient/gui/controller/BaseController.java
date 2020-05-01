package com.chatApplication.chatClient.gui.controller;

import com.chatApplication.chatClient.gui.ChatManager;
import com.chatApplication.chatClient.gui.view.ViewFactory;

public abstract class BaseController {

    protected ViewFactory viewFactory;
    protected ChatManager chatManager;

    public BaseController () {
        this.viewFactory = ViewFactory.getInstance();
        this.chatManager = ChatManager.getInstance();
    }
    
}
