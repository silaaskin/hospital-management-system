package com.hospital.management.controller;

import com.hospital.management.model.TriageRecord;
import com.hospital.management.model.TriageRecord.TriagePriority;
import com.hospital.management.service.TriageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller; // Değiştirildi
import org.springframework.ui.Model; // Eklendi
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller // View desteği için @Controller yaptık
@RequestMapping("/triage") // Prefix sadeleştirildi
@CrossOrigin(origins = "*")
public class TriageController {

    @Autowired
    private TriageService triageService;

    // ==========================================
    //          VIEW (HTML) DÖNDÜREN METODLAR
    // ==========================================

    @GetMapping("/view") // http://localhost:8080/triage/view
    public String showTriageDashboard(Model model) {
        List<TriageRecord> records = triageService.getAllTriageRecordsOrderedByPriority();
        model.addAttribute("triageRecords", records);
        model.addAttribute("pageTitle", "Triaj Takip Paneli (Öncelik Sıralı)");
        return "triage-list"; // templates/triage-list.html
    }

    @GetMapping("/view/urgent") // http://localhost:8080/triage/view/urgent
    public String showUrgentCases(Model model) {
        List<TriageRecord> urgentRecords = triageService.getTodayUrgentCases();
        model.addAttribute("triageRecords", urgentRecords);
        model.addAttribute("pageTitle", "Bugünkü Acil Vakalar");
        return "triage-list";
    }

    // ==========================================
    //          API (JSON) DÖNDÜREN METODLAR
    // ==========================================

    @GetMapping
    @ResponseBody
    public ResponseEntity<List<TriageRecord>> getAllTriageRecords() {
        return ResponseEntity.ok(triageService.getAllTriageRecords());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> getTriageRecordById(@PathVariable Long id) {
        try {
            TriageRecord record = triageService.getTriageRecordById(id)
                    .orElseThrow(() -> new RuntimeException("Triaj kaydı bulunamadı! ID: " + id));
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/api/patient/{patientId}")
    @ResponseBody
    public ResponseEntity<?> getTriageRecordsByPatient(@PathVariable Long patientId) {
        try {
            return ResponseEntity.ok(triageService.getTriageRecordsByPatient(patientId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> createTriageRecord(@RequestBody Map<String, Object> triageData) {
        try {
            Long patientId = Long.valueOf(triageData.get("patientId").toString());
            TriageRecord record = new TriageRecord();

            if (triageData.containsKey("priority")) {
                record.setPriority(TriagePriority.valueOf(triageData.get("priority").toString()));
            }
            if (triageData.containsKey("symptoms")) record.setSymptoms(triageData.get("symptoms").toString());
            if (triageData.containsKey("temperature")) record.setTemperature(Double.valueOf(triageData.get("temperature").toString()));
            // ... diğer alanlar ...

            TriageRecord savedRecord = triageService.createTriageRecord(patientId, record);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRecord);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteTriageRecord(@PathVariable Long id) {
        try {
            triageService.deleteTriageRecord(id);
            return ResponseEntity.ok("Triaj kaydı başarıyla silindi!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}