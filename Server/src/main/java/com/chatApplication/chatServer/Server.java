package com.chatApplication.chatServer;

import com.chatApplication.gui.Main;
import javafx.application.Platform;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* This class is implemented in order to be able to send messages from one connection
to the other connections.
 */
public class Server extends Thread {

    private final int serverPort;
    private final List<ServerWorker> workerList = new ArrayList<>();
    private final Map<String, String> workerStatusList = new HashMap<>();

    public Server(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while (true) {
                System.out.println("About to accept client connection...");
                appendText("About to accept client connection...");
                // connection to the client
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);
                appendText("Accepted connection from " + clientSocket);
                // create a new thread for each client
                ServerWorker serverWorker = new ServerWorker(this, clientSocket);
                workerList.add(serverWorker);
                serverWorker.start();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public List<ServerWorker> getWorkerList() {
        return this.workerList;
    }

    public void removeWorker(ServerWorker serverWorker) {
        workerList.remove(serverWorker);
    }

    public Map<String, String> getWorkerStatusList() {
        return workerStatusList;
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
