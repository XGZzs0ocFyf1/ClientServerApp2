package serverside.model;

import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;

@Data
public class User {
    private int id;
    private String login;
    private String password;
    private String nickname;

    public User userBuilder(ResultSet resultSet) throws SQLException {
        this.setId(resultSet.getInt(1));
        this.setLogin(resultSet.getString(2));
        this.setPassword(resultSet.getString(3));
        this.setNickname(resultSet.getString(4));

        return this;
    }
}
