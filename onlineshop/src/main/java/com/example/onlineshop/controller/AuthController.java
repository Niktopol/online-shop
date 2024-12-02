package com.example.onlineshop.controller;

import com.example.onlineshop.model.dto.UserDTO;
import com.example.onlineshop.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@CrossOrigin
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody UserDTO userData){
        String resp = authService.signUp(userData);
        if(resp.isEmpty()){
            return ResponseEntity.status(HttpServletResponse.SC_CREATED).body("User created");
        }else{
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).body(resp);
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<String> signIn(@RequestBody UserDTO userData, HttpServletRequest request, HttpServletResponse response){
        String resp = authService.signIn(userData, request, response);
        if(resp.isEmpty()){
            return ResponseEntity.ok("Signed in successfully");
        }else{
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).body(resp);
        }
    }
}
