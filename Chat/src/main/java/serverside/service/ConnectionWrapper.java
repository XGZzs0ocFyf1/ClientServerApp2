package serverside.service;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton
 *
 * Wrapper for connection instead of using connection pools.
 * This way used in learning purposes.
 */
@Slf4j
public final class ConnectionWrapper {


    private final static String DB = "jdbc:mysql://localhost:3306/j3t2";
    private final static String LOGIN = "root";
    private final static String PASSWORD = "4Ij_rG3F^$@gSV";

    private static volatile ConnectionWrapper instance;

    private ConnectionWrapper(){}

    public  static ConnectionWrapper getInstance(){
        if (instance == null){
            synchronized (ConnectionWrapper.class) {
                if (instance == null){
                    instance = new ConnectionWrapper();
                }
            }
        }
        return instance;
    }


    private static Connection connection;

    /**
     * Connection handler.
     * @return old connection if it is not empty or closed, otherwise return new connection instance.
     */
    public  Connection getConnection(){
        try {
            if (connection != null && !connection.isClosed()){
                return connection;
            }else{
                //if connection is null or already was closed we create new
                connection = DriverManager.getConnection(DB, LOGIN, PASSWORD);
                return connection;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return connection;
    }

    public  void closeConnection(){
        log.info("Closing connection ");
        try {
            connection.close();
        } catch (SQLException e) {
            log.info("The error occured while closing connection");
            e.printStackTrace();
        }

    }
}
