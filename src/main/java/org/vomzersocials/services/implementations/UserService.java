package org.vomzersocials.services.implementations;

import org.springframework.stereotype.Service;
import org.vomzersocials.data.models.User;
import org.vomzersocials.data.repositories.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public boolean registerNewUser(User user) {
            User newUser = new User();
            newUser.setUserName(user.getUserName());
            newUser.setPassword(user.getPassword());
            newUser.setRole(user.getRole());
            newUser.setIsLoggedIn(false);
            userRepository.save(user);
            return true;
    }
}
