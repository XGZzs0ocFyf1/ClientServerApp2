package serverside.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;


//client handler class : works with chat clients
public class ClientHandler {

    private final static Logger log = LoggerFactory.getLogger(ClientHandler.class);

    private Socket socket;
    private MyServer myServer;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String nickname;


    private long timeToLogoutAnonimus = 2 * 60 * 1000;
    private long timeToLogoutAuthenticated = 3 * 60 * 1000;
    private AtomicBoolean isAuthenticatedFlag = new AtomicBoolean();
    private long joinTime;
    private long authTime;

    public ClientHandler(MyServer myServer, Socket socket) {
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.nickname = "";
            this.isAuthenticatedFlag.set(false);

            //disconnect by timer
            new Thread(() -> {
                while (true) {
                    if (isAuthenticatedFlag.get()) {
                        if (authTime != 0 && System.currentTimeMillis() - authTime > timeToLogoutAuthenticated) {
                            closeConnection("3 min disconnect");
                            break;
                        }
                    } else {
                        if (joinTime != 0 && System.currentTimeMillis() - joinTime > timeToLogoutAnonimus) {
                            closeConnection("2 min disconnect");
                            break;
                        }
                    }
                }
            }).start();

            new Thread(() -> {
                try {
                    authentication();
                    readMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection("User " + nickname + " disconnected by 500.");
                }
            }).start();
        } catch (IOException e) {
            log.info("exception from core thread : {}");
            e.printStackTrace();
        }
    }


    private void authentication() throws IOException {
        joinTime = System.currentTimeMillis();
        while (true) {
            String messageFromClient = dis.readUTF();
            if (messageFromClient.startsWith("/auth")) {
                log.info("Authentication requested by user.");
                var arr = messageFromClient.split("\\s");
                var localNick = myServer
                        .getAuthService()
                        .getNickByLoginAndPassword(arr[1], arr[2]);
                if (localNick != null) {
                    if (!myServer.isNickBusy(localNick)) {
                        sendMessage("/authok " + localNick);
                        nickname = localNick;
                        myServer.subscribe(this);
                        authTime = System.currentTimeMillis();
                        isAuthenticatedFlag.set(true);
                        myServer.broadcastMessage("Hello " + nickname);

                        return;
                    } else {
                        sendMessage("Nickname is busy.");
                    }
                } else {
                    sendMessage("Wrong login and password");
                }
            }
        }
    }


    public void readMessage() throws IOException {
        var startAt = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - startAt > timeToLogoutAuthenticated) {
                closeConnection("disconnected by timer after 3 minutes");
                break;
            }
            var messageFromClient = dis.readUTF();
            handleMessageFromClient(messageFromClient);

        }

    }

    private void handleMessageFromClient(String messageFromClient) {
        log.info("{} send message {}", nickname, messageFromClient);

        if (messageFromClient.equals("/end")) {
            closeConnection("User " + nickname + " log out.");
            return;
        }

        if (messageFromClient.startsWith("/w")) {
            log.info("whisper mode /w activated");
            var words = messageFromClient.split("\\s");
            var messageRecipientNickname = words[1];
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < words.length; i++) {
                sb.append(words[i]);
            }
            var messageText = sb.toString();
            log.info("Sending message to {} by nickname: {}", messageRecipientNickname, messageText);
            var result = myServer.sendMessageToUserByNickname(messageRecipientNickname,
                    "PM from " + nickname + ": " + messageText);
            log.info(result);

        } else {
            log.info("this is not service message {}", messageFromClient);
            myServer.broadcastMessage(nickname + ": " + messageFromClient);
        }
        log.info("this is not service message {}", messageFromClient);

    }

    public void sendMessage(String message) {
        try {
            dos.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized String getNickname() {
        return nickname;
    }

    private void closeConnection(String message) {
        log.info("Connection was closed by client handler: {}", message);
        myServer.broadcastMessage("server to all: [" + nickname + "]" + message);
        myServer.unsubscribe(this);
        try {
            dis.close();
            dis.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
