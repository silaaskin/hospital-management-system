package com.hospital.management.controller;

import com.hospital.management.model.Patient;
import com.hospital.management.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller; // Değiştirildi
import org.springframework.ui.Model; // Eklendi
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller // View desteği için @Controller
@RequestMapping("/patients") // URL prefixini sadeleştirdik
@CrossOrigin(origins = "*")
public class PatientController {

    @Autowired
    private PatientService patientService;

    // ==========================================
    //          VIEW (HTML) DÖNDÜREN METODLAR
    // ==========================================

    @GetMapping("/view") // http://localhost:8080/patients/view
    public String showAllPatients(Model model) {
        List<Patient> patients = patientService.getAllPatients();
        model.addAttribute("patients", patients);
        model.addAttribute("pageTitle", "Hasta Kayıt Listesi");
        return "patients-list"; // templates/patients-list.html
    }

    @GetMapping("/view/{id}") // http://localhost:8080/patients/view/1
    public String showPatientDetail(@PathVariable Long id, Model model) {
        Patient patient = patientService.getPatientById(id)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı!"));
        model.addAttribute("patient", patient);
        return "patient-detail"; // templates/patient-detail.html
    }

    // ==========================================
    //          API (JSON) DÖNDÜREN METODLAR
    // ==========================================

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<Patient>> getAllPatients() {
        List<Patient> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getPatientById(@PathVariable Long id) {
        try {
            Patient patient = patientService.getPatientById(id)
                    .orElseThrow(() -> new RuntimeException("Hasta bulunamadı! ID: " + id));
            return ResponseEntity.ok(patient);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/api/tc/{tcNo}")
    @ResponseBody
    public ResponseEntity<?> getPatientByTcNo(@PathVariable String tcNo) {
        try {
            Patient patient = patientService.getPatientByTcNo(tcNo)
                    .orElseThrow(() -> new RuntimeException("Hasta bulunamadı! TC No: " + tcNo));
            return ResponseEntity.ok(patient);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<Patient>> searchPatients(@RequestParam String name) {
        List<Patient> patients = patientService.searchPatientsByName(name);
        return ResponseEntity.ok(patients);
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createPatient(@RequestBody Patient patient) {
        try {
            Patient savedPatient = patientService.savePatient(patient);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPatient);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> updatePatient(@PathVariable Long id, @RequestBody Patient patient) {
        try {
            Patient updatedPatient = patientService.updatePatient(id, patient);
            return ResponseEntity.ok(updatedPatient);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deletePatient(@PathVariable Long id) {
        try {
            patientService.deletePatient(id);
            return ResponseEntity.ok("Hasta başarıyla silindi!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}