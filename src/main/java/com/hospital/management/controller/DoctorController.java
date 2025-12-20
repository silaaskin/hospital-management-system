package com.hospital.management.controller;

import com.hospital.management.model.Doctor;
import com.hospital.management.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller; // View döndürmek için @RestController yerine @Controller
import org.springframework.ui.Model; // Veri taşımak için
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller // Sınıf düzeyinde @Controller yaptık
@RequestMapping("/doctors") // Prefix'i sadeleştirdik
@CrossOrigin(origins = "*")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    // ==========================================
    //          VIEW (HTML) DÖNDÜREN METODLAR
    // ==========================================
    @GetMapping("/login") // http://localhost:8080/doctors/login
    public String showLoginPage() {
        return "login"; // templates/login.html dosyasını arar
    }

    @GetMapping("/view") // http://localhost:8080/doctors/view
    public String showAllDoctors(Model model) {
        List<Doctor> doctors = doctorService.getAllDoctors();
        model.addAttribute("doctors", doctors);
        model.addAttribute("pageTitle", "Doktor Listesi");
        return "doctors-list"; // templates/doctors-list.html dosyasını arar
    }

    @GetMapping("/view/{id}") // http://localhost:8080/doctors/view/1
    public String showDoctorDetail(@PathVariable Long id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı!"));
        model.addAttribute("doctor", doctor);
        return "doctor-detail"; // templates/doctor-detail.html
    }

    // ==========================================
    //          API (JSON) DÖNDÜREN METODLAR
    // ==========================================

    @GetMapping("/api")
    @ResponseBody // JSON dönmesi için metod bazında ekledik
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getDoctorById(@PathVariable Long id) {
        try {
            Doctor doctor = doctorService.getDoctorById(id)
                    .orElseThrow(() -> new RuntimeException("Doktor bulunamadı! ID: " + id));
            return ResponseEntity.ok(doctor);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/api/specialization/{specialization}")
    @ResponseBody
    public ResponseEntity<List<Doctor>> getDoctorsBySpecialization(@PathVariable String specialization) {
        List<Doctor> doctors = doctorService.getDoctorsBySpecialization(specialization);
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/api/department/{department}")
    @ResponseBody
    public ResponseEntity<List<Doctor>> getDoctorsByDepartment(@PathVariable String department) {
        List<Doctor> doctors = doctorService.getDoctorsByDepartment(department);
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<Doctor>> searchDoctors(@RequestParam String name) {
        List<Doctor> doctors = doctorService.searchDoctorsByName(name);
        return ResponseEntity.ok(doctors);
    }

    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            Doctor doctor = doctorService.login(username, password)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı adı veya şifre hatalı!"));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("doctor", doctor);
            response.put("message", "Giriş başarılı!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createDoctor(@RequestBody Doctor doctor) {
        try {
            Doctor savedDoctor = doctorService.saveDoctor(doctor);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDoctor);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> updateDoctor(@PathVariable Long id, @RequestBody Doctor doctor) {
        try {
            Doctor updatedDoctor = doctorService.updateDoctor(id, doctor);
            return ResponseEntity.ok(updatedDoctor);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        try {
            doctorService.deleteDoctor(id);
            return ResponseEntity.ok("Doktor başarıyla silindi!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}