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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    // ==========================================
    //          1. GÖRÜNÜM METOTLARI (VIEW)
    // ==========================================

    @GetMapping("/view")
    public String showUserAppointments(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String userType = (String) session.getAttribute("userType");

        if (userId == null) return "redirect:/login";

        List<Appointment> allUserApps;
        if ("PATIENT".equals(userType)) {
            allUserApps = appointmentService.getAppointmentsByPatient(userId);
            model.addAttribute("pageTitle", "Randevularım");
        } else if ("DOCTOR".equals(userType)) {
            allUserApps = appointmentService.getAppointmentsByDoctor(userId);
            model.addAttribute("pageTitle", "Randevu Listem");
        } else {
            return "redirect:/";
        }

        prepareAppointmentLists(model, allUserApps);
        return "appointments-list";
    }

    @GetMapping("/list")
    public String showAllAppointments(Model model) {
        List<Appointment> allApps = appointmentService.getAllAppointments();
        model.addAttribute("pageTitle", "Tüm Randevular");
        prepareAppointmentLists(model, allApps);
        return "appointments-list";
    }

    private void prepareAppointmentLists(Model model, List<Appointment> sourceList) {
        if (sourceList == null) sourceList = new ArrayList<>();

        // Tarihi null olanları koruma amaçlı filtrele
        List<Appointment> validApps = sourceList.stream()
                .filter(a -> a.getAppointmentDate() != null)
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime tomorrowEnd = LocalDate.now().plusDays(1).atTime(LocalTime.MAX);

        // BUGÜN (Sadece bekleyenler)
        model.addAttribute("todayApps", validApps.stream()
                .filter(a -> a.getAppointmentDate().isAfter(now.minusMinutes(1)) && a.getAppointmentDate().isBefore(todayEnd))
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .collect(Collectors.toList()));

        // YARIN (Sadece bekleyenler)
        model.addAttribute("tomorrowApps", validApps.stream()
                .filter(a -> a.getAppointmentDate().isAfter(tomorrowStart) && a.getAppointmentDate().isBefore(tomorrowEnd))
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .collect(Collectors.toList()));

        // TÜM BEKLEYENLER
        model.addAttribute("pendingApps", validApps.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .collect(Collectors.toList()));

        // TAMAMLANANLAR (Geçmiş Randevular) - YENİ FİLTRE
        model.addAttribute("completedApps", validApps.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .collect(Collectors.toList()));

        // İPTAL EDİLENLER - YENİ FİLTRE
        model.addAttribute("cancelledApps", validApps.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CANCELLED)
                .collect(Collectors.toList()));

        model.addAttribute("appointments", validApps);
    }
    // ==========================================
    //          2. API METODLARI (JSON)
    // ==========================================

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<Appointment>> getAllAppointmentsApi() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createAppointment(@RequestBody Map<String, Object> appointmentData) {
        try {
            Long patientId = Long.valueOf(appointmentData.get("patientId").toString());
            Long doctorId = Long.valueOf(appointmentData.get("doctorId").toString());
            LocalDateTime date = LocalDateTime.parse(appointmentData.get("appointmentDate").toString());
            Appointment appointment = appointmentService.createAppointment(patientId, doctorId, date);
            return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.saveAppointment(appointment));
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
}