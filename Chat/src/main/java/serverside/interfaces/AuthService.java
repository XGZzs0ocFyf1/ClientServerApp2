package serverside.interfaces;

//just interface for oop LSP in our case
public interface AuthService {

    void start();
    void stop();

    /**
     *  Returns null if user not exists, else return user's nickname
     */

    String logIn(String login, String password);

    /**
     * Register new user by sending user and password
     * @param login user login, not null, unique in db
     * @param password user pass
     * @return true if registration was successful, false if login exists or any other problem occurred
     */
    boolean register(String login, String password, String nickname);

    boolean rename( String oldNickname, String newNickName);
}
