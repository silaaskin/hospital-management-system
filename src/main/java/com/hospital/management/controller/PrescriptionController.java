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

    @GetMapping("/view")
    public String showAllPrescriptions(Model model, HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        Long userId = (Long) session.getAttribute("userId");

        System.out.println("=== REÇETE GÖRÜNTÜLEME ===");
        System.out.println("User Type: " + userType);
        System.out.println("User ID: " + userId);

        if (userId == null) {
            System.out.println("HATA: userId null, yönlendiriliyor...");
            return "redirect:/";
        }

        try {
            if ("PATIENT".equals(userType)) {
                List<Prescription> prescriptions = prescriptionService.getPrescriptionsByPatient(userId);
                System.out.println("Hasta reçeteleri yüklendi: " + prescriptions.size() + " adet");

                for (Prescription p : prescriptions) {
                    System.out.println("  - Reçete ID: " + p.getId() + ", Doktor: " +
                            p.getDoctor().getFirstName() + ", İçerik: " +
                            (p.getPrescriptionText() != null ? p.getPrescriptionText().substring(0, Math.min(50, p.getPrescriptionText().length())) : "null"));
                }

                model.addAttribute("prescriptions", prescriptions);
                model.addAttribute("pageTitle", "Reçetelerim");
            } else if ("DOCTOR".equals(userType)) {
                List<Prescription> prescriptions = prescriptionService.getPrescriptionsByDoctor(userId);
                System.out.println("Doktor reçeteleri yüklendi: " + prescriptions.size() + " adet");

                for (Prescription p : prescriptions) {
                    System.out.println("  - Reçete ID: " + p.getId() + ", Hasta: " +
                            p.getPatient().getFirstName() + ", İçerik: " +
                            (p.getPrescriptionText() != null ? p.getPrescriptionText().substring(0, Math.min(50, p.getPrescriptionText().length())) : "null"));
                }

                model.addAttribute("prescriptions", prescriptions);
                model.addAttribute("pageTitle", "Yazdığım Reçeteler");
            } else {
                System.out.println("HATA: Geçersiz kullanıcı tipi: " + userType);
                return "redirect:/dashboard";
            }
        } catch (Exception e) {
            System.err.println("REÇETE YÜKLEME HATASI: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Reçeteler yüklenirken hata oluştu: " + e.getMessage());
            model.addAttribute("prescriptions", List.of());
        }

        System.out.println("Sayfa render ediliyor: prescriptions-list");
        return "prescriptions-list";
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createPrescription(@RequestBody Map<String, Object> prescriptionData) {
        try {
            System.out.println("=== YENİ REÇETE OLUŞTURULUYOR ===");

            Long appointmentId = Long.valueOf(prescriptionData.get("appointmentId").toString());
            Long doctorId = Long.valueOf(prescriptionData.get("doctorId").toString());
            String medications = prescriptionData.get("medications").toString();

            System.out.println("Appointment ID: " + appointmentId);
            System.out.println("Doctor ID: " + doctorId);
            System.out.println("Medications: " + medications);

            Appointment appointment = appointmentService.getAppointmentById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Randevu bulunamadı!"));
            Doctor doctor = doctorService.getDoctorById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Doktor bulunamadı!"));

            Prescription prescription = new Prescription();
            prescription.setAppointment(appointment);
            prescription.setPatient(appointment.getPatient());
            prescription.setDoctor(doctor);
            prescription.setPrescriptionText(medications);
            prescription.setCreatedDate(LocalDateTime.now());

            if (prescriptionData.containsKey("notes")) {
                prescription.setNotes(prescriptionData.get("notes").toString());
            }

            Prescription saved = prescriptionService.savePrescription(prescription);
            System.out.println("Reçete başarıyla kaydedildi! ID: " + saved.getId());

            appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
            appointmentService.saveAppointment(appointment);
            System.out.println("Randevu durumu COMPLETED olarak güncellendi");

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "Reçete başarıyla kaydedildi."));

        } catch (Exception e) {
            System.err.println("REÇETE OLUŞTURMA HATASI: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/triage")
    @ResponseBody
    public ResponseEntity<?> createTriagePrescription(@RequestBody Map<String, Object> data) {
        try {
            System.out.println("=== TRİAJ REÇETESİ OLUŞTURULUYOR ===");
            System.out.println("Gelen data: " + data);

            // Null kontrolleri
            if (!data.containsKey("triageId")) {
                throw new RuntimeException("triageId eksik!");
            }
            if (!data.containsKey("doctorId")) {
                throw new RuntimeException("doctorId eksik!");
            }
            if (!data.containsKey("medications")) {
                throw new RuntimeException("medications eksik!");
            }

            Long triageId = Long.valueOf(data.get("triageId").toString());
            Long doctorId = Long.valueOf(data.get("doctorId").toString());
            String medications = data.get("medications").toString();

            System.out.println("Triaj ID: " + triageId);
            System.out.println("Doktor ID: " + doctorId);
            System.out.println("İlaçlar: " + medications);

            TriageRecord triage = triageService.getTriageRecordById(triageId)
                    .orElseThrow(() -> new RuntimeException("Triaj kaydı bulunamadı! ID: " + triageId));

            System.out.println("Triaj kaydı bulundu: Hasta ID: " + triage.getPatient().getId());

            Doctor doctor = doctorService.getDoctorById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Doktor bulunamadı! ID: " + doctorId));

            System.out.println("Doktor bulundu: " + doctor.getFirstName() + " " + doctor.getLastName());

            Prescription p = new Prescription();
            p.setDoctor(doctor);
            p.setPatient(triage.getPatient());
            p.setAppointment(null); // Acil servis, randevusuz
            p.setPrescriptionText(medications);
            p.setCreatedDate(LocalDateTime.now());

            if (data.containsKey("notes") && data.get("notes") != null) {
                p.setNotes(data.get("notes").toString());
            }

            Prescription saved = prescriptionService.savePrescription(p);
            System.out.println("✅ Triaj reçetesi kaydedildi! ID: " + saved.getId());

            triageService.completeTriageMuayene(triageId);
            System.out.println("✅ Triaj muayenesi tamamlandı");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Acil reçetesi başarıyla oluşturuldu.",
                    "prescriptionId", saved.getId()
            ));

        } catch (NumberFormatException e) {
            System.err.println("❌ SAYISAL DÖNÜŞÜM HATASI: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Geçersiz ID formatı: " + e.getMessage()));

        } catch (RuntimeException e) {
            System.err.println("❌ İŞLEM HATASI: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            System.err.println("❌ BİLİNMEYEN HATA: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Sunucu hatası: " + e.getMessage()));
        }
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deletePrescription(@PathVariable Long id) {
        try {
            prescriptionService.deletePrescription(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Reçete silindi"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}