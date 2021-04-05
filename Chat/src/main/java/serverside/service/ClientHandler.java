package serverside.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


//client handler class : works with chat clients
public class ClientHandler {

    private final static Logger log = LoggerFactory.getLogger(ClientHandler.class);

    private Socket socket;
    private MyServer myServer;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String nickname;


    private final long timeToLogoutAnonimus = 2 * 60 * 1000;
    private final long timeToLogoutAuthenticated = 3 * 60 * 1000;
    private final AtomicBoolean isAuthenticatedFlag = new AtomicBoolean();
    private final AtomicBoolean isTimer3MinuteRefreshed = new AtomicBoolean();
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
            this.isTimer3MinuteRefreshed.set(false);

            ExecutorService executor = Executors.newFixedThreadPool(2);

            //disconnect by timer
            Thread timeoutHandlerTask = timeoutHandler();
            Thread mainLogicTask = mainLogic();

            executor.submit(timeoutHandlerTask);
            executor.submit(mainLogicTask);

            //close executor if all tasks performed
            executor.shutdown();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Thread mainLogic() {
        Thread t2 = new Thread(() -> {
            try {
                authentication();
                readMessage();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection("User " + nickname + " disconnected by 500.");
            }
        });
        return t2;
    }

    private Thread timeoutHandler() {
        Thread t1 = new Thread(() -> {
            while (true) {
                //refresh inactive status if send messages
                if (isAuthenticatedFlag.get()) {
                    if (isTimer3MinuteRefreshed.get()){
                        isTimer3MinuteRefreshed.set(false);
                        authTime = System.currentTimeMillis();
                        log.info("auth time set to current");
                    }

                    //disconnect after 3 mins
                    if (authTime != 0 && System.currentTimeMillis() - authTime > timeToLogoutAuthenticated) {
                        closeConnection("3 min disconnect");
                        break;
                    }
                } else {
                    //disconnect anonymous users
                    if (joinTime != 0 && System.currentTimeMillis() - joinTime > timeToLogoutAnonimus) {
                        closeConnection("2 min disconnect");
                        break;
                    }
                }
            }
        });
        return t1;
    }

    /**
     * handles cases: <br>
     *resigstration - register user. Calls by message /register login, password, nickname,
     * where login - authentication login, passwrod user password, nickname - user nick name in chat
     * if login already exists - do nothing
     *
     * <br>
     *     authentication - authenticate existed user. Calls by message in chat /auth login password
     *
     * @throws IOException
     */
    private void authentication() throws IOException {
        joinTime = System.currentTimeMillis();
        while (true) {
            String messageFromClient = dis.readUTF();

            //authentication request;
            if (messageFromClient.startsWith("/auth")) {
                log.info("Authentication requested by user.");
                var arr = messageFromClient.split("\\s");
                var localNick = myServer
                        .getAuthService()
                        .logIn(arr[1], arr[2]);


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

            if(messageFromClient.startsWith("/register")){
                String[] messageContent = messageFromClient.split("\\s");
                boolean isRegistered = myServer.getAuthService().register(messageContent[1], messageContent[2],
                        messageContent[3]);
                if (isRegistered){
                    log.info("User {} successfuly registered.", messageContent[1]);
                    sendMessage("You successfuly registered");
                }else{
                    log.info("Login \"{}\" or nickname \"{}\" already exists in this chat", messageContent[1],
                            messageContent[3]);
                    sendMessage("Login "+messageContent[1] + " or nickname "+nickname+" already exists in this chat.");
                }
            }







        }
    }


    public void readMessage() throws IOException {

        while (true) {
            var messageFromClient = dis.readUTF();
            handleMessageFromClient(messageFromClient);
            isTimer3MinuteRefreshed.set(true);

        }

    }

    private void handleMessageFromClient(String messageFromClient) {
        log.info("{} send message {}", nickname, messageFromClient);

        if (messageFromClient.equals("/end")) {
            closeConnection("User " + nickname + " log out.");
            return;
        }

        if(messageFromClient.startsWith("/rename")){
            String[] messageContent = messageFromClient.split("\\s");
            if (messageContent[1] !=null){
                boolean isRenamed = myServer.getAuthService().rename(this.nickname, messageContent[2]);
                if (isRenamed){
                    sendMessage("User "+nickname+" renamed to "+messageContent[2]);
                    this.nickname = messageContent[2];
                }
            }else{
                sendMessage("Change name request should looks like: /rename oldUserName newUserName");
            }
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
