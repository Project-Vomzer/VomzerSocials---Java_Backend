package org.vomzersocials.user.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.RegisterUserRequest;
import org.vomzersocials.user.dtos.responses.RegisterUserResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserService implements org.vomzersocials.user.services.interfaces.UserService {

    @Autowired
    private UserRepository userRepository;
    private String foundUserUserName;

    @Override
    public RegisterUserResponse registerNewUser(RegisterUserRequest registerUserRequest) {
        doesUserAlreadyExist(registerUserRequest.getUserName());
        nullOrWhiteSpaceChecker(registerUserRequest);

        User user = getUSerDetailsForNewRegistration(registerUserRequest);

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

    private User getUSerDetailsForNewRegistration(RegisterUserRequest registerUserRequest) {
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
            throw new IllegalArgumentException("Staff already exists");
        }
    }
//
//    @Override
//    public StaffLoginResponse loginStaff(StaffLoginRequest staffLoginRequest) {
//        Staff foundStaff = getStaff();
//        if (foundStaff == null)
//            throw new IllegalArgumentException("Staff not found");
//        if (!foundStaff.getPassword().equals(staffLoginRequest.getPassword()) ||
//                !foundStaff.getUsername().equals(staffLoginRequest.getUsername()))
//            throw new IllegalArgumentException("Wrong username or password");
//        checkLoginDetails(staffLoginRequest);
//        foundStaff.setIsLoggedIn(true);
//        staffRepository.save(foundStaff);
//        return loginStaffIfCredentialsAreCorrect(staffLoginRequest, foundStaff);
//    }
//
//    private StaffLoginResponse loginStaffIfCredentialsAreCorrect(StaffLoginRequest staffLoginRequest, Staff foundStaff) {
//        if (foundStaff.getPassword().equals(staffLoginRequest.getPassword()) &&
//                foundStaff.getUsername().equals(staffLoginRequest.getUsername())) {
//            foundStaff.setIsLoggedIn(true);
//            StaffLoginResponse staffLoginResponse = new StaffLoginResponse();
//            staffLoginResponse.setUsername(foundStaff.getUsername());
//            staffLoginResponse.setMessage("Logged in successfully");
//            return staffLoginResponse;
//        }
//        return null;
//    }

//    private void checkLoginDetails(StaffLoginRequest staffLoginRequest) {
//        if (containsWhiteSpace(staffLoginRequest.getUsername()) || containsWhiteSpace(staffLoginRequest.getPassword())
//                || staffLoginRequest.getUsername().trim().equals("") || staffLoginRequest.getPassword().trim().equals("")){
//            throw new IllegalArgumentException("Username or password cannot be empty");
//        }
//    }

    private User getUser() {
        String username = foundUserUserName;
        return userRepository.findUserByUserName(username);
    }
//
//    @Override
//    public StaffLogoutResponse logoutStaff(StafflogoutRequest stafflogoutRequest) {
//        Staff staff = getStaff();
//        staff.setIsLoggedIn(false);
//
//        StaffLogoutResponse staffLogoutResponse = new StaffLogoutResponse();
//        staffLogoutResponse.setMessage("Logged out successfully");
//        staffRepository.save(staff);
//        return staffLogoutResponse;
//    }

//    @Override
//    public RegisterStudentResponse registerStudent(RegisterStudentRequest registerStudentRequest) {
//        Staff foundStaff = getStaff();
//        checkIfStaffIsLoggedIn();
//        foundStaff.setIsLoggedIn(true);
//        return studentService.registerStudent(registerStudentRequest);
//    }

    private void checkIfStaffIsLoggedIn(){
        User foundStaff = getUser();
        System.out.println(foundStaff);
        if (!foundStaff.getIsLoggedIn())
            throw new IllegalArgumentException("Staff is not logged in...");
    }

}
