package com.hospital.management.controller;

import com.hospital.management.model.Prescription;
import com.hospital.management.service.PrescriptionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/prescriptions")
@CrossOrigin(origins = "*")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;
    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> updatePrescription(@PathVariable Long id, @RequestBody Prescription details) {
        try {
            return ResponseEntity.ok(prescriptionService.updatePrescription(id, details));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ==========================================
    //          GÖRÜNÜM (VIEW)
    // ==========================================

    @GetMapping("/view")
    public String showAllPrescriptions(Model model, HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        Long userId = (Long) session.getAttribute("userId");

        // GÜVENLİK: Eğer giriş yapılmadıysa ana sayfaya at
        if (userId == null) return "redirect:/";

        if ("PATIENT".equals(userType)) {
            // HASTA İSE: Sadece kendi reçetelerini görsün
            List<Prescription> myPrescriptions = prescriptionService.getPrescriptionsByPatient(userId);
            model.addAttribute("prescriptions", myPrescriptions);
            model.addAttribute("pageTitle", "Reçetelerim");
        } else if ("DOCTOR".equals(userType)) {
            // DOKTOR İSE: Yazdığı veya tüm reçeteleri görsün
            // (Burada tümünü getiriyoruz, istersen sadece doktorun yazdıklarını getirebilirsin)
            List<Prescription> allPrescriptions = prescriptionService.getAllPrescriptions();
            model.addAttribute("prescriptions", allPrescriptions);
            model.addAttribute("pageTitle", "Yazılan Reçeteler (Doktor Paneli)");
        }

        return "prescriptions-list"; // HTML dosyasını aşağıda güncelleyeceğiz
    }

    // ==========================================
    //          API (KAYIT İŞLEMİ)
    // ==========================================

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createPrescription(@RequestBody Map<String, Object> prescriptionData) {
        try {
            // Frontend'den (Modal) gelen verileri alıyoruz
            Long appointmentId = Long.valueOf(prescriptionData.get("appointmentId").toString());
            Long doctorId = Long.valueOf(prescriptionData.get("doctorId").toString());

            Prescription prescription = new Prescription();

            // Verileri güvenli şekilde dolduruyoruz
            if (prescriptionData.get("medications") != null)
                prescription.setMedications(prescriptionData.get("medications").toString());

            if(prescriptionData.containsKey("dosage"))
                prescription.setDosage(prescriptionData.get("dosage").toString());

            if(prescriptionData.containsKey("instructions"))
                prescription.setInstructions(prescriptionData.get("instructions").toString());

            if(prescriptionData.containsKey("durationDays") && prescriptionData.get("durationDays") != "")
                prescription.setDurationDays(Integer.valueOf(prescriptionData.get("durationDays").toString()));

            if(prescriptionData.containsKey("notes"))
                prescription.setNotes(prescriptionData.get("notes").toString());

            // Servise gönderip kaydediyoruz -> Bu andan itibaren hasta reçeteyi görebilir!
            Prescription saved = prescriptionService.createPrescription(appointmentId, doctorId, prescription);

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Hata: " + e.getMessage());
        }
    }

    // Diğer API metodları (Get, Delete vs.) buraya eklenebilir
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<Prescription>> getAll() {
        return ResponseEntity.ok(prescriptionService.getAllPrescriptions());
    }

}