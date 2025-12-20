package com.hospital.management.controller;

import com.hospital.management.model.Appointment;
import com.hospital.management.model.Appointment.AppointmentStatus;
import com.hospital.management.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller; // View için Controller şart
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller // Sınıf düzeyinde @RestController yerine @Controller
@RequestMapping("/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    // ==========================================
    //          VIEW (HTML) DÖNDÜREN METODLAR
    // ==========================================

    @GetMapping("/view") // http://localhost:8080/appointments/view
    public String showAllAppointments(Model model) {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        model.addAttribute("appointments", appointments);
        model.addAttribute("pageTitle", "Tüm Randevular");
        return "appointments-list"; // templates/appointments-list.html
    }

    @GetMapping("/view/{id}")
    public String showAppointmentDetail(@PathVariable Long id, Model model) {
        Appointment appointment = appointmentService.getAppointmentById(id)
                .orElseThrow(() -> new RuntimeException("Bulunamadı"));
        model.addAttribute("appointment", appointment);
        return "appointment-detail"; // templates/appointment-detail.html
    }

    // ==========================================
    //          API (JSON) DÖNDÜREN METODLAR
    // ==========================================

    @GetMapping
    @ResponseBody // Veri döndürmek için her metoda ekledik
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> getAppointmentById(@PathVariable Long id) {
        try {
            Appointment appointment = appointmentService.getAppointmentById(id)
                    .orElseThrow(() -> new RuntimeException("Randevu bulunamadı! ID: " + id));
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/patient/{patientId}")
    @ResponseBody
    public ResponseEntity<?> getAppointmentsByPatient(@PathVariable Long patientId) {
        try {
            return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(patientId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/doctor/{doctorId}")
    @ResponseBody
    public ResponseEntity<?> getAppointmentsByDoctor(@PathVariable Long doctorId) {
        try {
            return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(doctorId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/status/{status}")
    @ResponseBody
    public ResponseEntity<List<Appointment>> getAppointmentsByStatus(@PathVariable AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByStatus(status));
    }

    @GetMapping("/patient/{patientId}/upcoming")
    @ResponseBody
    public ResponseEntity<?> getUpcomingAppointmentsByPatient(@PathVariable Long patientId) {
        try {
            return ResponseEntity.ok(appointmentService.getUpcomingAppointmentsByPatient(patientId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/date-range")
    @ResponseBody
    public ResponseEntity<List<Appointment>> getAppointmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDateRange(start, end));
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> createAppointment(@RequestBody Map<String, Object> appointmentData) {
        try {
            Long patientId = Long.valueOf(appointmentData.get("patientId").toString());
            Long doctorId = Long.valueOf(appointmentData.get("doctorId").toString());
            LocalDateTime appointmentDate = LocalDateTime.parse(appointmentData.get("appointmentDate").toString());

            Appointment appointment = appointmentService.createAppointment(patientId, doctorId, appointmentDate);
            if (appointmentData.containsKey("notes")) appointment.setNotes(appointmentData.get("notes").toString());

            return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.saveAppointment(appointment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> updateAppointment(@PathVariable Long id, @RequestBody Appointment appointment) {
        try {
            return ResponseEntity.ok(appointmentService.updateAppointment(id, appointment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(appointmentService.cancelAppointment(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id) {
        try {
            appointmentService.deleteAppointment(id);
            return ResponseEntity.ok("Randevu başarıyla silindi!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}