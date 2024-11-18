package org.example.responsiveuserregistration.repository;

import org.example.responsiveuserregistration.model.Schedule;
import org.example.responsiveuserregistration.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByParticipantsContaining(User user);
    List<Schedule> findByTrainerId(Long trainerId);

    @Query("SELECT s FROM Schedule s ORDER BY s.date, s.startTime")
    List<Schedule> findAllOrderedByDateAndTime();
}
