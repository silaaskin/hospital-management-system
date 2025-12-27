package com.hospital.management.controller;

import com.hospital.management.model.Appointment;
import com.hospital.management.model.Appointment.AppointmentStatus;
import com.hospital.management.service.AppointmentService;
import com.hospital.management.repository.AppointmentRepository; //
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private AppointmentRepository appointmentRepository; //

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

    // MHRS tarzı saat doluluk kontrolü
    @GetMapping("/api/busy-slots")
    @ResponseBody
    public List<String> getBusySlots(@RequestParam Long doctorId, @RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime startOfDay = localDate.atStartOfDay();
        LocalDateTime endOfDay = localDate.atTime(LocalTime.MAX);

        List<Appointment> apps = appointmentRepository.findByAppointmentDateBetweenAndStatus(
                startOfDay, endOfDay, AppointmentStatus.SCHEDULED);

        return apps.stream()
                .filter(a -> a.getDoctor().getId().equals(doctorId))
                .map(a -> a.getAppointmentDate().toLocalTime().toString())
                .collect(Collectors.toList());
    }

    private void prepareAppointmentLists(Model model, List<Appointment> sourceList) {
        if (sourceList == null) sourceList = new ArrayList<>();
        List<Appointment> validApps = sourceList.stream()
                .filter(a -> a.getAppointmentDate() != null)
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        model.addAttribute("todayApps", validApps.stream()
                .filter(a -> a.getAppointmentDate().isAfter(now.minusMinutes(1)) && a.getAppointmentDate().isBefore(todayEnd))
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .collect(Collectors.toList()));

        model.addAttribute("pendingApps", validApps.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .collect(Collectors.toList()));

        model.addAttribute("completedApps", validApps.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .collect(Collectors.toList()));

        model.addAttribute("appointments", validApps);
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
}