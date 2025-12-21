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
        return "login-patient";
    }

    // API: Hasta Giriş İşlemi (GÜNCELLENDİ: Şifre Kontrolü Eklendi)
    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        String tcNo = credentials.get("tcNo");
        String password = credentials.get("password"); // Şifreyi de alıyoruz

        Optional<Patient> patient = patientService.login(tcNo);

        // Hem TC var mı, HEM DE şifre doğru mu kontrolü
        if (patient.isPresent() && password != null && password.equals(patient.get().getPassword())) {

            // OTURUM BİLGİLERİNİ KAYDET
            session.setAttribute("userType", "PATIENT");
            session.setAttribute("userId", patient.get().getId());
            session.setAttribute("userName", patient.get().getFirstName() + " " + patient.get().getLastName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("TC Kimlik No veya Şifre hatalı!");
        }
    }

    // ==========================================
    //          VIEW (HTML) METODLARI
    // ==========================================

    @GetMapping("/view")
    public String showAllPatients(Model model, HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        if (!"DOCTOR".equals(userType) && !"SECRETARY".equals(userType)) {
            return "redirect:/dashboard";
        }

        List<Patient> patients = patientService.getAllPatients();
        model.addAttribute("patients", patients);
        model.addAttribute("pageTitle", "Hasta Kayıt Listesi");
        return "patients-list";
    }

    @GetMapping("/add")
    public String showAddPatientPage(HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        if (!"DOCTOR".equals(userType) && !"SECRETARY".equals(userType)) {
            return "redirect:/dashboard";
        }
        return "patient-add";
    }

    // YENİ EKLENEN MVC METODU: HTML Formundan gelen veriyi kaydeder
    @PostMapping("/add")
    public String savePatientFromForm(@ModelAttribute Patient patient) {
        // --- OTOMATİK ŞİFRE OLUŞTURMA MANTIĞI ---
        generatePasswordForPatient(patient);
        // ----------------------------------------

        patientService.savePatient(patient);
        return "redirect:/patients/view";
    }

    @GetMapping("/edit/{id}")
    public String showEditPatientPage(@PathVariable Long id, Model model, HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        if (!"DOCTOR".equals(userType) && !"SECRETARY".equals(userType)) {
            return "redirect:/dashboard";
        }

        Patient patient = patientService.getPatientById(id)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı"));
        model.addAttribute("patient", patient);

        return "patient-edit";
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
            // --- OTOMATİK ŞİFRE OLUŞTURMA MANTIĞI (API İÇİN DE) ---
            generatePasswordForPatient(patient);
            // ------------------------------------------------------

            return ResponseEntity.status(HttpStatus.CREATED).body(patientService.savePatient(patient));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> updatePatient(@PathVariable Long id, @RequestBody Patient patient) {
        try {
            // Güncelleme yaparken şifre değişmesin istiyorsan buraya dokunma.
            // Eğer şifreyi de değiştirebilsinler istersen buraya da mantık eklenebilir.
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

    private void generatePasswordForPatient(Patient patient) {
        String tc = patient.getTcNo();
        String telefon = patient.getPhone();

        // Verileri temizleyelim (sadece rakam kalsın)
        String temizTelefon = (telefon != null) ? telefon.replaceAll("\\D", "") : "";

        // KONTROL: TC 11 hane mi VE Telefon geçerli uzunlukta mı?
        if (tc != null && tc.length() >= 4 && temizTelefon.length() >= 4) {

            String tcNinBasi = tc.substring(0, 4);
            String telefonunSonu = temizTelefon.substring(temizTelefon.length() - 4);

            // Şifre: TelSon4 + Tcİlk4
            String olusanSifre = telefonunSonu + tcNinBasi;
            patient.setPassword(olusanSifre);

        } else {
            // ARTIK 123456 YOK!
            // Eğer bilgiler eksikse kayıt işlemini durduruyoruz.
            throw new IllegalArgumentException("Otomatik şifre oluşturulamadı! Lütfen TC ve Telefon bilgilerini eksiksiz giriniz.");
        }
    }
}