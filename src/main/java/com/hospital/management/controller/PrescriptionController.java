package com.hospital.management.controller;

import com.hospital.management.model.Prescription;
import com.hospital.management.service.PrescriptionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/prescriptions")
@CrossOrigin(origins = "*")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    // ==========================================
    //          AKILLI GÖRÜNÜM (ROL BAZLI)
    // ==========================================

    @GetMapping("/view")
    public String showAllPrescriptions(Model model, HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        Long userId = (Long) session.getAttribute("userId");

        if ("PATIENT".equals(userType)) {
            // HASTA İSE: Sadece kendi reçetelerini görsün
            List<Prescription> myPrescriptions = prescriptionService.getPrescriptionsByPatient(userId);
            model.addAttribute("prescriptions", myPrescriptions);
            model.addAttribute("pageTitle", "Reçetelerim");
        } else if ("DOCTOR".equals(userType)) {
            // DOKTOR İSE: Tüm reçeteleri görsün
            List<Prescription> allPrescriptions = prescriptionService.getAllPrescriptions();
            model.addAttribute("prescriptions", allPrescriptions);
            model.addAttribute("pageTitle", "Tüm Reçeteler (Doktor Paneli)");
        } else {
            return "redirect:/"; // Giriş yoksa at
        }

        return "prescriptions-list"; // templates/prescriptions-list.html
    }

    // ==========================================
    //          API METODLARI
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
            return ResponseEntity.ok(prescriptionService.getPrescriptionById(id).orElseThrow());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // (Diğer filtreleme metodların: doctor, patient, date-range aynen kalabilir)

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createPrescription(@RequestBody Map<String, Object> prescriptionData) {
        try {
            Long appointmentId = Long.valueOf(prescriptionData.get("appointmentId").toString());
            Long doctorId = Long.valueOf(prescriptionData.get("doctorId").toString());

            Prescription prescription = new Prescription();
            prescription.setMedications(prescriptionData.get("medications").toString());
            if(prescriptionData.containsKey("dosage")) prescription.setDosage(prescriptionData.get("dosage").toString());
            if(prescriptionData.containsKey("instructions")) prescription.setInstructions(prescriptionData.get("instructions").toString());
            if(prescriptionData.containsKey("durationDays")) prescription.setDurationDays(Integer.valueOf(prescriptionData.get("durationDays").toString()));
            if(prescriptionData.containsKey("notes")) prescription.setNotes(prescriptionData.get("notes").toString());

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    prescriptionService.createPrescription(appointmentId, doctorId, prescription)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // (Delete ve Update metodlarını da buraya ekleyebilirsin)
}