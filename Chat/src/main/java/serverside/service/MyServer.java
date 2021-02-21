package serverside.service;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serverside.interfaces.AuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class MyServer {
    private final int PORT = 8082;
    private final Logger log = LoggerFactory.getLogger(MyServer.class);

    private List<ClientHandler> clients;

    @Getter
    private AuthService authService;

    public MyServer() {
        try (ServerSocket server = new ServerSocket(PORT)) {
            log.info("Server started on portn {}.", PORT);
            authService = new BaseAuthService();
            authService.start();

            clients = new ArrayList<>();

            while (true) {
                log.info("Server waiting for client connection");
                Socket socket = server.accept();
                log.info("Client connected");
                new ClientHandler(this, socket);
            }


        } catch (IOException e) {
            log.warn("Server was destroyed by earthquake.");
            e.printStackTrace();
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }


    public synchronized void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMessage(message);
        }
    }

    public synchronized void subscribe(ClientHandler client) {
        clients.add(client);
    }

    public synchronized void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }

    public boolean isNickBusy(String localNick) {
        for (ClientHandler client : clients) {
            if (client.getNickname().equals(localNick)) {
                return true;
            }
        }
        return false;
    }

    public String sendMessageToUserByNickname(String messageRecipientNickname,
                                               String messageText) {
        log.info("sendMessageToUserByNickName: {} : {}", messageRecipientNickname, messageText);
        for (ClientHandler client : clients) {
            if (client.getNickname().equals(messageRecipientNickname)) {
                log.info("messageResipient {}", client);
                log.info("message resipient name {}", client.getNickname());
                client.sendMessage(messageText);
            }
        }
        return "";
//
//        log.info("message resipient name {}", messageRecipientOptional.get().getNickname());
//         if (messageRecipientOptional.get() == null){
//             return "Nick " + messageRecipientNickname +" is not exist in chat.";
//         }else{
//             var messageRecipient = messageRecipientOptional.get();
//             messageRecipient.sendMessage(messageText);
//             return "Message was send to " +messageRecipientNickname;
//         }
    }
}
