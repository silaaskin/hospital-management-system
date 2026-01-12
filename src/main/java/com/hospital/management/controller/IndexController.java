package com.hospital.management.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "login-selection";
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        String userType = (String) session.getAttribute("userType");
        String userName = (String) session.getAttribute("userName");

        if (userType == null) return "redirect:/";

        model.addAttribute("userName", userName);

        if ("DOCTOR".equals(userType)) {
            return "dashboard-doctor";
        } else if ("PATIENT".equals(userType)) {
            return "dashboard-patient";
        } else if ("SECRETARY".equals(userType)) {
            return "dashboard-secretary";
        }

        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}