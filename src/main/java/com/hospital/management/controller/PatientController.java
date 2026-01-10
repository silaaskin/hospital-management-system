package com.hospital.management.controller;

import com.hospital.management.model.*;
import com.hospital.management.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/patients")
@CrossOrigin(origins = "*")
public class PatientController {

    @Autowired private PatientService patientService;
    @Autowired private DoctorService doctorService;
    @Autowired private AppointmentService appointmentService;
    @Autowired private PrescriptionService prescriptionService;
    @Autowired private TriageService triageService;

    @GetMapping("/login")
    public String showLoginPage() { return "login-patient"; }

    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        System.out.println("=== HASTA GİRİŞİ ===");
        String tcNo = credentials.get("tcNo");
        String password = credentials.get("password");

        System.out.println("Gelen TC: " + tcNo);
        System.out.println("Gelen Şifre: " + password);

        Optional<Patient> patientOpt = patientService.getPatientByTcNo(tcNo);

        if (patientOpt.isEmpty()) {
            System.out.println("❌ Hasta bulunamadı! TC: " + tcNo);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("TC Kimlik numarası sistemde kayıtlı değil!");
        }

        Patient patient = patientOpt.get();
        System.out.println("✅ Hasta bulundu:");
        System.out.println("  - ID: " + patient.getId());
        System.out.println("  - İsim: " + patient.getFirstName() + " " + patient.getLastName());
        System.out.println("  - TC: " + patient.getTcNo());
        System.out.println("  - Veritabanı Şifresi: " + patient.getPassword());

        if (patient.getPassword() == null || patient.getPassword().isEmpty()) {
            System.out.println("⚠️ UYARI: Hastanın şifresi boş! Otomatik şifre oluşturuluyor...");
            // Şifre yoksa oluştur
            String newPassword = generatePasswordForPatient(patient);
            patient.setPassword(newPassword);
            patientService.savePatient(patient);
            System.out.println("✅ Yeni şifre oluşturuldu: " + newPassword);
        }

        if (password != null && password.equals(patient.getPassword())) {
            // SESSION'A KAYDEDİYORUZ
            session.setAttribute("userType", "PATIENT");
            session.setAttribute("userId", patient.getId());
            session.setAttribute("userName", patient.getFirstName() + " " + patient.getLastName());

            System.out.println("✅ GİRİŞ BAŞARILI!");
            System.out.println("  - Session User Type: " + session.getAttribute("userType"));
            System.out.println("  - Session User ID: " + session.getAttribute("userId"));
            System.out.println("  - Session User Name: " + session.getAttribute("userName"));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("patientId", patient.getId());
            response.put("patientName", patient.getFirstName() + " " + patient.getLastName());
            return ResponseEntity.ok(response);
        } else {
            System.out.println("❌ Şifre yanlış!");
            System.out.println("  - Beklenen: " + patient.getPassword());
            System.out.println("  - Girilen: " + password);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Şifre hatalı!");
        }
    }

    @GetMapping("/view")
    public String showAllPatients(Model model, HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        if (!"DOCTOR".equals(userType) && !"SECRETARY".equals(userType)) return "redirect:/dashboard";
        model.addAttribute("patients", patientService.getAllPatients());
        model.addAttribute("pageTitle", "Hasta Kayıt Listesi");
        return "patients-list";
    }

    @GetMapping("/history/{id}")
    public String showPatientHistory(@PathVariable Long id, Model model) {
        Patient patient = patientService.getPatientById(id).orElseThrow();
        model.addAttribute("patient", patient);
        model.addAttribute("appointments", appointmentService.getAppointmentsByPatient(id));
        model.addAttribute("triageRecords", triageService.getTriageRecordsByPatient(id));
        model.addAttribute("prescriptions", prescriptionService.getPrescriptionsByPatient(id));
        return "patient-history";
    }

    @GetMapping("/add")
    public String showAddPatientPage(HttpSession session) {
        if (!"DOCTOR".equals(session.getAttribute("userType")) && !"SECRETARY".equals(session.getAttribute("userType"))) return "redirect:/dashboard";
        return "patient-add";
    }

    @PostMapping("/add")
    public String savePatientFromForm(@ModelAttribute Patient patient) {
        String password = generatePasswordForPatient(patient);
        patient.setPassword(password);
        patientService.savePatient(patient);
        System.out.println("✅ Yeni hasta kaydedildi. ID: " + patient.getId() + ", Şifre: " + password);
        return "redirect:/patients/view";
    }

    @GetMapping("/appointments/book")
    public String showBookAppointmentPage(Model model, HttpSession session) {
        String userType = (String) session.getAttribute("userType");

        if ("SECRETARY".equals(userType)) {
            model.addAttribute("allPatients", patientService.getAllPatients());
        } else if (session.getAttribute("userId") == null) {
            return "redirect:/patients/login";
        }

        List<Doctor> doctors = doctorService.getAllDoctors();
        model.addAttribute("doctors", doctors);
        model.addAttribute("departments", doctors.stream().map(Doctor::getSpecialization).distinct().collect(Collectors.toList()));

        return "patient-appointment-book";
    }

    @PostMapping("/appointments/book")
    public String bookAppointment(@RequestParam("doctorId") Long doctorId,
                                  @RequestParam("appointmentDate") String dateStr,
                                  @RequestParam(value = "patientId", required = false) Long pId,
                                  HttpSession session) {
        Long patientId = (pId != null) ? pId : (Long) session.getAttribute("userId");

        System.out.println("=== RANDEVU OLUŞTURMA ===");
        System.out.println("Session'dan gelen Patient ID: " + session.getAttribute("userId"));
        System.out.println("Parametre'den gelen Patient ID: " + pId);
        System.out.println("Kullanılacak Patient ID: " + patientId);
        System.out.println("Doktor ID: " + doctorId);
        System.out.println("Tarih: " + dateStr);

        if (patientId == null) {
            System.out.println("❌ Patient ID null! Session boş olabilir.");
            return "redirect:/patients/login";
        }

        try {
            appointmentService.createAppointment(patientId, doctorId, LocalDateTime.parse(dateStr));
            System.out.println("✅ Randevu başarıyla oluşturuldu!");
            return "redirect:/dashboard?success=RandevuOlusturuldu";
        } catch (Exception e) {
            System.err.println("❌ Randevu oluşturma hatası: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/dashboard?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    @GetMapping("/prescriptions")
    public String showPrescriptions(Model model, HttpSession session) {
        Long patientId = (Long) session.getAttribute("userId");

        System.out.println("=== HASTA REÇETE SAYFASI ===");
        System.out.println("Session'dan Patient ID: " + patientId);

        if (patientId == null) {
            System.out.println("❌ Session'da patient ID yok! Login sayfasına yönlendiriliyor.");
            return "redirect:/patients/login";
        }

        List<Prescription> prescriptions = prescriptionService.getPrescriptionsByPatient(patientId);
        System.out.println("✅ Bulunan reçete sayısı: " + prescriptions.size());

        model.addAttribute("prescriptionList", prescriptions);
        return "patient-prescriptions";
    }

    private String generatePasswordForPatient(Patient patient) {
        String tc = patient.getTcNo();
        String telefon = patient.getPhone();
        String temizTelefon = (telefon != null) ? telefon.replaceAll("\\D", "") : "";

        if (tc != null && tc.length() >= 4 && temizTelefon.length() >= 4) {
            String password = temizTelefon.substring(temizTelefon.length() - 4) + tc.substring(0, 4);
            System.out.println("Şifre oluşturuldu: " + password + " (Telefon son 4: " + temizTelefon.substring(temizTelefon.length() - 4) + " + TC ilk 4: " + tc.substring(0, 4) + ")");
            return password;
        } else {
            System.out.println("⚠️ Varsayılan şifre kullanılıyor: 123456");
            return "123456";
        }
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createPatient(@RequestBody Patient patient) {
        try {
            String password = generatePasswordForPatient(patient);
            patient.setPassword(password);
            Patient saved = patientService.savePatient(patient);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/view/{id}")
    public String showPatientDetail(@PathVariable Long id, Model model) {
        patientService.getPatientById(id).ifPresent(p -> model.addAttribute("patient", p));
        return "patient-detail";
    }

    @GetMapping("/edit/{id}")
    public String showEditPatientPage(@PathVariable Long id, Model model) {
        patientService.getPatientById(id).ifPresent(p -> model.addAttribute("patient", p));
        return "patient-edit";
    }
}