package com.hospital.management.service;

import com.hospital.management.model.Patient;
import com.hospital.management.model.TriageRecord;
import com.hospital.management.model.TriageRecord.TriagePriority;
import com.hospital.management.repository.TriageRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TriageService {

    @Autowired
    private TriageRecordRepository triageRecordRepository;
    @Autowired
    private PatientService patientService;

    public List<TriageRecord> getAllTriageRecords() { return triageRecordRepository.findAll(); }
    public Optional<TriageRecord> getTriageRecordById(Long id) { return triageRecordRepository.findById(id); }

    public List<TriageRecord> getTriageRecordsByPatient(Long patientId) {
        Patient patient = patientService.getPatientById(patientId)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı!"));
        return triageRecordRepository.findByPatient(patient);
    }

    public TriageRecord createTriageRecord(Long patientId, TriageRecord triageRecord) {
        Patient patient = patientService.getPatientById(patientId).orElseThrow();
        triageRecord.setPatient(patient);
        triageRecord.setTriageDate(LocalDateTime.now());
        triageRecord.setStatus("WAITING");
        triageRecord.setPriority(calculatePriority(triageRecord));
        return triageRecordRepository.save(triageRecord);
    }

    public TriageRecord updateTriageRecord(Long id, TriageRecord details) {
        TriageRecord record = triageRecordRepository.findById(id).orElseThrow();
        record.setTemperature(details.getTemperature());
        record.setHeartRate(details.getHeartRate());
        record.setBloodPressureSystolic(details.getBloodPressureSystolic());
        record.setBloodPressureDiastolic(details.getBloodPressureDiastolic());
        record.setSymptoms(details.getSymptoms());
        record.setPriority(calculatePriority(record));
        return triageRecordRepository.save(record);
    }

    public TriageRecord completeTriageMuayene(Long id) {
        TriageRecord record = triageRecordRepository.findById(id).orElseThrow();
        record.setStatus("TREATED");
        return triageRecordRepository.save(record);
    }

    public List<TriageRecord> getAllTriageRecordsOrderedByPriority() {
        return triageRecordRepository.findAllOrderedByPriority(LocalDateTime.now().minusDays(1));
    }

    private TriagePriority calculatePriority(TriageRecord record) {
        int score = 0;

        if (record.getTemperature() != null) {
            if (record.getTemperature() >= 39.5 || record.getTemperature() <= 35.0) score += 4;
            else if (record.getTemperature() >= 38.5) score += 2;
            else if (record.getTemperature() >= 37.5) score += 1;
        }

        if (record.getHeartRate() != null) {
            if (record.getHeartRate() >= 130 || record.getHeartRate() <= 40) score += 4;
            else if (record.getHeartRate() >= 110 || record.getHeartRate() <= 50) score += 2;
            else if (record.getHeartRate() >= 100) score += 1;
        }

        if (record.getBloodPressureSystolic() != null) {
            if (record.getBloodPressureSystolic() >= 180 || record.getBloodPressureSystolic() <= 80) score += 4;
            else if (record.getBloodPressureSystolic() >= 160 || record.getBloodPressureSystolic() <= 90) score += 2;
            else if (record.getBloodPressureSystolic() >= 140) score += 1;
        }

        if (record.getBloodPressureDiastolic() != null) {
            if (record.getBloodPressureDiastolic() >= 110 || record.getBloodPressureDiastolic() <= 50) score += 3;
            else if (record.getBloodPressureDiastolic() >= 100) score += 1;
        }

        if (score >= 10) return TriagePriority.CRITICAL;
        else if (score >= 6) return TriagePriority.URGENT;
        else if (score >= 3) return TriagePriority.SEMI_URGENT;
        else return TriagePriority.NON_URGENT;
    }
}