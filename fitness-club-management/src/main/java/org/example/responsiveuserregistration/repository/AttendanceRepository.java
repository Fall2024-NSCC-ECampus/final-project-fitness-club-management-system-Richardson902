package org.example.responsiveuserregistration.repository;

import org.example.responsiveuserregistration.model.Attendance;
import org.example.responsiveuserregistration.model.AttendanceId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, AttendanceId> {
}
