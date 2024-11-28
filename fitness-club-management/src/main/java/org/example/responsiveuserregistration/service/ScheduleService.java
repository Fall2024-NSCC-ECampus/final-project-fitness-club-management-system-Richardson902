package org.example.responsiveuserregistration.service;

import org.example.responsiveuserregistration.model.Schedule;
import org.example.responsiveuserregistration.model.User;
import org.example.responsiveuserregistration.repository.ScheduleRepository;
import org.example.responsiveuserregistration.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

/**
 * Service class for managing schedules.
 */
@Service
public class ScheduleService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;


    /**
     * Retrieves participants from a list of user IDs and associates them with a schedule/
     *
     * @param userIds List of user IDs.
     * @param schedule The Schedule to associate thr users with.
     * @return A sorted set of users by name.
     */
    private Set<User> getParticipantsFromUserIds(List<Long> userIds, Schedule schedule) {
        Set<User> participants = new HashSet<>();
        for (Long userId : userIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            user.getSchedules().add(schedule);
            participants.add(user);
        }
        return participants;
    }

    /**
     * Schedules a new session.
     *
     * @param trainerId The ID of the trainer.
     * @param userIds List of user IDs to participate in the session.
     * @param date The date of the session
     * @param startTime The start time of the session.
     * @param endTime The end time of the session.
     */
    public void scheduleSession(Long trainerId, List<Long> userIds, LocalDate date, LocalTime startTime, LocalTime endTime) {
        Schedule schedule = new Schedule(trainerId, date, startTime, endTime);
        schedule.setParticipants(getParticipantsFromUserIds(userIds, schedule));
        scheduleRepository.save(schedule);
    }

    public void updateSchedule(Long scheduleId, LocalTime startTime, LocalTime endTime, Long trainerId, List<Long> userIds) {
        Schedule schedule = findById(scheduleId);
        if (startTime != null && endTime != null) {
            schedule.setStartTime(startTime);
            schedule.setEndTime(endTime);
        }
        if (trainerId != null) {
            schedule.setTrainerId(trainerId);
        }
        if (userIds != null) {
            schedule.setParticipants(getParticipantsFromUserIds(userIds, schedule));
        }
        scheduleRepository.save(schedule);
    }

    /**
     * Marks the attendance of participants for a session.
     *
     * @param scheduleId The ID of the schedule.
     * @param absentUserIds List of user IDs who are absent.
     */
    public void markAttendance(Long scheduleId, List<Long> absentUserIds) {
        Schedule schedule = findById(scheduleId);
        Set<User> participants = schedule.getParticipants();
        Set<User> absentUsers = new HashSet<>();

        for (User participant : participants) {
            if (!absentUserIds.contains(participant.getUserId())) {
                absentUsers.add(participant);
            }
        }
        schedule.setAbsentUsers(absentUsers);
        scheduleRepository.save(schedule);
    }


    /**
     * Retrieves all scheduled sessions.
     *
     * @return A list of all scheduled sessions.
     */
    public List<Schedule> getAllSchedules() {
        List<Schedule> schedules = scheduleRepository.findAllOrderedByDateAndTime();
        for (Schedule schedule : schedules) {
            schedule.setParticipants(new LinkedHashSet<>(userRepository
                    .findParticipantsByScheduleID(schedule.getScheduleId()))); // ensures data is sorted by name before passing to the view
            schedule.setAbsentUsers(new LinkedHashSet<>(userRepository
                    .findAbsentUsersByScheduleID(schedule.getScheduleId())));
        }
        return schedules;
    }

    /**
     * Retrieves schedule sessions for a user
     *
     * @param user The user to retrieve schedule sessions for.
     * @return A list of schedule sessions for the user.
     */
    public List<Schedule> getSchedulesForUser(User user) {
        List<Schedule> schedules;
        if (user.getRoles().contains("ADMIN")) {
            schedules = getAllSchedules();
        } else {
            schedules = scheduleRepository.findByParticipantsContaining(user);
            schedules.addAll(scheduleRepository.findByTrainerId(user.getUserId()));
        }

        for (Schedule schedule : schedules) {
            User trainer = userRepository.findById(schedule.getTrainerId())
                    .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));
            schedule.setTrainerName(trainer.getUsername());
        }

        return schedules;
    }

    public void deleteSchedule(Long scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }


    /**
     * Finds a scheduled session by its ID.
     *
     * @param scheduleId The ID of the schedule session to find.
     * @return the found schedule session.
     */
    public Schedule findById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId).orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
    }

    /**
     * Finds schedule sessions by a participant.
     *
     * @param user The user to find schedule sessions for.
     * @return A list of schedule sessions containing the user.
     */
    public List<Schedule> findByParticipantsContaining(User user) {
        return scheduleRepository.findByParticipantsContaining(user);
    }

}
