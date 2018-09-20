package byteMe.services;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class GameInstanceService {


    public int generateInstanceID() {
        return 100000 + ThreadLocalRandom.current().nextInt(900000);
    }

    public String getCurrentUsername() {
        Object userObject = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userObject instanceof UserDetails) {
           return ((UserDetails) userObject).getUsername();
        }
        return null;
    }

}
