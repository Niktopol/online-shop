package com.example.onlineshop.service;

import com.example.onlineshop.model.dto.UserDTO;
import com.example.onlineshop.model.entity.User;
import com.example.onlineshop.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import static com.example.onlineshop.model.entity.User.Role.CUSTOMER;

@AllArgsConstructor
@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final SecurityContextRepository securityContextRepository;
    private final AuthenticationManager authenticationManager;
    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Transactional
    public String createUser(User user) {
        try{
            userRepository.save(user);
        }catch (Exception e){
            return String.format("Username '%s' is already taken", user.getUsername());
        }
        return "";
    }

    public String signUp(UserDTO userData) {
        if (userData.getName().isEmpty()){
            return "'name' value is required";
        }
        if (userData.getUsername().isEmpty()){
            return "'username' value is required";
        }
        if (userData.getPassword().isEmpty()){
            return "'password' value is required";
        }
        User user = new User(userData.getName(), userData.getUsername(),
                passwordEncoder.encode(userData.getPassword()), CUSTOMER, true);

        return createUser(user);
    }

    public String signIn(UserDTO userData, HttpServletRequest request, HttpServletResponse response) {
        if (userData.getUsername().isEmpty()){
            return "'username' value is required";
        }
        if (userData.getPassword().isEmpty()){
            return "'password' value is required";
        }
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                userData.getUsername(), userData.getPassword());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(token);
        }catch (AuthenticationException e){
            return e.getMessage();
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextRepository.saveContext(context, request, response);

        return "";
    }
}
