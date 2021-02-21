package clientside;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


//Client #0 for testing chat
public class Client {

    private final static Logger log = LoggerFactory.getLogger(Client.class);
    private final int SERVER_PORT = 8082;
    private final String SERVER_ADDRESS = "localhost";
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private JTextArea chatArea;
    private JTextField msgInputField;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss");
    private boolean isAuthorised = false;

    public Client() {
        try {
            connection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        prepareGUI();
    }

    public void connection() throws IOException {
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());

        log.info("Connecting to socket {}", socket.getPort());
        log.info("dis : {}", dis);
        log.info("dos : {}", dos);

        new Thread(() -> {
            try {

                //Auth infinite loop; if authorised = false stay here
                while (true) {
                    String message = dis.readUTF();

                    //client side authorisation by server responce
                    if (message.startsWith("/authok")) {
                        setAuthorised();
                        break;
                    }
                    var time = formatter.format(LocalDateTime.now());
                    chatArea.append("Server [" + time + "] : " + message + "\n");
                }

                while (true) {
                    String message = dis.readUTF();
                    var time = formatter.format(LocalDateTime.now());
                    chatArea.append("Server [" + time + "] : " + message + "\n");
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public void send() {
        if (msgInputField.getText() != null && !msgInputField.getText().trim().isEmpty()) {

            try {
                var time = formatter.format(LocalDateTime.now());
                dos.writeUTF(msgInputField.getText());

                if (msgInputField.getText().equals("/end")) {
                    isAuthorised = false;
                    closeConnection();
                }
//                chatArea.append("Client [" + time + "] : " + msgInputField.getText() + "\n");
                msgInputField.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeConnection() {
        try {
            dis.close();
            dos.close();
            socket.close();
        } catch (IOException e) {

        }
    }

    private void prepareGUI() {
        JFrame frame = new JFrame("Chat client");
        frame.setBounds(600, 300, 400, 400);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        BorderLayout borderLayout = new BorderLayout();
        frame.setLayout(borderLayout);
        frame.setResizable(false);
        chatArea = new JTextArea(21, 20);
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setBackground(new Color(190, 134, 243));

        msgInputField = new JTextField(29);
        var sendButton = new JButton("send");

        sendButton.addActionListener(e -> {
            send();
        });

        msgInputField.addActionListener(e -> {
            send();
        });

        frame.add(new JScrollPane(chatArea), BorderLayout.NORTH);
        frame.add(msgInputField, BorderLayout.WEST);
        frame.add(sendButton, BorderLayout.EAST);
        frame.setVisible(true);
    }

    private void setAuthorised() {
        this.isAuthorised = true;
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new Client();
        });
    }
}
