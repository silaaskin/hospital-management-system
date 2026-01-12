-- Varsa eski tetikleyicileri siliyoruz
DROP TRIGGER IF EXISTS trg_after_appointment_insert //
DROP TRIGGER IF EXISTS trg_after_appointment_delete //

-- Yeni randevu eklendiğinde çalışır
CREATE TRIGGER trg_after_appointment_insert
    AFTER INSERT ON appointments
    FOR EACH ROW
BEGIN
    -- Bu tetikleyici, yeni bir randevu eklendiğinde
    -- başka bir tabloya log atabilir veya durumu kontrol edebilir.
    -- Örneğin: Randevu eklendiğinde hastanın son randevu tarihini bir yere kaydedebilir.
    SET @last_inserted_id = NEW.id;
END //

-- Randevu durumu güncellendiğinde (Örn: SCHEDULED -> COMPLETED)
DROP TRIGGER IF EXISTS trg_before_appointment_status_update //
CREATE TRIGGER trg_before_appointment_status_update
    BEFORE UPDATE ON appointments
    FOR EACH ROW
BEGIN
    -- Eğer randevu tamamlandı olarak işaretlenirse,
    -- otomatik olarak işlem tarihini notlara ekleyebiliriz.
    IF OLD.status <> NEW.status AND NEW.status = 'COMPLETED' THEN
        -- Not alanını otomatik güncelleme örneği
        -- SET NEW.notes = CONCAT(IFNULL(OLD.notes, ''), ' [Muayene Tamamlandı: ', NOW(), ']');
        SET NEW.status = 'COMPLETED';
END IF;
END //