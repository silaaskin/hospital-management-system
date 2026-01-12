DROP PROCEDURE IF EXISTS GetPatientUpcomingAppointmentCount //

CREATE PROCEDURE GetPatientUpcomingAppointmentCount(IN p_tc VARCHAR(11), OUT p_count INT)
BEGIN
SELECT COUNT(*) INTO p_count
FROM appointments a
         JOIN patients p ON a.patient_id = p.id
WHERE p.tc_no = p_tc AND a.status = 'SCHEDULED';
END //

DROP PROCEDURE IF EXISTS GetPatientAppointments //

CREATE PROCEDURE GetPatientAppointments(IN p_patient_id BIGINT)
BEGIN
SELECT a.id, a.appointment_date, a.status, d.first_name AS doctor_first_name, d.last_name AS doctor_last_name
FROM appointments a
         JOIN doctors d ON a.doctor_id = d.id
WHERE a.patient_id = p_patient_id
ORDER BY a.appointment_date;
END //

DROP PROCEDURE IF EXISTS GetDoctorDashboardStats //

CREATE PROCEDURE GetDoctorDashboardStats(IN p_doctor_id BIGINT)
BEGIN
SELECT
    COUNT(CASE WHEN status = 'SCHEDULED' AND DATE(appointment_date) = CURDATE() THEN 1 END) as today_count,
    COUNT(CASE WHEN status = 'SCHEDULED' AND DATE(appointment_date) = DATE_ADD(CURDATE(), INTERVAL 1 DAY) THEN 1 END) as tomorrow_count,
    COUNT(CASE WHEN status = 'SCHEDULED' THEN 1 END) as total_pending,
    COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed_count
FROM appointments
WHERE doctor_id = p_doctor_id;
END //