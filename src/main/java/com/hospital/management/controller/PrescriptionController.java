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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/prescriptions")
@CrossOrigin(origins = "*")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private TriageService triageService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    // ==========================================
    //          1. GÖRÜNÜM METOTLARI (VIEW)
    // ==========================================

    @GetMapping("/view")
    public String showAllPrescriptions(Model model, HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) return "redirect:/";

        if ("PATIENT".equals(userType)) {
            // Hastanın kendi reçeteleri
            model.addAttribute("prescriptions", prescriptionService.getPrescriptionsByPatient(userId));
            model.addAttribute("pageTitle", "Reçetelerim");
        } else if ("DOCTOR".equals(userType)) {
            // Doktorun yazdığı reçeteler
            model.addAttribute("prescriptions", prescriptionService.getPrescriptionsByDoctor(userId));
            model.addAttribute("pageTitle", "Yazdığım Reçeteler");
        }
        return "prescriptions-list";
    }

    // ==========================================
    //          2. API METODLARI (JSON)
    // ==========================================

    // NORMAL RANDEVU REÇETESİ OLUŞTURMA
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createPrescription(@RequestBody Map<String, Object> prescriptionData) {
        try {
            Long appointmentId = Long.valueOf(prescriptionData.get("appointmentId").toString());
            Long doctorId = Long.valueOf(prescriptionData.get("doctorId").toString());
            String medications = prescriptionData.get("medications").toString();

            // Randevuyu ve Doktoru Doğrula
            Appointment appointment = appointmentService.getAppointmentById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Randevu bulunamadı!"));
            Doctor doctor = doctorService.getDoctorById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Doktor bulunamadı!"));

            // Reçete Nesnesini Oluştur
            Prescription prescription = new Prescription();
            prescription.setAppointment(appointment);
            prescription.setPatient(appointment.getPatient());
            prescription.setDoctor(doctor);
            prescription.setPrescriptionText(medications);
            prescription.setCreatedDate(LocalDateTime.now());

            // Opsiyonel Alanlar
            if (prescriptionData.containsKey("notes")) {
                prescription.setNotes(prescriptionData.get("notes").toString());
            }

            prescriptionService.savePrescription(prescription);
            return ResponseEntity.status(HttpStatus.CREATED).body("Reçete başarıyla kaydedildi.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Hata: " + e.getMessage());
        }
    }

    // TRİAJ (ACİL) REÇETESİ OLUŞTURMA
    @PostMapping("/api/triage")
    @ResponseBody
    public ResponseEntity<?> createTriagePrescription(@RequestBody Map<String, Object> data) {
        try {
            Long triageId = Long.valueOf(data.get("triageId").toString());
            Long doctorId = Long.valueOf(data.get("doctorId").toString());
            String medications = data.get("medications").toString();

            // Triaj Kaydını ve Doktoru Al
            TriageRecord triage = triageService.getTriageRecordById(triageId)
                    .orElseThrow(() -> new RuntimeException("Triaj kaydı bulunamadı!"));
            Doctor doctor = doctorService.getDoctorById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Doktor bulunamadı!"));

            // Reçete Oluştur
            Prescription p = new Prescription();
            p.setDoctor(doctor);
            p.setPatient(triage.getPatient());
            p.setAppointment(null); // Acil servis, randevusuzdur
            p.setPrescriptionText(medications);
            p.setCreatedDate(LocalDateTime.now());

            if (data.containsKey("notes")) {
                p.setNotes(data.get("notes").toString());
            }

            prescriptionService.savePrescription(p);

            // Muayeneyi tamamlandı olarak işaretle
            triageService.completeTriageMuayene(triageId);

            return ResponseEntity.ok("Acil reçetesi başarıyla oluşturuldu.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deletePrescription(@PathVariable Long id) {
        try {
            prescriptionService.deletePrescription(id); //
            return ResponseEntity.ok("Reçete silindi");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}