package serverside.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serverside.interfaces.AuthService;

import java.util.ArrayList;
import java.util.List;

//base auth service with hardcoded credentials (just for leaning)
public class BaseAuthService implements AuthService {

    private final static Logger log = LoggerFactory.getLogger(BaseAuthService.class);
    private List<Entry> entries;

    public BaseAuthService(){
        entries = new ArrayList<>();
        entries.add(new Entry("a", "b", "Johny"));
        entries.add(new Entry("c", "d", "Alex"));
        entries.add(new Entry("e", "f", "HaiBao"));
    }




    @Override
    public void start() {
        log.info("AuthService started.");

    }

    @Override
    public void stop() {
        log.info("AuthService stopped.");
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password){
        for (Entry entry: entries ){
            if (entry.login.equals(login) && entry.password.equals(password)){
                return entry.nickname;
            }
        }
        return "anonimus";
    }

    @AllArgsConstructor
    private class Entry{

        private String login;
        private String password;
        private String nickname;
    }


}
