package com.chatApplication.chatClient.muc;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatClient {

    private static final String SERVER_NAME = "localHost";
    private static final int SERVER_PORT = 8818;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferedIn;
    private String thisClientLogin;
    private String thisClientStatus;

    private final List<UserStatusListener> userStatusListeners = new ArrayList<>();
    private final List<MessageListener> messageListeners = new ArrayList<>();
    private final List<UserAvailabilityListener> userAvailabilityListeners = new ArrayList<>();
    private final List<PictureChangeListener> pictureChangeListeners = new ArrayList<>();

    private static ChatClient instance;

    private ChatClient() {
    }

    public static ChatClient getInstance() {
        if (instance == null) {
            instance = new ChatClient();
        }
        return instance;
    }

    public void msg(String sendTo, String msgBody) throws IOException {
        String cmd = "msg " + sendTo + " " + msgBody + "\n";
        serverOut.write(cmd.getBytes());
    }

    public void logoff() throws IOException {
        String cmd = "logoff\n";
        if (serverOut != null) {
            serverOut.write(cmd.getBytes());
        }
    }

    public void availabilityChange(String newStatus) throws IOException {
        String cmd = "sts " + newStatus + "\n";
        if (serverOut != null) {
            serverOut.write(cmd.getBytes());
        }
    }

    public void pictureChange() throws IOException {
        String cmd = "picture\n";
        if (serverOut != null) {
            serverOut.write(cmd.getBytes());
        }
    }

    public String login(String login, String password) throws IOException {
        String cmd = "login " + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response Line: " + response);

        String[] tokens = response.split(" ");
        String confirmation = tokens[0];
        String status = tokens[1];

        if ("LoginOK".equalsIgnoreCase(confirmation)) {
            startMessageReader();
            thisClientLogin = login;
            thisClientStatus = status;
            return "Login OK";
        } else if ("No Such Username".equalsIgnoreCase(response)) {
            return "No Such Username";
        } else if ("Incorrect Password".equalsIgnoreCase(response)) {
            return "Incorrect Password";
        } else if ("User already connected".equalsIgnoreCase(response)) {
            disconnect();
        }

        return "OOOOPS";
    }

    private void startMessageReader() {
        new Thread(this::readMessageLoop).start();
    }

    private void readMessageLoop() {
        try {
            String line;
            while ((line = bufferedIn.readLine()) != null) {
                if (line.equalsIgnoreCase("alreadyIn")){
                    socket.close();
                    return;
                }
                String[] tokens = line.split(" ");
                //String[] tokens = StringUtils.split(line);
                if (tokens.length > 0) {
                    String cmd = tokens[0];
                    cmd = cmd.replaceAll("\\p{Punct}", "");
                    if ("online".equalsIgnoreCase(cmd)) {
                        handleOnline(tokens);
                    } else if ("offline".equalsIgnoreCase(cmd)) {
                        handleOffline(tokens);
                    } else if ("availability".equalsIgnoreCase(cmd)) {
                        handleAvailability(tokens);
                    } else if ("msg".equalsIgnoreCase(cmd)) {
                        String[] tokensMsg = line.split(" ", 3);
                        //String[] tokensMsg = StringUtils.split(line, null, 3);
                        handleMessage(tokensMsg);
                    } else if ("PictureUpdate".equalsIgnoreCase(cmd)) {
                        handlePictureChange(tokens);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void handleMessage(String[] tokensMsg) {
        String login = tokensMsg[1];
        String msgBody = tokensMsg[2];

        for (MessageListener listener : messageListeners) {
            listener.onMessage(login, msgBody);
        }
    }

    private void handleAvailability(String[] tokens) {
        String login = tokens[1];
        String newStatus = tokens[2];

        for (UserAvailabilityListener listener : userAvailabilityListeners) {
            listener.availabilityStatus(login, newStatus);
        }
    }

    private void handlePictureChange(String[] tokens) {
        String login = tokens[1];
        for (PictureChangeListener listener : pictureChangeListeners) {
            listener.onPictureChanged(login);
        }
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for (UserStatusListener listener : userStatusListeners) {
            listener.offline(login);
        }
    }

    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        String userStatus = tokens[2];
        for (UserStatusListener listener : userStatusListeners) {
            listener.online(login, userStatus);
        }
    }

    public boolean connect() {
        try {
            if (!openSocket()) {
                return false;
            }
            System.out.println("Client port is " + socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean openSocket() {
        try {
            this.socket = new Socket(SERVER_NAME, SERVER_PORT);
        } catch (Exception e){
            System.out.println("Cannot connect to the server!");
            return false;
        }
        return true;
    }

    private boolean disconnect(){
        try {
            this.bufferedIn.close();
            this.serverIn.close();
            this.serverOut.close();
            this.socket.close();

            System.out.println("Disconnected");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getThisClientLogin() {
        return thisClientLogin;
    }

    public String getThisClientStatus() {
        return thisClientStatus;
    }

    public void addUserStatusListener(UserStatusListener listener) {
        userStatusListeners.add(listener);
    }

    public void removeUserStatusListener(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }

    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    public void addUserAvailabilityListener(UserAvailabilityListener listener) {
        userAvailabilityListeners.add(listener);
    }

    public void removeUserAvailabilityListener(UserAvailabilityListener listener) {
        userAvailabilityListeners.remove(listener);
    }

    public void addPictureChangeListener(PictureChangeListener listener) {
        pictureChangeListeners.add(listener);
    }

    public void removePictureChangeListener(PictureChangeListener listener) {
        pictureChangeListeners.remove(listener);
    }
}
