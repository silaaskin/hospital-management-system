package com.hospital.management.repository;

import com.hospital.management.model.Patient;
import com.hospital.management.model.TriageRecord;
import com.hospital.management.model.TriageRecord.TriagePriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TriageRecordRepository extends JpaRepository<TriageRecord, Long> {

    // Hastaya göre triaj kayıtlarını listele
    List<TriageRecord> findByPatient(Patient patient);

    // Öncelik seviyesine göre triaj kayıtlarını listele
    List<TriageRecord> findByPriority(TriagePriority priority);

    // Öncelik seviyesine göre sırala (acil olanlar önce)
    @Query("SELECT t FROM TriageRecord t WHERE t.triageDate >= :startDate " +
            "ORDER BY CASE t.priority " +
            "WHEN 'CRITICAL' THEN 1 " +
            "WHEN 'URGENT' THEN 2 " +
            "WHEN 'SEMI_URGENT' THEN 3 " +
            "WHEN 'NON_URGENT' THEN 4 " +
            "WHEN 'ROUTINE' THEN 5 END, t.triageDate ASC")
    List<TriageRecord> findAllOrderedByPriority(@Param("startDate") LocalDateTime startDate);

    // Belirli tarih aralığındaki triaj kayıtlarını listele
    List<TriageRecord> findByTriageDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Hastanın en son triaj kaydını getir
    @Query("SELECT t FROM TriageRecord t WHERE t.patient = :patient " +
            "ORDER BY t.triageDate DESC")
    List<TriageRecord> findLatestByPatient(@Param("patient") Patient patient);

    // Bugünkü acil triaj kayıtlarını listele
    @Query("SELECT t FROM TriageRecord t WHERE t.priority IN ('CRITICAL', 'URGENT') " +
            "AND t.triageDate >= :startOfDay AND t.triageDate < :endOfDay " +
            "ORDER BY t.priority ASC, t.triageDate ASC")
    List<TriageRecord> findTodayUrgentCases(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);
}