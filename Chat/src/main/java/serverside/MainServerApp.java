package serverside;

import serverside.service.MyServer;

//main: first start it, then chat clients
public class MainServerApp {

    public static void main(String[] args) {
        new MyServer() ;
    }
}
