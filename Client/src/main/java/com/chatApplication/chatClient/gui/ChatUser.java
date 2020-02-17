package com.chatApplication.chatClient.gui;

public class ChatUser {

    private final String login;
    private final String picturePath;
    private String statusLightPath;


    public ChatUser(final String login, final String picturePath, String userStatus) {
        this.login = login;
        this.picturePath = picturePath;
        System.out.println(this.picturePath);
        setColor(userStatus);
    }

    public String getLogin() {
        return login;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public String getStatusLightPath() {
        return this.statusLightPath;
    }

    public void setColor(String newStatus) {
        switch (newStatus) {
            case "available" :
                this.statusLightPath = "/utils/images/icons/ok.png";
                break;
            case "busy" :
                this.statusLightPath = "/utils/images/icons/busy.png";
                break;
            case "away" :
                this.statusLightPath = "/utils/images/icons/away.png";
                break;
            case "dnd" :
                this.statusLightPath = "/utils/images/icons/dnd.png";
                break;
            default:
                this.statusLightPath = "/utils/images/icons/unknown.png";
                break;
        }
    }

}
