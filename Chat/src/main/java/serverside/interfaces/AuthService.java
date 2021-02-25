package serverside.interfaces;

//just interface for oop LSP in our case
public interface AuthService {

    void start();
    void stop();
    String getNickByLoginAndPassword(String login, String password);
}
