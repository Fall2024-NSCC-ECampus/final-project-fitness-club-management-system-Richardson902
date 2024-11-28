package org.example.fitnessclubmanagement.repository;

import org.example.fitnessclubmanagement.model.Schedule;
import org.example.fitnessclubmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository for managing Schedule entities.
 */
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /**
     * Finds all schedules that contain the given user as a participant.
     * @param user the user to search for
     * @return a list of schedules that contain the given user as a participant
     */
    List<Schedule> findByParticipantsContaining(User user);

    /**
     * Finds schedules by thr trainer's ID.
     *
     * @param trainerId the ID of the trainer to search for
     * @return a list of schedules with the given trainer's ID
     */
    List<Schedule> findByTrainerId(Long trainerId);

    /**
     * Finds all schedules ordered by date and start time.
     *
     * @return a list of all schedules ordered by date and start time
     */
    @Query("SELECT s FROM Schedule s ORDER BY s.date, s.startTime")
    List<Schedule> findAllOrderedByDateAndTime();
}
