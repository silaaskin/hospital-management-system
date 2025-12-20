package com.hospital.management.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    /**
     * KÖK DİZİN YÖNLENDİRMESİ
     * Tarayıcıya sadece http://localhost:8080/ yazıldığında çalışır.
     * Kullanıcıyı doğrudan Doktor Giriş Ekranına yönlendirir.
     */
    @GetMapping("/")
    public String index() {
        // "redirect:" komutu tarayıcıyı başka bir adrese postalar
        return "redirect:/doctors/login";
    }

    /**
     * DASHBOARD (ANA MENÜ) SAYFASI
     * Giriş başarılı olduğunda (login.html içindeki JS ile) buraya yönlendirilir.
     * templates/dashboard.html dosyasını ekrana basar.
     */
    @GetMapping("/dashboard")
    public String showDashboard() {
        return "dashboard";
    }
}