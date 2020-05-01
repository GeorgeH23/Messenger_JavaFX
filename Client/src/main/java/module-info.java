module Client {
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.swing;
    requires sqlite.jdbc;
    requires java.desktop;
    requires org.apache.commons.lang3;
    requires javafx.media;
    requires tratTester;
    requires Common;

    opens com.chatApplication.chatClient.gui;
    opens com.chatApplication.chatClient.gui.controller;
    opens com.chatApplication.chatClient.gui.model.handlers;
    opens com.chatApplication.chatClient.gui.model.utility;
    opens com.chatApplication.chatClient.gui.controller.services;
    opens com.chatApplication.chatClient.gui.view;
    opens com.chatApplication.chatClient.muc;

    exports com.chatApplication.chatClient.gui;
    exports com.chatApplication.chatClient.gui.controller;
    exports com.chatApplication.chatClient.gui.model.handlers;
    exports com.chatApplication.chatClient.gui.model.utility;
    exports com.chatApplication.chatClient.gui.controller.services;
    exports com.chatApplication.chatClient.gui.view;
    exports com.chatApplication.chatClient.muc;

}