package com.hospital.management.controller;

import com.hospital.management.model.Appointment;
import com.hospital.management.model.Appointment.AppointmentStatus;
import com.hospital.management.service.AppointmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    // ==========================================
    //          1. AKILLI GÖRÜNÜM (VIEW)
    // ==========================================

    @GetMapping("/view")
    public String showAppointments(Model model, HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        Long userId = (Long) session.getAttribute("userId");

        if ("PATIENT".equals(userType)) {
            // HASTA İSE: Sadece kendi randevularını görsün
            List<Appointment> myAppointments = appointmentService.getAppointmentsByPatient(userId);
            model.addAttribute("appointments", myAppointments);
            model.addAttribute("pageTitle", "Randevularım");
        } else if ("DOCTOR".equals(userType)) {
            // --- GÜNCELLEME BURADA YAPILDI ---
            // ESKİSİ: appointmentService.getAllAppointments();
            // YENİSİ: Sadece giriş yapan doktora ait randevuları getirir.
            List<Appointment> myDoctorAppointments = appointmentService.getAppointmentsByDoctor(userId);
            model.addAttribute("appointments", myDoctorAppointments);
            model.addAttribute("pageTitle", "Randevu Listem");
        } else {
            return "redirect:/"; // Giriş yapmamışsa ana sayfaya at
        }

        return "appointments-list"; // templates/appointments-list.html dosyasını açar
    }

    // ==========================================
    //          2. API METODLARI (JSON)
    // ==========================================

    // Tüm randevuları listele
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    // ID'ye göre randevu getir
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getAppointmentById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(appointmentService.getAppointmentById(id)
                    .orElseThrow(() -> new RuntimeException("Randevu bulunamadı")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Hastaya göre randevuları listele
    @GetMapping("/api/patient/{patientId}")
    @ResponseBody
    public ResponseEntity<?> getAppointmentsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(patientId));
    }

    // Doktora göre randevuları listele
    @GetMapping("/api/doctor/{doctorId}")
    @ResponseBody
    public ResponseEntity<?> getAppointmentsByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(doctorId));
    }

    // Duruma göre randevuları listele (ONAYLANDI, İPTAL vb.)
    @GetMapping("/api/status/{status}")
    @ResponseBody
    public ResponseEntity<List<Appointment>> getAppointmentsByStatus(@PathVariable AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByStatus(status));
    }

    // Hastanın GELECEK randevuları
    @GetMapping("/api/patient/{patientId}/upcoming")
    @ResponseBody
    public ResponseEntity<?> getUpcomingAppointmentsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getUpcomingAppointmentsByPatient(patientId));
    }

    // Tarih aralığına göre randevular
    @GetMapping("/api/date-range")
    @ResponseBody
    public ResponseEntity<List<Appointment>> getAppointmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDateRange(start, end));
    }

    // Yeni randevu oluştur
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createAppointment(@RequestBody Map<String, Object> appointmentData) {
        try {
            Long patientId = Long.valueOf(appointmentData.get("patientId").toString());
            Long doctorId = Long.valueOf(appointmentData.get("doctorId").toString());
            LocalDateTime date = LocalDateTime.parse(appointmentData.get("appointmentDate").toString());

            Appointment appointment = appointmentService.createAppointment(patientId, doctorId, date);

            if (appointmentData.containsKey("notes")) appointment.setNotes(appointmentData.get("notes").toString());
            if (appointmentData.containsKey("complaints")) appointment.setComplaints(appointmentData.get("complaints").toString());

            return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.saveAppointment(appointment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Randevu GÜNCELLE
    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> updateAppointment(@PathVariable Long id, @RequestBody Appointment appointment) {
        try {
            return ResponseEntity.ok(appointmentService.updateAppointment(id, appointment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Randevu İPTAL ET
    @PutMapping("/api/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(appointmentService.cancelAppointment(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Randevu TAMAMLA
    @PutMapping("/api/{id}/complete")
    @ResponseBody
    public ResponseEntity<?> completeAppointment(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(appointmentService.completeAppointment(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Randevu SİL
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id) {
        try {
            appointmentService.deleteAppointment(id);
            return ResponseEntity.ok("Randevu silindi");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}