package com.hospital.management.controller;

import com.hospital.management.model.TriageRecord;
import com.hospital.management.model.TriageRecord.TriagePriority;
import com.hospital.management.service.TriageService;
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
@RequestMapping("/triage")
@CrossOrigin(origins = "*")
public class TriageController {

    @Autowired
    private TriageService triageService;

    // ==========================================
    //          1. GÜVENLİ GÖRÜNÜM (VIEW)
    // ==========================================

    @GetMapping("/view")
    public String showTriageDashboard(Model model, HttpSession session) {
        // GÜVENLİK KONTROLÜ: Kimlik kontrolü yap
        String userType = (String) session.getAttribute("userType");

        // Sadece DOKTOR görebilir. Hasta veya giriş yapmamış kişi giremez.
        if (!"DOCTOR".equals(userType)) {
            return "redirect:/dashboard";
        }

        // Öncelik sırasına göre (Kırmızı -> Sarı -> Yeşil) listele
        List<TriageRecord> records = triageService.getAllTriageRecordsOrderedByPriority();
        model.addAttribute("triageRecords", records);
        model.addAttribute("pageTitle", "Acil Durum / Triaj Paneli");

        return "triage-list"; // templates/triage-list.html
    }

    // ==========================================
    //          2. API METODLARI (JSON)
    // ==========================================

    // Tüm kayıtları getir
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<TriageRecord>> getAllTriageRecords() {
        return ResponseEntity.ok(triageService.getAllTriageRecords());
    }

    // ID'ye göre getir
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getTriageRecordById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(triageService.getTriageRecordById(id)
                    .orElseThrow(() -> new RuntimeException("Triaj kaydı bulunamadı!")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Belirli bir hastanın triaj kayıtlarını getir
    @GetMapping("/api/patient/{patientId}")
    @ResponseBody
    public ResponseEntity<?> getTriageRecordsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(triageService.getTriageRecordsByPatient(patientId));
    }

    // Önceliğe göre filtrele (CRITICAL, URGENT, NORMAL)
    @GetMapping("/api/priority/{priority}")
    @ResponseBody
    public ResponseEntity<List<TriageRecord>> getTriageRecordsByPriority(@PathVariable TriagePriority priority) {
        return ResponseEntity.ok(triageService.getTriageRecordsByPriority(priority));
    }

    // Sadece BUGÜNKÜ acil vakaları getir
    @GetMapping("/api/urgent/today")
    @ResponseBody
    public ResponseEntity<List<TriageRecord>> getTodayUrgentCases() {
        return ResponseEntity.ok(triageService.getTodayUrgentCases());
    }

    // Yeni triaj kaydı oluştur
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createTriageRecord(@RequestBody Map<String, Object> triageData) {
        try {
            Long patientId = Long.valueOf(triageData.get("patientId").toString());
            TriageRecord record = new TriageRecord();

            // Verileri güvenli şekilde al
            if (triageData.containsKey("symptoms")) record.setSymptoms(triageData.get("symptoms").toString());
            if (triageData.containsKey("priority")) record.setPriority(TriagePriority.valueOf(triageData.get("priority").toString()));
            if (triageData.containsKey("temperature")) record.setTemperature(Double.valueOf(triageData.get("temperature").toString()));
            if (triageData.containsKey("heartRate")) record.setHeartRate(Integer.valueOf(triageData.get("heartRate").toString()));
            if (triageData.containsKey("bloodPressureSystolic")) record.setBloodPressureSystolic(Integer.valueOf(triageData.get("bloodPressureSystolic").toString()));
            if (triageData.containsKey("bloodPressureDiastolic")) record.setBloodPressureDiastolic(Integer.valueOf(triageData.get("bloodPressureDiastolic").toString()));
            if (triageData.containsKey("notes")) record.setNotes(triageData.get("notes").toString());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(triageService.createTriageRecord(patientId, record));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Triaj kaydını GÜNCELLE
    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> updateTriageRecord(@PathVariable Long id, @RequestBody TriageRecord record) {
        try {
            return ResponseEntity.ok(triageService.updateTriageRecord(id, record));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Triaj kaydını SİL
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteTriageRecord(@PathVariable Long id) {
        try {
            triageService.deleteTriageRecord(id);
            return ResponseEntity.ok("Triaj kaydı silindi");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}