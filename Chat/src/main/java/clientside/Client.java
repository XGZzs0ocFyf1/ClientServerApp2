package clientside;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;


//Client №0 for testing chat

/**
 * java 3 task 3.
 * modified client class
 * add local storage (text file). It will be create automaticaly if not exists.
 * at client start file opened (or created).
 * history populated from file
 * history appended by text while client working
 * history crop by last 100 and then saved to file
 */
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
    private Queue<String> history = new LinkedBlockingQueue<>();
    private final static String FILENAME = "src/main/resources/history.txt";

    public Client() {

        try {
            connection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        prepareGUI();

    }


    /**
     * Saves 100 or history.size() if queue size is less than 100 elements starting
     * from head of queue.
     */
    private void saveHistory() {
        log.info("Saving history to local file");


        //take 100 strings or all letters if their number is less than 100
        int counter = history.size() > 100 ? 100 : history.size();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILENAME))) {
            while (counter > 0) {
                String message = history.poll();
                writer.write(message);
                counter--;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    /**
     * Method reads file and add all its content to message queue.
     * @param filename local file on client
     */
    private void readHistoryFromFile(String filename) {

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = null;
            int counter = 0;
            while ((line = br.readLine()) != null) {
                System.out.println(counter);
                history.add(line);
                counter++;
            }
            log.info(" {} элементов скопировано в историю", counter);

        } catch (FileNotFoundException e) {
            log.info("Файл {} не существует. Создаю файл", filename);
            // File f = new File(filename);
            //  log.info("Файл {} создан.", f.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connection() throws IOException {
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());

        log.info("Connecting to socket {}", socket.getPort());
        log.info("dis : {}", dis);
        log.info("dos : {}", dos);
        log.info("Printing history of last 100 messages:");

        new Thread(() -> {
            readHistoryFromFile(FILENAME);
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
                    String chatOutput = "Server [" + time + "] : " + message + "\n";
                    chatArea.append(chatOutput);
                    history.add(chatOutput);
                }

                while (true) {
                    String message = dis.readUTF();
                    var time = formatter.format(LocalDateTime.now());

                    //adds to history and to chat area
                    String chatOutput = "Server [" + time + "] : " + message + "\n";
                    chatArea.append(chatOutput);
                    history.add(chatOutput);
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
                    saveHistory();
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

    public static void main(String[] args) throws IOException {

//        CopyOnWriteArrayList<String> history = new CopyOnWriteArrayList<>();
//        history.add("1\n");
//        history.add("2\n");
//        history.add("3\n");
//        history.add("4\n");
//        history.add("5\n");
//        history.add("6\n");
//        log.info("Saving history to local file");
//
//        java.util.List<String> localHistory100 = history.stream().limit(100).collect(Collectors.toList());
//
//        BufferedWriter writer = new BufferedWriter(new FileWriter(FILENAME));
//        int counter = 0;
//        while (counter < localHistory100.size()){
//            writer.write(localHistory100.get(counter));
//            counter++;
//        }
//        writer.close();


        SwingUtilities.invokeLater(() -> {
            new Client();
        });
    }
}
