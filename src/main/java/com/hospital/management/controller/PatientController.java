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
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Giriş hatalı!");
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
        generatePasswordForPatient(patient);
        patientService.savePatient(patient);
        return "redirect:/patients/view";
    }

    @GetMapping("/appointments/book")
    public String showBookAppointmentPage(Model model, HttpSession session) {
        String userType = (String) session.getAttribute("userType");

        // Eğer sekreter ise tüm hastaları seçebilmesi için listeye ekle
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
    public String bookAppointment(@RequestParam("doctorId") Long doctorId, @RequestParam("appointmentDate") String dateStr, @RequestParam(value = "patientId", required = false) Long pId, HttpSession session) {
        Long patientId = (pId != null) ? pId : (Long) session.getAttribute("userId");
        if (patientId == null) return "redirect:/patients/login";
        try {
            appointmentService.createAppointment(patientId, doctorId, LocalDateTime.parse(dateStr));
            return "redirect:/dashboard?success=RandevuOlusturuldu";
        } catch (Exception e) {
            return "redirect:/dashboard?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    @GetMapping("/prescriptions")
    public String showPrescriptions(Model model, HttpSession session) {
        Long patientId = (Long) session.getAttribute("userId");
        if (patientId == null) return "redirect:/patients/login";
        model.addAttribute("prescriptionList", prescriptionService.getPrescriptionsByPatient(patientId));
        return "patient-prescriptions";
    }

    private void generatePasswordForPatient(Patient patient) {
        String tc = patient.getTcNo();
        String telefon = patient.getPhone();
        String temizTelefon = (telefon != null) ? telefon.replaceAll("\\D", "") : "";
        if (tc != null && tc.length() >= 4 && temizTelefon.length() >= 4) {
            patient.setPassword(temizTelefon.substring(temizTelefon.length() - 4) + tc.substring(0, 4));
        } else {
            patient.setPassword("123456");
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