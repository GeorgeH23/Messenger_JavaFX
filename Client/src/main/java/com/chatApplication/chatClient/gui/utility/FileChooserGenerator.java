package com.chatApplication.chatClient.gui.utility;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

public class FileChooserGenerator {

    public static File showOpenFile(Window window){
        FileChooser fileChooser=new FileChooser();
        fileChooser.setTitle("Profile Photo");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG (*.jpg)", "*.jpg"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG (*.png)", "*.png"));
        return fileChooser.showOpenDialog(window);
    }
}
