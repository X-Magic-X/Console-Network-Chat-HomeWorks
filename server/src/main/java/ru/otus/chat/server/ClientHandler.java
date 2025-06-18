package ru.otus.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private Server server;
    private DataInputStream in;
    private DataOutputStream out;

    private String username;

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        username = "user" + socket.getPort();
        sendMsg("Вы подключились под ником: " + username);

        new Thread(() -> {
            try {
                System.out.println("Клиент подключился " + socket.getPort());

                while (true) {
                    String message = in.readUTF();
                    if (message.startsWith("/")) {
                        if (message.equals("/exit")) {
                            sendMsg("/exitok");
                            break;
                        } else if (message.startsWith("/w")) {
                            sendPrivateMsg(message);
                        }
                    } else {
                        server.broadcastMessage(username + ": " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    public void sendMsg(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPrivateMsg(String message) {
        String[] privateMessage = message.split(" ", 3);
        if (privateMessage.length < 3) {
            sendMsg("Используйте: /w <username> <message>");
        } else if (privateMessage[1].equals(username)) {
            sendMsg("Вы не можете писать себе");
        } else {
            ClientHandler client = server.findClientByUsername(privateMessage[1]);
            if (client != null) {
                sendMsg("я -> " + privateMessage[1] + ": " + privateMessage[2]);
                client.sendMsg(username + " -> я: " + privateMessage[2]);
            } else {
                sendMsg("Пользователь " + privateMessage[1] + " не найден");
            }
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void disconnect() {
        server.unsubscribe(this);
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
