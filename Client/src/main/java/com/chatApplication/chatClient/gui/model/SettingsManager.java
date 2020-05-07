package com.chatApplication.chatClient.gui.model;

import com.chatApplication.chatClient.gui.view.Theme;
import java.io.*;

public class SettingsManager {

    private static final String FILE_NAME = "Settings.txt";
    private static final String PATH = System.getProperty("user.home") + File.separator + "Documents" +
            File.separator + "MessengerApplication" + File.separator;

    public static Theme loadPreferredTheme() {
        Theme theme = null;
        File file = new File(PATH + FILE_NAME);
        if (!file.isFile()) {
            return Theme.THEME_ONE;
        } else {
            try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    String[] tokens = line.split(" ");
                    if (tokens.length == 3) {
                        switch (tokens[2]) {
                            case "THEME_TWO":
                                theme = Theme.THEME_TWO;
                                break;
                            case "THEME_THREE":
                                theme = Theme.THEME_THREE;
                                break;
                            default:
                                theme = Theme.THEME_ONE;
                                break;
                        }
                    } else {
                        return Theme.THEME_ONE;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return theme;
    }

    public static void savePreferredTheme(Theme theme) {
        String themeToSave = "";
        File file = new File(PATH + FILE_NAME);
        switch (theme) {
            case THEME_ONE:
                themeToSave = "THEME_ONE";
                break;
            case THEME_TWO:
                themeToSave = "THEME_TWO";
                break;
            case THEME_THREE:
                themeToSave = "THEME_THREE";
                break;
        }
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("Theme = " + themeToSave);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
