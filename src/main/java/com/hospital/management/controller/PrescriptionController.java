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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/prescriptions")
@CrossOrigin(origins = "*")
public class PrescriptionController {

    @Autowired private PrescriptionService prescriptionService;
    @Autowired private TriageService triageService;
    @Autowired private DoctorService doctorService;
    @Autowired private AppointmentService appointmentService;

    @GetMapping("/view")
    public String showAllPrescriptions(Model model, HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/";

        if ("PATIENT".equals(userType)) {
            model.addAttribute("prescriptions", prescriptionService.getPrescriptionsByPatient(userId));
            model.addAttribute("pageTitle", "Reçetelerim");
        } else if ("DOCTOR".equals(userType)) {
            model.addAttribute("prescriptions", prescriptionService.getPrescriptionsByDoctor(userId));
            model.addAttribute("pageTitle", "Yazdığım Reçeteler");
        }
        return "prescriptions-list";
    }

    // NORMAL RANDEVU REÇETESİ
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createPrescription(@RequestBody Map<String, Object> prescriptionData) {
        try {
            Long appointmentId = Long.valueOf(prescriptionData.get("appointmentId").toString());
            Long doctorId = Long.valueOf(prescriptionData.get("doctorId").toString());

            // 1. Randevuyu ve Doktoru Doğrula
            Appointment appointment = appointmentService.getAppointmentById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Randevu bulunamadı!"));
            Doctor doctor = doctorService.getDoctorById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Doktor bulunamadı!"));

            // 2. Hastayı Randevudan Al ve Geçerliliğini Kontrol Et
            Patient patient = appointment.getPatient();
            if (patient == null || patient.getId() == null || patient.getId() == 0) {
                throw new RuntimeException("Bu randevuya bağlı geçerli bir hasta bulunamadı (ID 0 Hatası)!");
            }

            Prescription prescription = new Prescription();
            prescription.setAppointment(appointment);
            prescription.setPatient(patient); // Hastayı açıkça set ediyoruz
            prescription.setDoctor(doctor);

            // 3. Verileri Doldur
            if (prescriptionData.get("medications") != null)
                prescription.setMedications(prescriptionData.get("medications").toString());

            if(prescriptionData.containsKey("dosage"))
                prescription.setDosage(prescriptionData.get("dosage").toString());

            if(prescriptionData.containsKey("instructions"))
                prescription.setInstructions(prescriptionData.get("instructions").toString());

            if(prescriptionData.containsKey("durationDays") && !prescriptionData.get("durationDays").toString().isEmpty()) {
                prescription.setDurationDays(Integer.valueOf(prescriptionData.get("durationDays").toString()));
            }

            if(prescriptionData.containsKey("notes"))
                prescription.setNotes(prescriptionData.get("notes").toString());

            prescription.setPrescriptionDate(LocalDate.now());

            // 4. Kaydet
            prescriptionService.savePrescription(prescription);

            return ResponseEntity.status(HttpStatus.CREATED).body("Reçete başarıyla kaydedildi.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Hata: " + e.getMessage());
        }
    }
    // TRİAJ (ACİL) REÇETESİ
    @PostMapping("/api/triage")
    @ResponseBody
    public ResponseEntity<?> createTriagePrescription(@RequestBody Map<String, Object> data) {
        try {
            Long triageId = Long.valueOf(data.get("triageId").toString());
            Long doctorId = Long.valueOf(data.get("doctorId").toString());

            TriageRecord triage = triageService.getTriageRecordById(triageId).orElseThrow();
            Doctor doctor = doctorService.getDoctorById(doctorId).orElseThrow();

            Prescription p = new Prescription();
            p.setDoctor(doctor);
            p.setPatient(triage.getPatient());
            p.setAppointment(null);
            p.setMedications(data.get("medications").toString());
            p.setNotes(data.get("notes") != null ? data.get("notes").toString() : "");
            p.setPrescriptionDate(LocalDate.now());

            prescriptionService.savePrescription(p);
            triageService.completeTriageMuayene(triageId);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}