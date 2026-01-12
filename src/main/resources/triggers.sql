DROP TRIGGER IF EXISTS trg_after_appointment_insert //
CREATE TRIGGER trg_after_appointment_insert
    AFTER INSERT ON appointments
    FOR EACH ROW
BEGIN
    INSERT INTO appointment_logs (appointment_id, action_type, details)
    VALUES (NEW.id, 'INSERT', CONCAT('Yeni randevu oluşturuldu. Hasta ID: ', NEW.patient_id));
END //

DROP TRIGGER IF EXISTS trg_before_appointment_status_update //
CREATE TRIGGER trg_before_appointment_status_update
    BEFORE UPDATE ON appointments
    FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status AND NEW.status = 'COMPLETED' THEN
        SET NEW.status = 'COMPLETED';
END IF;
END //

DROP TRIGGER IF EXISTS trg_before_appointment_data_change //
CREATE TRIGGER trg_before_appointment_data_change
    BEFORE UPDATE ON appointments
    FOR EACH ROW
BEGIN
    IF OLD.appointment_date <> NEW.appointment_date THEN
        INSERT INTO appointment_logs (appointment_id, action_type, details)
        VALUES (OLD.id, 'UPDATE_DATE', CONCAT('Eski: ', OLD.appointment_date, ' -> Yeni: ', NEW.appointment_date));
END IF;
END //

DROP TRIGGER IF EXISTS trg_after_appointment_delete //
CREATE TRIGGER trg_after_appointment_delete
    AFTER DELETE ON appointments
    FOR EACH ROW
BEGIN
    INSERT INTO appointment_logs (appointment_id, action_type, details)
    VALUES (OLD.id, 'DELETE', 'Randevu silindi.');
END //