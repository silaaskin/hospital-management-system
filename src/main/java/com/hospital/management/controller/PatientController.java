package com.hospital.management.controller;

import com.hospital.management.model.Appointment;
import com.hospital.management.model.Doctor;
import com.hospital.management.model.Patient;
import com.hospital.management.model.Prescription;
import com.hospital.management.service.AppointmentService;
import com.hospital.management.service.DoctorService;
import com.hospital.management.service.PatientService;
import com.hospital.management.service.PrescriptionService;
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

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PrescriptionService prescriptionService;

    // ==========================================
    //          GİRİŞ VE GÜVENLİK
    // ==========================================

    @GetMapping("/login")
    public String showLoginPage() {
        return "login-patient";
    }

    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        String tcNo = credentials.get("tcNo");
        String password = credentials.get("password");

        Optional<Patient> patient = patientService.login(tcNo);

        if (patient.isPresent() && password != null && password.equals(patient.get().getPassword())) {
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

    @PostMapping("/add")
    public String savePatientFromForm(@ModelAttribute Patient patient) {
        generatePasswordForPatient(patient);
        patientService.savePatient(patient);
        return "redirect:/patients/view";
    }

    // --- RANDEVU ALMA EKRANI ---
    @GetMapping("/appointments/book")
    public String showBookAppointmentPage(Model model, HttpSession session) {
        Long patientId = (Long) session.getAttribute("userId");
        if (patientId == null) return "redirect:/patients/login";

        List<Doctor> doctors = doctorService.getAllDoctors();
        List<String> departments = doctors.stream()
                .map(Doctor::getSpecialization)
                .distinct()
                .collect(Collectors.toList());

        model.addAttribute("doctors", doctors);
        model.addAttribute("departments", departments);

        return "patient-appointment-book";
    }

    // --- RANDEVU KAYDETME (DÜZELTİLDİ: Try-Catch Eklendi) ---
    @PostMapping("/appointments/book")
    public String bookAppointment(@RequestParam("doctorId") Long doctorId,
                                  @RequestParam("appointmentDate") String dateStr,
                                  HttpSession session) {

        Long patientId = (Long) session.getAttribute("userId");
        if (patientId == null) return "redirect:/patients/login";

        try {
            LocalDateTime date = LocalDateTime.parse(dateStr);
            appointmentService.createAppointment(patientId, doctorId, date);

            return "redirect:/dashboard?success=RandevuBasariylaOlusturuldu";

        } catch (Exception e) {
            e.printStackTrace(); // Hatanın terminalde görünmesi için

            // Hatayı URL'e güvenli şekilde ekleyelim (Türkçe karakter sorunu olmasın)
            String hataMesaji = "Islem basarisiz";
            try {
                hataMesaji = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            } catch (Exception ex) {}

            return "redirect:/dashboard?error=" + hataMesaji;
        }
    }

    // --- REÇETELERİM ---
    @GetMapping("/prescriptions")
    public String showPrescriptions(Model model, HttpSession session) {
        Long patientId = (Long) session.getAttribute("userId");
        if (patientId == null) return "redirect:/patients/login";

        List<Prescription> myPrescriptions = prescriptionService.getPrescriptionsByPatient(patientId);
        model.addAttribute("prescriptionList", myPrescriptions);

        return "patient-prescriptions";
    }

    // --- ŞİFRE OLUŞTURUCU ---
    private void generatePasswordForPatient(Patient patient) {
        String tc = patient.getTcNo();
        String telefon = patient.getPhone();
        String temizTelefon = (telefon != null) ? telefon.replaceAll("\\D", "") : "";

        if (tc != null && tc.length() >= 4 && temizTelefon.length() >= 4) {
            String tcNinBasi = tc.substring(0, 4);
            String telefonunSonu = temizTelefon.substring(temizTelefon.length() - 4);
            patient.setPassword(telefonunSonu + tcNinBasi);
        } else {
            patient.setPassword("123456");
        }
    }

    // Edit ve Detail (Değişiklik yok)
    @GetMapping("/edit/{id}")
    public String showEditPatientPage(@PathVariable Long id, Model model, HttpSession session) {
        Optional<Patient> p = patientService.getPatientById(id);
        if(p.isPresent()){
            model.addAttribute("patient", p.get());
            return "patient-edit";
        }
        return "redirect:/patients/view";
    }

    @GetMapping("/view/{id}")
    public String showPatientDetail(@PathVariable Long id, Model model) {
        Optional<Patient> p = patientService.getPatientById(id);
        if(p.isPresent()) {
            model.addAttribute("patient", p.get());
            return "patient-detail";
        }
        return "redirect:/patients/view";
    }

    // ==========================================
    //          API METODLARI
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

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createPatient(@RequestBody Patient patient) {
        try {
            generatePasswordForPatient(patient);
            return ResponseEntity.status(HttpStatus.CREATED).body(patientService.savePatient(patient));
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

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> updatePatient(@PathVariable Long id, @RequestBody Patient patient) {
        try {
            return ResponseEntity.ok(patientService.updatePatient(id, patient));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}