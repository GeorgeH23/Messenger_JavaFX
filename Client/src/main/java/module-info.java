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
    opens com.chatApplication.chatClient.gui.controllers;
    opens com.chatApplication.chatClient.gui.handlers;
    opens com.chatApplication.chatClient.gui.utility;

    exports com.chatApplication.chatClient.gui;
    exports com.chatApplication.chatClient.gui.controllers;
    exports com.chatApplication.chatClient.gui.handlers;
    exports com.chatApplication.chatClient.gui.utility;
}