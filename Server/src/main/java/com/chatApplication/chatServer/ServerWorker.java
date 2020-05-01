package com.chatApplication.chatServer;

import com.chatApplication.dataModel.DataSource;
import com.chatApplication.common.PasswordHasher;
import com.chatApplication.gui.Main;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private String login;
    private OutputStream outputStream;
    private HashSet<String> topicSet;
    private static int loginCount = 0;
    private boolean isLoggedIn;
    private Map<String, String> userStatus;
    private final DataSource dataSource;

    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
        this.login = null;
        this.isLoggedIn = false;
        this.topicSet = new HashSet<>();
        this.userStatus = server.getWorkerStatusList();
        this.dataSource = new DataSource();
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while (((line = reader.readLine()) != null) && clientSocket.isConnected()) {
                // Split the line into individual tokens with help of Apache Comm3.9 Lib
                // splits the line in multiple tokens based on the whitespace character

                String[] tokens = StringUtils.split(line);
                if (tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    if ("logoff".equals(cmd) || "quit".equalsIgnoreCase(cmd)) {
                        handleLogoff();
                        break;
                    } else if ("sts".equals(cmd)) {
                        String[] tokensMsg = StringUtils.split(line, null, 3);
                        handleAvailabilityChange(tokensMsg);
                    } else if ("login".equalsIgnoreCase(cmd)) {

                        List<ServerWorker> workerList = server.getWorkerList();
                        boolean equals = false;

                        for (ServerWorker worker : workerList) {
                            String login = worker.getLogin();
                            if (login != null) {
                                if (tokens[1].equals(login)) {
                                    equals = true;
                                    System.out.println("YOURE INNNNN");
                                    appendText("YOURE INNNNN");
                                }
                            }
                        }

                        if (equals) {
                            String msg = "User already connected";
                            outputStream.write(msg.getBytes());
                            server.removeWorker(this);
                            reader.close();
                            inputStream.close();
                            outputStream.close();
                            clientSocket.close();
                            break;
                        } else {
                            if (!handleLogin(outputStream, tokens)) {
                                break;
                            }
                        }

                    } else if ("msg".equalsIgnoreCase(cmd)) {
                        String[] tokensMsg = StringUtils.split(line, null, 3);
                        handleMessage(tokensMsg);
                    } else if ("join".equalsIgnoreCase(cmd)) {
                        handleJoin(tokens);
                    } else if ("leave".equalsIgnoreCase(cmd)) {
                        handleLeave(tokens);
                    } else if ("picture".equalsIgnoreCase(cmd)) {
                        handlePictureChange(tokens);
                    } else {
                        String msg = "Unknown command: \"" + cmd + "\"\n";
                        outputStream.write(msg.getBytes());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Connection was reset by the client.");
            appendText("Connection was reset by the client.");
        }
        if (this.login != null) {
            System.out.println("Connection lost with: \"" + getLogin() + "\" or user logged off.");
            appendText("Connection lost with: \"" + getLogin() + "\" or user logged off.");
            handleLogoff();
        } else {
            System.out.println("Connection lost for the client.");
            appendText("Connection lost for the client.");
            server.removeWorker(this);
        }
        clientSocket.close();
    }

    private void handleLeave(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }

    public boolean isMemberOfTopic(String topic) {
        return topicSet.contains(topic);
    }

    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            // by adding this topic to the topicSet we are saying that this connection is part of this topic
            topicSet.add(topic);
        }
    }

    // format: "msg" "login" body...
    // format: "msg" "#topic" body...
    private void handleMessage(String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String sendTo = tokens[1];
            String body = tokens[2];

            boolean isTopic = sendTo.charAt(0) == '#';

            List<ServerWorker> workerList = server.getWorkerList();
            for (ServerWorker worker : workerList) {
                if (isTopic) {
                    if (worker.isMemberOfTopic(sendTo)) {
                        String outMsg = "msg " + sendTo + ":\"" + login + "\" " + body + "\n";
                        worker.send(outMsg);
                    }
                } else if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                    String outMsg = "msg \"" + login + "\" " + body + "\n";
                    worker.send(outMsg);
                }
            }
        } else {
            System.out.println("Incorrect message format. Check tokens length.");
            appendText("Incorrect message format. Check tokens length.");
        }
    }

    private void handleLogoff() throws IOException {
        if (getIsLoggedIn()) {
            server.removeWorker(this);
            String message;
            List<ServerWorker> workerList = server.getWorkerList();
            // Send other online users current user's status
            for (ServerWorker worker : workerList) {
                if (worker.getLogin() != null) {
                    if (!login.equals(worker.getLogin())) {
                        message = "Offline: \"" + login + "\"\n";
                        worker.send(message);
                    }
                }
            }
            clientSocket.close();
            loginCount--;
            isLoggedIn = false;
        } else {
            System.out.println("The client is not logged in.");
            appendText("The client is not logged in.");
        }
    }

    private void handleAvailabilityChange(String[] tokens) throws IOException {
        if (tokens.length == 2) {
            String newStatus = tokens[1];
            if (getIsLoggedIn()) {
                userStatus.replace(login, newStatus);
                String message;
                List<ServerWorker> workerList = server.getWorkerList();
                for (ServerWorker worker : workerList) {
                    if (worker.getLogin() != null) {
                        if (!login.equals(worker.getLogin())) {
                            message = "Availability: \"" + login + "\" " + newStatus + "\n";
                            worker.send(message);
                        }
                    }
                }
            }
        }
    }

    private void handlePictureChange(String[] tokens) throws IOException {
        String message;
        List<ServerWorker> workerList = server.getWorkerList();
        for (ServerWorker worker : workerList) {
            if (worker.getLogin() != null) {
                if (!login.equals(worker.getLogin())) {
                    message = "PictureUpdate: \"" + login + "\"\n";
                    worker.send(message);
                }
            }
        }
    }

    private boolean handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];

            String msg;
            String dbMessage = checkLoginDetails(login, password);
            dataSource.close();

            switch (dbMessage) {
                case "Credentials OK":
                    isLoggedIn = true;
                    this.login = login;
                    loginCount++;

                    if (!userStatus.containsKey(login)) {
                        userStatus.put(login, "available");
                    }

                    msg = "LoginOK " + userStatus.get(login) + "\n";
                    outputStream.write(msg.getBytes());

                    System.out.println("User \"" + login + "\" logged in successfully.");
                    appendText("User \"" + login + "\" logged in successfully.");

                    // When a user logs in the other online users will get a notification
                    String message;
                    List<ServerWorker> workerList = server.getWorkerList();
                    // Send other online users current user's status
                    for (ServerWorker worker : workerList) {
                        if (!login.equals(worker.getLogin())) {
                            message = "Online: \"" + login + "\" " + userStatus.get(login) + "\n";
                            worker.send(message);
                        } else {
                            if (ServerWorker.loginCount == 1) {
                                message = "0 users  are online.\n";
                                worker.send(message);
                            } else {
                                // Send current user all other online logins
                                for (ServerWorker onlineWorker : workerList) {
                                    if (onlineWorker.getLogin() != null) {
                                        if (!login.equals(onlineWorker.getLogin())) {
                                            message = "Online: \"" + onlineWorker.getLogin() + "\" " + userStatus.get(onlineWorker.getLogin()) + "\n";
                                            System.out.println(message);
                                            appendText(message);
                                            worker.send(message);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return true;
                case "Incorrect Password":
                    msg = "Incorrect Password\n";
                    outputStream.write(msg.getBytes());
                    System.err.println("Incorrect Password for " + login + "!");
                    appendText("Incorrect Password for " + login + "!");
                    return false;
                case "No Such Username":
                    msg = "No Such Username\n";
                    outputStream.write(msg.getBytes());
                    System.err.println("No Such Username: " + login + "!");
                    appendText("No Such Username: " + login + "!");
                    return false;
                case "Database Connection Error":
                    msg = "Database Connection Error\n";
                    outputStream.write(msg.getBytes());
                    System.err.println("Database Connection Error!");
                    appendText("Database Connection Error!");
                    return false;
                case "Hashing Error":
                    msg = "Hashing Error\n";
                    outputStream.write(msg.getBytes());
                    System.err.println("Hashing Error!");
                    appendText("Hashing Error!");
                    return false;
                default:
                    msg = "Something Went Wrong\n\n";
                    outputStream.write(msg.getBytes());
                    System.err.println("Something Went Wrong\n");
                    appendText("Something Went Wrong\n");
                    return false;
            }
        }
        return false;
    }

    private String checkLoginDetails(String username, String password) {
        try {
            if (dataSource.open()) {
                Map<String, String> dbResults = dataSource.checkLogin(username);
                String hashedPassword = "";
                if (dbResults != null) {
                    hashedPassword = PasswordHasher.getInstance().generateHash(password);

                    if (username.equals(dbResults.get("username")) && hashedPassword.equals(dbResults.get("password"))) {
                        return "Credentials OK";
                    } else if (username.equals(dbResults.get("username")) && !hashedPassword.equals(dbResults.get("password"))) {
                        return "Incorrect Password";
                    }
                } else {
                    return "No Such Username";
                }
            } else {
                return "Database Connection Error";
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "Hashing Error";
        }
        return "Something Went Wrong";
    }

    private void send(String msg) throws IOException {
        if (this.login != null) {
            this.outputStream.write(msg.getBytes());
        }
    }

    private String getLogin() {
        return this.login;
    }

    private boolean getIsLoggedIn() {
        return this.isLoggedIn;
    }

    private void appendText(String text) {
        String date = getCurrentLocalDateTimeStamp();
        StringBuilder sb = new StringBuilder();
        sb.append("- ");
        sb.append(date);
        sb.append(":\n\t");
        sb.append(text);
        sb.append("\n");
        Platform.runLater(() -> {
            Main.getController().getTextArea().appendText(sb.toString());
        });
    }

    private String getCurrentLocalDateTimeStamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
