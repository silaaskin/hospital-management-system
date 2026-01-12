package com.hospital.management.controller;

import com.hospital.management.model.Appointment;
import com.hospital.management.model.Appointment.AppointmentStatus;
import com.hospital.management.model.Prescription;
import com.hospital.management.model.Patient;
import com.hospital.management.service.AppointmentService;
import com.hospital.management.service.PrescriptionService;
import com.hospital.management.service.PatientService;
import com.hospital.management.repository.AppointmentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    @Autowired private AppointmentService appointmentService;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private PrescriptionService prescriptionService;
    @Autowired private PatientService patientService;

    @GetMapping("/view")
    public String showUserAppointments(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String userType = (String) session.getAttribute("userType");

        if (userId == null) return "redirect:/login";

        List<Appointment> allUserApps;
        if ("PATIENT".equals(userType)) {
            allUserApps = appointmentService.getAppointmentsByPatient(userId);
            model.addAttribute("pageTitle", "Randevularım");


            patientService.getPatientById(userId).ifPresent(patient -> {
                Integer upcomingCount = appointmentService.getUpcomingCountByTc(patient.getTcNo());
                model.addAttribute("upcomingCount", upcomingCount);
            });

        } else if ("DOCTOR".equals(userType)) {
            allUserApps = appointmentService.getAppointmentsByDoctor(userId);
            model.addAttribute("pageTitle", "Randevu Listem");
        } else {
            return "redirect:/";
        }

        prepareAppointmentLists(model, allUserApps);
        return "appointments-list";
    }

    @GetMapping("/api/doctor-appointments")
    @ResponseBody
    public ResponseEntity<?> getDoctorAppointments(HttpSession session) {
        Long doctorId = (Long) session.getAttribute("userId");
        if (doctorId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Doktor ID bulunamadı");

        List<Appointment> allApps = appointmentService.getAppointmentsByDoctor(doctorId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime tomorrowEnd = LocalDate.now().plusDays(1).atTime(LocalTime.MAX);

        Map<String, Object> response = new HashMap<>();

        response.put("today", allApps.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .filter(a -> a.getAppointmentDate() != null)
                .filter(a -> a.getAppointmentDate().isAfter(now.minusMinutes(1)) && a.getAppointmentDate().isBefore(todayEnd))
                .map(this::convertToAppointmentDTO).collect(Collectors.toList()));

        response.put("tomorrow", allApps.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .filter(a -> a.getAppointmentDate() != null)
                .filter(a -> a.getAppointmentDate().isAfter(tomorrowStart) && a.getAppointmentDate().isBefore(tomorrowEnd))
                .map(this::convertToAppointmentDTO).collect(Collectors.toList()));

        response.put("pending", allApps.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .map(this::convertToAppointmentDTO).collect(Collectors.toList()));

        response.put("completed", allApps.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .map(this::convertToAppointmentDTO).collect(Collectors.toList()));

        response.put("cancelled", allApps.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CANCELLED)
                .map(this::convertToAppointmentDTO).collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/{appointmentId}/complete")
    @ResponseBody
    public ResponseEntity<?> completeAppointment(@PathVariable Long appointmentId) {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new Exception("Randevu bulunamadı"));
            appointment.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.save(appointment);
            return ResponseEntity.ok(Map.of("success", true, "message", "Muayene tamamlandı"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/save-prescription")
    @ResponseBody
    public ResponseEntity<?> savePrescription(@RequestBody Map<String, Object> data) {
        try {
            Long appointmentId = Long.valueOf(data.get("appointmentId").toString());
            String prescriptionText = data.get("prescription").toString();
            String notes = data.getOrDefault("notes", "").toString();

            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new Exception("Randevu bulunamadı"));

            if (prescriptionService != null) {
                Prescription prescription = new Prescription();
                prescription.setAppointment(appointment);
                prescription.setPatient(appointment.getPatient());
                prescription.setDoctor(appointment.getDoctor());
                prescription.setPrescriptionText(prescriptionText);
                prescription.setNotes(notes);
                prescription.setCreatedDate(LocalDateTime.now());
                prescriptionService.savePrescription(prescription);
            }

            appointment.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.save(appointment);

            return ResponseEntity.ok(Map.of("success", true, "message", "Reçete kaydedildi"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/busy-slots")
    @ResponseBody
    public List<String> getBusySlots(@RequestParam Long doctorId, @RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        List<Appointment> apps = appointmentRepository.findByAppointmentDateBetweenAndStatus(
                localDate.atStartOfDay(), localDate.atTime(LocalTime.MAX), AppointmentStatus.SCHEDULED);

        return apps.stream()
                .filter(a -> a.getDoctor().getId().equals(doctorId))
                .map(a -> a.getAppointmentDate().toLocalTime().toString())
                .collect(Collectors.toList());
    }

    private Map<String, Object> convertToAppointmentDTO(Appointment app) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", app.getId());
        dto.put("appointmentDate", app.getAppointmentDate());
        dto.put("status", app.getStatus().name());
        dto.put("patientName", app.getPatient() != null ? app.getPatient().getFullName() : "Bilinmeyen");
        dto.put("patientTcNo", app.getPatient() != null ? app.getPatient().getTcNo() : "---");
        return dto;
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
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED).collect(Collectors.toList()));

        model.addAttribute("pendingApps", validApps.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED).collect(Collectors.toList()));

        model.addAttribute("completedApps", validApps.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).collect(Collectors.toList()));

        model.addAttribute("appointments", validApps);
    }


    @GetMapping("/api/doctor-stats")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> getDoctorStats(HttpSession session) {
        Long doctorId = (Long) session.getAttribute("userId");
        if (doctorId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Yetkisiz erişim");

        try {
            List<Object[]> result = appointmentRepository.getDoctorDashboardStats(doctorId);

            Map<String, Object> response = new HashMap<>();

            if (result != null && !result.isEmpty() && result.get(0) != null) {
                Object[] stats = result.get(0);
                response.put("todayCount", stats[0] != null ? stats[0].toString() : "0");
                response.put("tomorrowCount", stats[1] != null ? stats[1].toString() : "0");
                response.put("pendingCount", stats[2] != null ? stats[2].toString() : "0");
            } else {
                response.put("todayCount", "0");
                response.put("tomorrowCount", "0");
                response.put("pendingCount", "0");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Dashboard Stats Hatası: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("İstatistikler alınamadı.");
        }
    }
}