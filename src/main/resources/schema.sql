
CREATE TABLE IF NOT EXISTS doctors (
    id bigint NOT NULL AUTO_INCREMENT,
    first_name varchar(255) NOT NULL,
    last_name varchar(255) NOT NULL,
    specialization varchar(255) NOT NULL,
    department varchar(255),
    username varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (username)
    ) ENGINE=InnoDB //


CREATE TABLE IF NOT EXISTS patients (
    id bigint NOT NULL AUTO_INCREMENT,
    tc_no varchar(11) NOT NULL,
    first_name varchar(255) NOT NULL,
    last_name varchar(255) NOT NULL,
    password varchar(255),
    birth_date date,
    phone varchar(255),
    email varchar(255),
    blood_type varchar(255),
    address TEXT,
    PRIMARY KEY (id),
    UNIQUE (tc_no)
    ) ENGINE=InnoDB //


CREATE TABLE IF NOT EXISTS secretaries (
    id bigint NOT NULL AUTO_INCREMENT,
    first_name varchar(255) NOT NULL,
    last_name varchar(255) NOT NULL,
    username varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (username)
    ) ENGINE=InnoDB //


CREATE TABLE IF NOT EXISTS appointments (
    id bigint NOT NULL AUTO_INCREMENT,
    patient_id bigint NOT NULL,
    doctor_id bigint NOT NULL,
    appointment_date datetime(6) NOT NULL,
    status enum ('CANCELLED','COMPLETED','NO_SHOW','SCHEDULED') NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    FOREIGN KEY (patient_id) REFERENCES patients (id)
    ) ENGINE=InnoDB //


CREATE TABLE IF NOT EXISTS prescriptions (
    id bigint NOT NULL AUTO_INCREMENT,
    appointment_id bigint,
    patient_id bigint NOT NULL,
    doctor_id bigint NOT NULL,
    prescription_text TEXT NOT NULL,
    dosage TEXT,
    instructions TEXT,
    duration_days integer,
    notes TEXT,
    created_date datetime(6) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (appointment_id) REFERENCES appointments (id),
    FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    FOREIGN KEY (patient_id) REFERENCES patients (id)
    ) ENGINE=InnoDB //


CREATE TABLE IF NOT EXISTS triage_records (
    id bigint NOT NULL AUTO_INCREMENT,
    patient_id bigint NOT NULL,
    triage_date datetime(6) NOT NULL,
    priority enum ('CRITICAL','NON_URGENT','ROUTINE','SEMI_URGENT','URGENT') NOT NULL,
    status varchar(255) NOT NULL,
    symptoms TEXT,
    blood_pressure_systolic integer,
    blood_pressure_diastolic integer,
    heart_rate integer,
    temperature float(53),
    PRIMARY KEY (id),
    FOREIGN KEY (patient_id) REFERENCES patients (id)
    ) ENGINE=InnoDB //