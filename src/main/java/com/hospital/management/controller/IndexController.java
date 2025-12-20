package com.hospital.management.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    // 1. Ana sayfaya geleni SEÇİM EKRANINA yolla
    @GetMapping("/")
    public String index() {
        return "login-selection"; // templates/login-selection.html
    }

    // 2. Dashboard'a gitmek isteyeni KONTROL ET ve YÖNLENDİR
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        String userType = (String) session.getAttribute("userType");
        String userName = (String) session.getAttribute("userName");

        // Giriş yapmamışsa ana sayfaya at
        if (userType == null) {
            return "redirect:/";
        }

        model.addAttribute("userName", userName);

        // Rolüne göre doğru sayfayı aç
        if ("DOCTOR".equals(userType)) {
            return "dashboard-doctor"; // templates/dashboard-doctor.html
        } else if ("PATIENT".equals(userType)) {
            return "dashboard-patient"; // templates/dashboard-patient.html
        }

        return "redirect:/";
    }

    // 3. Çıkış Yap (Oturumu Siler)
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}