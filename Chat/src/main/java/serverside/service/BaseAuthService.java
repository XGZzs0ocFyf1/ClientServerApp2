package serverside.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serverside.interfaces.AuthService;
import serverside.model.User;

import java.sql.*;

//base auth service with hardcoded DB credentials  (just for leaning)
public class BaseAuthService implements AuthService {

    private final static String DRIVER = "com.mysql.jdbc.Driver";
    private final static String DB = "jdbc:mysql://localhost:3306/j3t2";
    private final static String LOGIN = "root";
    private final static String PASSWORD = "4Ij_rG3F^$@gSV";

    private final static String FIND_BY_LOGIN = "select * from users where login= ?";
    private final static String FIND_BY_NICKNAME = "select * from users where nickname = ?";
    private final static String SAVE_USER = "insert into users(login, password, nickname) values(?, ?, ?)";
    private final static String RENAME_REQUEST = "UPDATE users SET nickname = ? where login = ?";

    private final static Logger log = LoggerFactory.getLogger(BaseAuthService.class);
    private ConnectionWrapper connectionWrapper;

    @Override
    public void start() {
        log.info("AuthService started.");
        connectionWrapper = ConnectionWrapper.getInstance();
    }

    @Override
    public void stop() {
        log.info("AuthService stopped.");
        connectionWrapper.closeConnection();
    }

    @Override
    public String logIn(String login, String password) {

        User user = findUserByLogin(login);

        if (user == null) {
            return "anonimus";
        }

        if (user.getLogin().equals(login) && user.getPassword().equals(password)) {
            return user.getNickname();
        }

        return "anonimus";
    }

    @Override
    public boolean register(String login, String password, String nickname) {

        //check if user exists in database; if exists return false (can't register); nick also unique
        if (findUserByLogin(login) != null || findUserByNickname(nickname) != null) {
            return false;
        }

        //registration process
        Connection connection = connectionWrapper.getConnection();

        try(PreparedStatement ps =  connection.prepareStatement(SAVE_USER)){
            ps.setString(1, login);
            ps.setString(2, password);
            ps.setString(3, nickname);
            ps.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }

    /**
     * Method renames old user nickname to new user nick name. This is not user login.
     * @param oldNickName - Old nickname
     * @param newNickName - new nickname
     * @return true if rename was successful, false if not
     */
    @Override
    public boolean rename(String oldNickName, String newNickName) {

        if (!oldNickName.isBlank()){

            User user = findUserByNickname(oldNickName);
            if (user !=null){
                Connection connection = connectionWrapper.getConnection();

                try(PreparedStatement ps =  connection.prepareStatement(RENAME_REQUEST)) {
                    ps.setString(1, newNickName);
                    ps.setString(2, user.getLogin());
                    ps.executeUpdate();
                } catch (SQLException e) {

                    e.printStackTrace();
                    return false;
                }

            }
            return true;
        }
        return false;
    }

    private User findUserByLogin(String login) {
        Connection con = connectionWrapper.getConnection();
        try (PreparedStatement preparedStatement = con.prepareStatement(FIND_BY_LOGIN)) {
            preparedStatement.setString(1, login);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                User user = new User().userBuilder(rs);
                if (user != null) {
                    return user;
                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    private User findUserByNickname(String nickname) {
        try (
                Connection con = connectionWrapper.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement(FIND_BY_NICKNAME);

        ) {
            preparedStatement.setString(1, nickname);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                User user = new User().userBuilder(rs);
                if (user != null) {
                    return user;
                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }
}
