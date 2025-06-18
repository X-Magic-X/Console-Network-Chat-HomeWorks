package ru.otus.chat.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private int port;
    private List<ClientHandler> clients;

    public Server(int port) {
        this.port = port;
        clients = new CopyOnWriteArrayList<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                subscribe(new ClientHandler(socket, this));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler) {
        broadcastMessage("Клиент " + clientHandler.getUsername() + " вышел из чата");
        System.out.println("Клиент " + clientHandler.getUsername() + " вышел из чата");
        clients.remove(clientHandler);
    }

    public void broadcastMessage(String message) {
        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
    }

    public ClientHandler findClientByUsername(String username) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(username)) {
                return client;
            }
        }
        return null;
    }
}
