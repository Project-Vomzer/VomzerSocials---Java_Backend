package org.vomzersocials.user.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.LoginRequest;
import org.vomzersocials.user.dtos.requests.RegisterUserRequest;
import org.vomzersocials.user.dtos.responses.LoginResponse;
import org.vomzersocials.user.dtos.responses.RegisterUserResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AuthenticationServiceImpl implements org.vomzersocials.user.services.interfaces.AuthenticationService {
    @Autowired
    private UserRepository userRepository;
    private String foundUserUserName;

    @Override
    public RegisterUserResponse registerNewUser(RegisterUserRequest registerUserRequest) {
        doesUserAlreadyExist(registerUserRequest.getUserName());
        nullOrWhiteSpaceChecker(registerUserRequest);

        User user = getUserDetailsForNewRegistration(registerUserRequest);

        RegisterUserResponse registerStaffResponse = new RegisterUserResponse();
        registerStaffResponse.setUserName(user.getUsername());
        registerStaffResponse.setIsLoggedIn(false);
        registerStaffResponse.setMessage("User is registered in successfully.");
        foundUserUserName = registerStaffResponse.getUserName();

        return registerStaffResponse;
    }

    private void doesUserAlreadyExist(String username) {
        if (userRepository.findUserByUserName(username) != null ){
            throw new IllegalArgumentException("Username already exists");
        }
    }

    private static void nullOrWhiteSpaceChecker(RegisterUserRequest registerUserRequest) {
        if (containsWhiteSpace(registerUserRequest.getUserName()) ||
                containsWhiteSpace(registerUserRequest.getPassword())){
            throw new IllegalArgumentException("Username and password are required!");
        }
        if (registerUserRequest.getPassword().isEmpty() || registerUserRequest.getUserName().isEmpty()){
            throw new IllegalArgumentException("Username or password cannot be empty");
        }
    }

    private static boolean containsWhiteSpace(String username) {
        Pattern pattern = Pattern.compile("(.*?)\\s(.*?)");
        Matcher matcher = pattern.matcher(username);
        return matcher.find();
    }

    private User getUserDetailsForNewRegistration(RegisterUserRequest registerUserRequest) {
        User user = new User();
        user.setUserName(registerUserRequest.getUserName());
        user.setPassword(registerUserRequest.getPassword());
        user.setRole(registerUserRequest.getRole());
        user.setIsLoggedIn(false);

        isRegisteredUserEntityMoreThan1();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        user.setDateOfCreation(LocalDate.parse(LocalDate.now().format(formatter), formatter).atStartOfDay());
        userRepository.save(user);
        return user;
    }

    private void isRegisteredUserEntityMoreThan1() {
        if (userRepository.count() == 1){
            throw new IllegalArgumentException("User already exists");
        }
    }

    public LoginResponse loginUser(LoginRequest userLoginRequest) {
        User foundUser = getUser();
        if (foundUser == null)
            throw new IllegalArgumentException("User not found");
        if (!foundUser.getPassword().equals(userLoginRequest.getPassword()) ||
                !foundUser.getUsername().equals(userLoginRequest.getUsername()))
            throw new IllegalArgumentException("Wrong username or password");
        checkLoginDetails(userLoginRequest);
        foundUser.setIsLoggedIn(true);
        userRepository.save(foundUser);
        return loginStaffIfCredentialsAreCorrect(userLoginRequest, foundUser);
    }

    private LoginResponse loginStaffIfCredentialsAreCorrect(LoginRequest loginRequest, User foundUser) {
        if (foundUser.getPassword().equals(loginRequest.getPassword()) &&
                foundUser.getUsername().equals(loginRequest.getUsername())) {
            foundUser.setIsLoggedIn(true);
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setUserName(foundUser.getUsername());
            loginResponse.setMessage("Logged in successfully");
            return loginResponse;
        }
        return null;
    }

    private void checkLoginDetails(LoginRequest loginRequest) {
        if (containsWhiteSpace(loginRequest.getUsername()) || containsWhiteSpace(loginRequest.getPassword())
                || loginRequest.getUsername().trim().equals("") || loginRequest.getPassword().trim().equals("")){
            throw new IllegalArgumentException("Username or password cannot be empty");
        }
    }

    private User getUser() {
        String username = foundUserUserName;
        return userRepository.findUserByUserName(username);
    }

    public LoginResponse logoutUser(LoginRequest loginRequest) {
        User user = getUser();
        user.setIsLoggedIn(false);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setMessage("Logged out successfully");
        userRepository.save(user);
        return loginResponse;
    }


    private void checkIfUserIsLoggedIn(){
        User foundStaff = getUser();
        System.out.println(foundStaff);
        if (!foundStaff.getIsLoggedIn())
            throw new IllegalArgumentException("Staff is not logged in...");
    }
}
