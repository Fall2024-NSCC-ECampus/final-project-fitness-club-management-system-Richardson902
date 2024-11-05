package org.example.responsiveuserregistration.repository;

import org.example.responsiveuserregistration.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
}
