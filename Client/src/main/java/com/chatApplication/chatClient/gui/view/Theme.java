package com.chatApplication.chatClient.gui.view;

public enum Theme {

    THEME_ONE,
    THEME_TWO,
    THEME_THREE;

    public static String getCssPath(Theme theme) {
        switch (theme) {
            case THEME_ONE:
                return "/utils/css/Default_";
            case THEME_TWO:
                return "/utils/css/ThemeTwo_";
            case THEME_THREE:
                return "/utils/css/ThemeThree_";
            default:
                return null;
        }
    }
}
