package com.hospital.management.controller;

import com.hospital.management.model.Doctor;
import com.hospital.management.service.DoctorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/doctors")
@CrossOrigin(origins = "*")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login-doctor"; // templates/login-doctor.html dosyasını açar
    }

    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            Doctor doctor = doctorService.login(username, password)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı adı veya şifre hatalı!"));

            session.setAttribute("userType", "DOCTOR");
            session.setAttribute("userId", doctor.getId());
            session.setAttribute("userName", doctor.getFirstName() + " " + doctor.getLastName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }


    @GetMapping("/view")
    public String showAllDoctors(Model model) {
        List<Doctor> doctors = doctorService.getAllDoctors();
        model.addAttribute("doctors", doctors);
        return "doctors-list";
    }

    @GetMapping("/view/{id}")
    public String showDoctorDetail(@PathVariable Long id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı!"));
        model.addAttribute("doctor", doctor);
        return "doctor-detail";
    }

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getDoctorById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(doctorService.getDoctorById(id)
                    .orElseThrow(() -> new RuntimeException("Doktor bulunamadı")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/api/specialization/{specialization}")
    @ResponseBody
    public ResponseEntity<List<Doctor>> getDoctorsBySpecialization(@PathVariable String specialization) {
        return ResponseEntity.ok(doctorService.getDoctorsBySpecialization(specialization));
    }


    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createDoctor(@RequestBody Doctor doctor) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.saveDoctor(doctor));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> updateDoctor(@PathVariable Long id, @RequestBody Doctor doctor) {
        try {
            return ResponseEntity.ok(doctorService.updateDoctor(id, doctor));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        try {
            doctorService.deleteDoctor(id);
            return ResponseEntity.ok("Doktor silindi");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}