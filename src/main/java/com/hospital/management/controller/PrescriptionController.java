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

    @GetMapping("/view")
    public String showAllPrescriptions(Model model, HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/";

        if ("PATIENT".equals(userType)) {
            // Hasta sadece kendine yazılanları görür
            model.addAttribute("prescriptions", prescriptionService.getPrescriptionsByPatient(userId));
            model.addAttribute("pageTitle", "Reçetelerim");
        } else if ("DOCTOR".equals(userType)) {
            // DEĞİŞİKLİK: Doktor sadece KENDİ yazdığı reçeteleri görür
            List<Prescription> myPrescriptions = prescriptionService.getPrescriptionsByDoctor(userId);
            model.addAttribute("prescriptions", myPrescriptions);
            model.addAttribute("pageTitle", "Yazdığım Reçeteler");
        }
        return "prescriptions-list";
    }

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
            p.setPatient(triage.getPatient()); // Hastayı triaj kaydından aldık
            p.setAppointment(null);            // Randevu ID hatasını önlemek için null
            p.setMedications(data.get("medications").toString());
            p.setNotes(data.get("notes") != null ? data.get("notes").toString() : "");
            p.setPrescriptionDate(LocalDate.now());

            prescriptionService.savePrescription(p);

            // Muayene tamamlandığı için statü güncelle
            triageService.completeTriageMuayene(triageId);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}