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
    public ResponseEntity<?> createPrescription(@RequestBody Map<String, Object> data) {
        try {
            Long appointmentId = Long.valueOf(data.get("appointmentId").toString());
            Long doctorId = Long.valueOf(data.get("doctorId").toString());

            Appointment app = appointmentService.getAppointmentById(appointmentId).orElseThrow();
            Doctor doctor = doctorService.getDoctorById(doctorId).orElseThrow();

            Prescription p = new Prescription();
            p.setAppointment(app);
            p.setPatient(app.getPatient());
            p.setDoctor(doctor);
            p.setMedications(data.get("medications").toString());
            p.setDosage(data.get("dosage") != null ? data.get("dosage").toString() : "");
            p.setInstructions(data.get("instructions") != null ? data.get("instructions").toString() : "");

            if(data.get("durationDays") != null && !data.get("durationDays").toString().isEmpty()) {
                p.setDurationDays(Integer.valueOf(data.get("durationDays").toString()));
            }

            p.setNotes(data.get("notes") != null ? data.get("notes").toString() : "");
            p.setPrescriptionDate(LocalDate.now());

            prescriptionService.savePrescription(p);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
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