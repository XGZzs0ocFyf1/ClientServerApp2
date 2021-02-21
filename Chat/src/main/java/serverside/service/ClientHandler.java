package serverside.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private final static Logger log = LoggerFactory.getLogger(ClientHandler.class);

    private Socket socket;
    private MyServer myServer;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String nickname;

    public ClientHandler(MyServer myServer, Socket socket) {
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.nickname = "";

            new Thread(() -> {
                try {
                    authentication();
                    readMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }


            }).start();
        } catch (IOException e) {

        }
    }


    private void authentication() throws IOException {
        while (true) {
            String str = dis.readUTF();
            if (str.startsWith("/auth")) {
                log.info("Authentication requested by user.");
                var arr = str.split("\\s");
                var localNick = myServer
                        .getAuthService()
                        .getNickByLoginAndPassword(arr[1], arr[2]);
                if (localNick != null) {
                    if (!myServer.isNickBusy(localNick)) {
                        sendMessage("/authok " + localNick);
                        nickname = localNick;
                        myServer.subscribe(this);
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
        while (true) {

            var messageFromClient = dis.readUTF();

            log.info("{} send message {}", nickname, messageFromClient);

            if (messageFromClient.equals("/end")) {
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
                        "PM from " +nickname + ": "+ messageText);
                log.info(result);

            }else{
                myServer.broadcastMessage(nickname + ": " + messageFromClient);
            }


        }

    }

    public void sendMessage(String message) {
        try {
            dos.writeUTF(message);
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private void closeConnection() {
        log.info("Connection was closed.");
        myServer.unsubscribe(this);
        myServer.broadcastMessage(nickname + " leave chat.");

        try {
            dis.close();
            dis.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public String getNickname() {
        return nickname;
    }
}
