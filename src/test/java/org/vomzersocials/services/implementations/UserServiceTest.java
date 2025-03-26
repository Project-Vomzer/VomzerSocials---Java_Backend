package org.vomzersocials.services.implementations;

import org.junit.jupiter.api.Test;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.services.implementations.UserService;
import org.vomzersocials.utils.Role;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private final UserService userService;

    public UserServiceTest(UserService userService) {
        this.userService = userService;
    }

    @Test
    public void test_thatUserCanRegister() {

        User user = new User();
        user.setUserName("Ade");
        user.setPassword("1234");
        user.setIsLoggedIn(false);
        user.setRole(Role.ADMIN);
        assertTrue(userService.registerNewUser(user));


    }

}