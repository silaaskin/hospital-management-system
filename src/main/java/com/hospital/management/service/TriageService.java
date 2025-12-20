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

    // Tüm triaj kayıtlarını listele
    public List<TriageRecord> getAllTriageRecords() {
        return triageRecordRepository.findAll();
    }

    // ID'ye göre triaj kaydı bul
    public Optional<TriageRecord> getTriageRecordById(Long id) {
        return triageRecordRepository.findById(id);
    }

    // Hastaya göre triaj kayıtlarını listele
    public List<TriageRecord> getTriageRecordsByPatient(Long patientId) {
        Patient patient = patientService.getPatientById(patientId)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı! ID: " + patientId));
        return triageRecordRepository.findByPatient(patient);
    }

    // Öncelik seviyesine göre triaj kayıtlarını listele
    public List<TriageRecord> getTriageRecordsByPriority(TriagePriority priority) {
        return triageRecordRepository.findByPriority(priority);
    }

    // Yeni triaj kaydı oluştur
    public TriageRecord createTriageRecord(Long patientId, TriageRecord triageRecord) {
        Patient patient = patientService.getPatientById(patientId)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı! ID: " + patientId));

        triageRecord.setPatient(patient);
        triageRecord.setTriageDate(LocalDateTime.now());

        // Otomatik önceliklendirme
        if (triageRecord.getPriority() == null) {
            triageRecord.setPriority(calculatePriority(triageRecord));
        }

        return triageRecordRepository.save(triageRecord);
    }

    // Triaj kaydı kaydet/güncelle
    public TriageRecord saveTriageRecord(TriageRecord triageRecord) {
        return triageRecordRepository.save(triageRecord);
    }

    // Triaj kaydı güncelle
    public TriageRecord updateTriageRecord(Long id, TriageRecord triageDetails) {
        TriageRecord triageRecord = triageRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Triaj kaydı bulunamadı! ID: " + id));

        triageRecord.setPriority(triageDetails.getPriority());
        triageRecord.setSymptoms(triageDetails.getSymptoms());
        triageRecord.setBloodPressureSystolic(triageDetails.getBloodPressureSystolic());
        triageRecord.setBloodPressureDiastolic(triageDetails.getBloodPressureDiastolic());
        triageRecord.setTemperature(triageDetails.getTemperature());
        triageRecord.setHeartRate(triageDetails.getHeartRate());
        triageRecord.setRespiratoryRate(triageDetails.getRespiratoryRate());
        triageRecord.setNotes(triageDetails.getNotes());
        triageRecord.setPerformedBy(triageDetails.getPerformedBy());

        return triageRecordRepository.save(triageRecord);
    }

    // Triaj kaydı sil
    public void deleteTriageRecord(Long id) {
        TriageRecord triageRecord = triageRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Triaj kaydı bulunamadı! ID: " + id));
        triageRecordRepository.delete(triageRecord);
    }

    // Öncelik sırasına göre triaj kayıtlarını getir
    public List<TriageRecord> getAllTriageRecordsOrderedByPriority() {
        return triageRecordRepository.findAllOrderedByPriority(LocalDateTime.now().minusDays(1));
    }

    // Bugünkü acil vakaları getir
    public List<TriageRecord> getTodayUrgentCases() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return triageRecordRepository.findTodayUrgentCases(startOfDay, endOfDay);
    }

    // Hastanın son triaj kaydını getir
    public Optional<TriageRecord> getLatestTriageRecordByPatient(Long patientId) {
        Patient patient = patientService.getPatientById(patientId)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı! ID: " + patientId));
        List<TriageRecord> records = triageRecordRepository.findLatestByPatient(patient);
        return records.isEmpty() ? Optional.empty() : Optional.of(records.get(0));
    }

    // Otomatik önceliklendirme algoritması
    private TriagePriority calculatePriority(TriageRecord record) {
        int score = 0;

        // Ateş kontrolü
        if (record.getTemperature() != null) {
            if (record.getTemperature() >= 39.5) score += 3;
            else if (record.getTemperature() >= 38.5) score += 2;
            else if (record.getTemperature() >= 37.5) score += 1;
        }

        // Kalp atış hızı kontrolü
        if (record.getHeartRate() != null) {
            if (record.getHeartRate() >= 120 || record.getHeartRate() <= 50) score += 3;
            else if (record.getHeartRate() >= 100 || record.getHeartRate() <= 60) score += 2;
        }

        // Solunum hızı kontrolü
        if (record.getRespiratoryRate() != null) {
            if (record.getRespiratoryRate() >= 30 || record.getRespiratoryRate() <= 10) score += 3;
            else if (record.getRespiratoryRate() >= 24 || record.getRespiratoryRate() <= 12) score += 2;
        }

        // Tansiyon kontrolü
        if (record.getBloodPressureSystolic() != null) {
            if (record.getBloodPressureSystolic() >= 180 || record.getBloodPressureSystolic() <= 90) score += 3;
            else if (record.getBloodPressureSystolic() >= 160 || record.getBloodPressureSystolic() <= 100) score += 2;
        }

        // Puan değerlendirmesi
        if (score >= 9) return TriagePriority.CRITICAL;
        else if (score >= 6) return TriagePriority.URGENT;
        else if (score >= 3) return TriagePriority.SEMI_URGENT;
        else if (score >= 1) return TriagePriority.NON_URGENT;
        else return TriagePriority.ROUTINE;
    }
}