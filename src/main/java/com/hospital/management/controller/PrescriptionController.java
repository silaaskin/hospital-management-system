package com.hospital.management.controller;

import com.hospital.management.model.Prescription;
import com.hospital.management.service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller; // Değiştirildi
import org.springframework.ui.Model; // Eklendi
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller // View desteği için @Controller
@RequestMapping("/prescriptions") // URL sadeleştirildi
@CrossOrigin(origins = "*")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    // ==========================================
    //          VIEW (HTML) DÖNDÜREN METODLAR
    // ==========================================

    @GetMapping("/view") // http://localhost:8080/prescriptions/view
    public String showAllPrescriptions(Model model) {
        List<Prescription> prescriptions = prescriptionService.getAllPrescriptions();
        model.addAttribute("prescriptions", prescriptions);
        model.addAttribute("pageTitle", "Sistemdeki Tüm Reçeteler");
        return "prescriptions-list"; // templates/prescriptions-list.html
    }

    @GetMapping("/view/{id}") // http://localhost:8080/prescriptions/view/1
    public String showPrescriptionDetail(@PathVariable Long id, Model model) {
        Prescription prescription = prescriptionService.getPrescriptionById(id)
                .orElseThrow(() -> new RuntimeException("Reçete bulunamadı!"));
        model.addAttribute("prescription", prescription);
        return "prescription-detail"; // templates/prescription-detail.html
    }

    // ==========================================
    //          API (JSON) DÖNDÜREN METODLAR
    // ==========================================

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<Prescription>> getAllPrescriptions() {
        return ResponseEntity.ok(prescriptionService.getAllPrescriptions());
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getPrescriptionById(@PathVariable Long id) {
        try {
            Prescription prescription = prescriptionService.getPrescriptionById(id)
                    .orElseThrow(() -> new RuntimeException("Reçete bulunamadı! ID: " + id));
            return ResponseEntity.ok(prescription);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/api/appointment/{appointmentId}")
    @ResponseBody
    public ResponseEntity<?> getPrescriptionByAppointment(@PathVariable Long appointmentId) {
        try {
            Prescription prescription = prescriptionService.getPrescriptionByAppointment(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Bu randevu için reçete bulunamadı!"));
            return ResponseEntity.ok(prescription);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/api/doctor/{doctorId}")
    @ResponseBody
    public ResponseEntity<?> getPrescriptionsByDoctor(@PathVariable Long doctorId) {
        try {
            return ResponseEntity.ok(prescriptionService.getPrescriptionsByDoctor(doctorId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/api/patient/{patientId}")
    @ResponseBody
    public ResponseEntity<List<Prescription>> getPrescriptionsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionsByPatient(patientId));
    }

    @GetMapping("/api/date-range")
    @ResponseBody
    public ResponseEntity<List<Prescription>> getPrescriptionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionsByDateRange(start, end));
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createPrescription(@RequestBody Map<String, Object> prescriptionData) {
        try {
            Long appointmentId = Long.valueOf(prescriptionData.get("appointmentId").toString());
            Long doctorId = Long.valueOf(prescriptionData.get("doctorId").toString());

            Prescription prescription = new Prescription();
            prescription.setMedications(prescriptionData.get("medications").toString());
            // Diğer set işlemlerini burada yapabilirsin (dosage, instructions vs.)

            Prescription savedPrescription = prescriptionService.createPrescription(appointmentId, doctorId, prescription);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPrescription);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deletePrescription(@PathVariable Long id) {
        try {
            prescriptionService.deletePrescription(id);
            return ResponseEntity.ok("Reçete başarıyla silindi!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}