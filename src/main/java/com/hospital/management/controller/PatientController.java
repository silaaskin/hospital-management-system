package com.hospital.management.controller;

import com.hospital.management.model.Patient;
import com.hospital.management.service.PatientService;
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
import java.util.Optional;

@Controller
@RequestMapping("/patients")
@CrossOrigin(origins = "*")
public class PatientController {

    @Autowired
    private PatientService patientService;

    // ==========================================
    //          GİRİŞ VE GÜVENLİK (SESSION)
    // ==========================================

    // VİEW: Hasta Giriş Sayfası
    @GetMapping("/login")
    public String showLoginPage() {
        return "login-patient"; // templates/login-patient.html dosyasını açar
    }

    // API: Hasta Giriş İşlemi
    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        String tcNo = credentials.get("tcNo");
        Optional<Patient> patient = patientService.login(tcNo);

        if (patient.isPresent()) {
            // OTURUM BİLGİLERİNİ KAYDET
            session.setAttribute("userType", "PATIENT");
            session.setAttribute("userId", patient.get().getId());
            session.setAttribute("userName", patient.get().getFirstName() + " " + patient.get().getLastName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("TC Kimlik No bulunamadı!");
        }
    }

    // ==========================================
    //          VIEW (HTML) METODLARI
    // ==========================================

    @GetMapping("/view")
    public String showAllPatients(Model model, HttpSession session) {
        // GÜVENLİK: Bu sayfayı sadece Doktorlar görebilir
        String userType = (String) session.getAttribute("userType");
        if (!"DOCTOR".equals(userType)) {
            return "redirect:/dashboard"; // Yetkisiz giriş ise panele at
        }

        List<Patient> patients = patientService.getAllPatients();
        model.addAttribute("patients", patients);
        model.addAttribute("pageTitle", "Hasta Kayıt Listesi");
        return "patients-list";
    }

    @GetMapping("/view/{id}")
    public String showPatientDetail(@PathVariable Long id, Model model) {
        Patient patient = patientService.getPatientById(id)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı!"));
        model.addAttribute("patient", patient);
        return "patient-detail";
    }

    // ==========================================
    //          API (JSON) METODLARI
    // ==========================================

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<Patient>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getPatientById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(patientService.getPatientById(id)
                    .orElseThrow(() -> new RuntimeException("Hasta bulunamadı")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/api/tc/{tcNo}")
    @ResponseBody
    public ResponseEntity<?> getPatientByTcNo(@PathVariable String tcNo) {
        try {
            return ResponseEntity.ok(patientService.getPatientByTcNo(tcNo)
                    .orElseThrow(() -> new RuntimeException("Hasta bulunamadı")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<Patient>> searchPatients(@RequestParam String name) {
        return ResponseEntity.ok(patientService.searchPatientsByName(name));
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createPatient(@RequestBody Patient patient) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(patientService.savePatient(patient));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> updatePatient(@PathVariable Long id, @RequestBody Patient patient) {
        try {
            return ResponseEntity.ok(patientService.updatePatient(id, patient));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deletePatient(@PathVariable Long id) {
        try {
            patientService.deletePatient(id);
            return ResponseEntity.ok("Hasta silindi");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}