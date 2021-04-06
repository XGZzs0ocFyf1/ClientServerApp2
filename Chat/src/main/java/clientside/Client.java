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
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


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
    private List<String> history = new ArrayList<>();
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

        //here we select start position 0 (if size - 100 < 0) or some initial value (that = size - 100)
        int counter = Math.max(0, history.size() - 100);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILENAME))) {
            while (counter < history.size()) {

                String message = history.get(counter);
                log.info("{} : {} ", counter, message);
                writer.write(message);
                counter++;
            }
            log.info("Saved");
        } catch (IOException e) {
            log.info("could not save to file");
            e.printStackTrace();
        }


    }

    /**
     * Method reads file and add all its content to message queue.
     *
     * @param filename local file on client
     */
    private void readHistoryFromFile(String filename) {

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int counter = 0;
            while ((line = br.readLine()) != null) {
                log.info("{} : {}", counter, line);
                history.add(line + "\n");
                counter++;
            }
            log.info(" {} элементов скопировано в историю", counter);

        } catch (FileNotFoundException e) {
            log.info("Файл {} не существует. Создаю файл", filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void printLast100FromHistory() {
        log.info("history size {}", history.size());
        log.info("Printing history of last {} messages:");

        int counter = history.size() - 100;
        counter = Math.max(counter, 0); //check if startIDx negative - starts from zero

        while (counter < history.size()) {
            String message = history.get(counter);
            log.info("{} : {}", counter, message);
            chatArea.append(message);
            counter++;
        }


    }


    public void send() {
        if (msgInputField.getText() != null && !msgInputField.getText().trim().isEmpty()) {

            try {
                dos.writeUTF(msgInputField.getText());

                if (msgInputField.getText().equals("/end")) {
                    isAuthorised = false;
                    saveHistory();
                    closeConnection();
                }
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

    public void connection() throws IOException {
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());

        log.info("Connecting to socket {}", socket.getPort());
        log.info("dis : {}", dis);
        log.info("dos : {}", dos);

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

                }

                printLast100FromHistory();

                while (true) {
                    String message = dis.readUTF();
                    var time = formatter.format(LocalDateTime.now());

                    //adds to history and to chat area
                    String chatOutput = "Server [" + time + "] : " + message + "\n";
                    chatArea.append(chatOutput);
                    history.add(chatOutput);
                }


            } catch (IOException e) {
                saveHistory();
                e.printStackTrace();
            }
        }).start();
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

        //generate random color background
        var blueColorValue = ThreadLocalRandom.current().nextInt(50, 250);
        var redColorValue = ThreadLocalRandom.current().nextInt(50, 250);
        var greenColorValue = ThreadLocalRandom.current().nextInt(50, 250);
        chatArea.setBackground(new Color(redColorValue, greenColorValue, blueColorValue));

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
