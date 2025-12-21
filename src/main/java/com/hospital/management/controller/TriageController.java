package com.hospital.management.controller;

import com.hospital.management.model.Patient;
import com.hospital.management.model.TriageRecord;
import com.hospital.management.service.PatientService;
import com.hospital.management.service.TriageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/triage")
@CrossOrigin(origins = "*")
public class TriageController {

    @Autowired
    private TriageService triageService;
    @Autowired
    private PatientService patientService;

    @GetMapping("/view")
    public String showTriageDashboard(Model model, HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        if (!"DOCTOR".equals(userType) && !"SECRETARY".equals(userType)) return "redirect:/dashboard";
        List<TriageRecord> records = triageService.getAllTriageRecordsOrderedByPriority();
        model.addAttribute("triageRecords", records);
        model.addAttribute("pageTitle", "Acil Servis / Triaj Monitörü");
        return "triage-list";
    }

    @GetMapping("/add")
    public String showAddTriagePage(HttpSession session) {
        if (!"SECRETARY".equals(session.getAttribute("userType"))) return "redirect:/dashboard";
        return "triage-add";
    }

    @PostMapping("/add")
    public String addTriageRecord(@RequestParam("tcNo") String tcNo, @ModelAttribute TriageRecord triageRecord) {
        Optional<Patient> patientOpt = patientService.getPatientByTcNo(tcNo);
        if (patientOpt.isEmpty()) return "redirect:/triage/add?error=HastaBulunamadi";
        triageService.createTriageRecord(patientOpt.get().getId(), triageRecord);
        return "redirect:/triage/view?success=Created";
    }

    @GetMapping("/edit/{id}")
    public String showEditTriagePage(@PathVariable Long id, Model model, HttpSession session) {
        if (!"SECRETARY".equals(session.getAttribute("userType"))) return "redirect:/dashboard";
        TriageRecord record = triageService.getTriageRecordById(id).orElseThrow();
        model.addAttribute("record", record);
        return "triage-edit";
    }

    @PostMapping("/update")
    public String updateTriage(@ModelAttribute TriageRecord triageRecord) {
        triageService.updateTriageRecord(triageRecord.getId(), triageRecord);
        return "redirect:/triage/view?success=Updated";
    }

    @PutMapping("/api/{id}/complete")
    @ResponseBody
    public ResponseEntity<?> completeMuayene(@PathVariable Long id) {
        try {
            triageService.completeTriageMuayene(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}