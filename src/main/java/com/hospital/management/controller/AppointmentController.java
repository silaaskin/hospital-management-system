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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
            List<Appointment> myAppointments = appointmentService.getAppointmentsByPatient(userId);
            model.addAttribute("appointments", myAppointments);
            model.addAttribute("pageTitle", "Randevularım");
        } else if ("DOCTOR".equals(userType)) {
            List<Appointment> myDoctorAppointments = appointmentService.getAppointmentsByDoctor(userId);
            model.addAttribute("appointments", myDoctorAppointments);
            model.addAttribute("pageTitle", "Randevu Listem");
        } else {
            return "redirect:/";
        }

        return "appointments-list";
    }

    // ==========================================
    //          2. API METODLARI (JSON)
    // ==========================================

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

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

    @GetMapping("/api/patient/{patientId}")
    @ResponseBody
    public ResponseEntity<?> getAppointmentsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(patientId));
    }

    @GetMapping("/api/doctor/{doctorId}")
    @ResponseBody
    public ResponseEntity<?> getAppointmentsByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(doctorId));
    }

    @GetMapping("/api/status/{status}")
    @ResponseBody
    public ResponseEntity<List<Appointment>> getAppointmentsByStatus(@PathVariable AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByStatus(status));
    }

    @GetMapping("/api/patient/{patientId}/upcoming")
    @ResponseBody
    public ResponseEntity<?> getUpcomingAppointmentsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getUpcomingAppointmentsByPatient(patientId));
    }

    @GetMapping("/api/date-range")
    @ResponseBody
    public ResponseEntity<List<Appointment>> getAppointmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(appointmentService.getAppointmentsInDateRange(start, end));
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createAppointment(@RequestBody Map<String, Object> appointmentData) {
        try {
            Long patientId = Long.valueOf(appointmentData.get("patientId").toString());
            Long doctorId = Long.valueOf(appointmentData.get("doctorId").toString());
            LocalDateTime date = LocalDateTime.parse(appointmentData.get("appointmentDate").toString());

            Appointment appointment = appointmentService.createAppointment(patientId, doctorId, date);

            // 'notes', 'complaints' ve 'diagnosis' alanlarına dair tüm set işlemleri kaldırıldı.

            return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.saveAppointment(appointment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> updateAppointment(@PathVariable Long id, @RequestBody Appointment appointment) {
        try {
            return ResponseEntity.ok(appointmentService.updateAppointment(id, appointment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/api/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(appointmentService.cancelAppointment(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/api/{id}/complete")
    @ResponseBody
    public ResponseEntity<?> completeAppointment(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(appointmentService.completeAppointment(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

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
    @GetMapping("/list")
    public String showAppointments(Model model) {
        LocalDateTime now = LocalDateTime.now();

        // Bugünün sonu (23:59:59)
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        // Yarının başlangıcı ve bitişi
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime tomorrowEnd = LocalDate.now().plusDays(1).atTime(LocalTime.MAX);

        // 1. Bugünün randevuları (Şu andan gün sonuna kadar olanlar)
        model.addAttribute("todayApps", appointmentService.getAppointmentsInDateRange(now, todayEnd));

        // 2. Yarının randevuları (Tüm gün)
        model.addAttribute("tomorrowApps", appointmentService.getAppointmentsInDateRange(tomorrowStart, tomorrowEnd));

        // 3. Bekleyen (Gelecekteki tüm) randevular
        model.addAttribute("pendingApps", appointmentService.getAppointmentsByStatus(AppointmentStatus.SCHEDULED));

        return "appointments-list";
    }

}