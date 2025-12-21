package com.hospital.management.controller;

import com.hospital.management.model.Secretary;
import com.hospital.management.repository.SecretaryRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/secretaries")
public class SecretaryController {

    @Autowired
    private SecretaryRepository secretaryRepository;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login-secretary"; // templates/login-secretary.html
    }

    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Optional<Secretary> sec = secretaryRepository.findByUsername(username);

        if (sec.isPresent() && sec.get().getPassword().equals(password)) {
            session.setAttribute("userType", "SECRETARY");
            session.setAttribute("userId", sec.get().getId());
            session.setAttribute("userName", sec.get().getFirstName() + " " + sec.get().getLastName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).body("Hatalı giriş");
    }
}