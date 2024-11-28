package org.example.fitnessclubmanagement.service;

import org.example.fitnessclubmanagement.exceptions.UserNotFoundException;
import org.example.fitnessclubmanagement.model.Schedule;
import org.example.fitnessclubmanagement.model.User;
import org.example.fitnessclubmanagement.repository.ScheduleRepository;
import org.example.fitnessclubmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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
     * Retrieves participants from user IDs and associates them with a schedule session.
     *
     * @param userIds the list of user IDs
     * @param schedule the schedule session
     * @return the set of participants
     * @throws UserNotFoundException if a user is not found
     */
    private Set<User> getParticipantsFromUserIds(List<Long> userIds, Schedule schedule) {
        Set<User> participants = new HashSet<>();
        for (Long userId : userIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            user.getSchedules().add(schedule); // add session to user's schedule - needed for their relationship
            participants.add(user); // add user to participants of the schedule session
        }
        return participants;
    }

    /**
     * Schedules a session with a trainer and participants.
     *
     * @param trainerId the ID of the trainer
     * @param userIds the list of user IDs
     * @param date the date of the session
     * @param startTime the start time of the session
     * @param endTime the end time of the session
     */
    public void scheduleSession(Long trainerId, List<Long> userIds, LocalDate date, LocalTime startTime, LocalTime endTime) {
        Schedule schedule = new Schedule(trainerId, date, startTime, endTime);
        userIds.remove(trainerId); // remove trainer from participants since they are not a participant
        schedule.setParticipants(getParticipantsFromUserIds(userIds, schedule));
        scheduleRepository.save(schedule);
    }

    /**
     * Updates an existing schedule.
     *
     * @param scheduleId the ID of the schedule
     * @param startTime the new start time
     * @param endTime the new end time
     * @param trainerId the new trainer ID
     * @param userIds the new list of user IDs
     */
    public void updateSchedule(Long scheduleId, LocalTime startTime, LocalTime endTime, Long trainerId, List<Long> userIds) {
        Schedule schedule = findById(scheduleId);

        // Update start and end times
        if (startTime != null && endTime != null) {
            updateScheduleTimes(schedule, startTime, endTime);
        }

        //Update trainer and participants
        if (trainerId != null) {
            updateScheduleTrainer(schedule, trainerId, userIds);
        } else if (userIds != null) {
            updateScheduleParticipants(schedule, userIds);
        }

        // Save updated schedule
        scheduleRepository.save(schedule);
    }

    /**
     * Updates the start and end times of a schedule.
     *
     * @param schedule the schedule to update
     * @param startTime the new start time
     * @param endTime the new end time
     */
    private void updateScheduleTimes(Schedule schedule, LocalTime startTime, LocalTime endTime) {
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
    }

    /**
     * Updates the trainer and participants of a schedule.
     *
     * @param schedule the schedule to update
     * @param trainerId the new trainer ID
     * @param userIds the new list of user IDs
     */
    private void updateScheduleTrainer(Schedule schedule, Long trainerId, List<Long> userIds) {
        schedule.setTrainerId(trainerId);

        // If null, use existing participants from session
        if (userIds == null) {
            userIds = schedule.getParticipants().stream()
                    .map(User::getUserId)
                    .collect(Collectors.toList());
        }

        // Create a mutable list to remove the trainer from the participants
        // for the chance the new trainer is also a participant
        userIds = new ArrayList<>(userIds);
        userIds.remove(trainerId);

        schedule.setParticipants(getParticipantsFromUserIds(userIds, schedule));
    }

    /**
     * Updates the participants of a schedule.
     *
     * @param schedule the schedule to update
     * @param userIds the new list of user IDs
     */
    private void updateScheduleParticipants(Schedule schedule, List<Long> userIds) {
        schedule.setParticipants(getParticipantsFromUserIds(userIds, schedule));
    }

    /**
     * Marks the attendance of participants in a schedule.
     *
     * @param scheduleId the ID of the schedule
     * @param absentUserIds the list of user IDs who are absent
     */
    public void markAttendance(Long scheduleId, List<Long> absentUserIds) {
        Schedule schedule = findById(scheduleId);
        Set<User> participants = schedule.getParticipants();
        Set<User> absentUsers = new HashSet<>();

        for (User participant : participants) {
            if (!absentUserIds.contains(participant.getUserId())) {
                absentUsers.add(participant); // add user to absent list if not marked present
            }
        }
        schedule.setAbsentUsers(absentUsers);
        scheduleRepository.save(schedule);
    }

    /**
     * Retrieves all schedules.
     *
     * @return the list of schedules
     */
    public List<Schedule> getAllSchedules() {
        List<Schedule> schedules = scheduleRepository.findAllOrderedByDateAndTime();

        // Fixes an ordering issue with the table data in the view
        // now the data is sorted by name properly
        for (Schedule schedule : schedules) {
            schedule.setParticipants(new LinkedHashSet<>(userRepository
                    .findParticipantsByScheduleID(schedule.getScheduleId()))); // ensures data is sorted by name before passing to the view
            schedule.setAbsentUsers(new LinkedHashSet<>(userRepository
                    .findAbsentUsersByScheduleID(schedule.getScheduleId())));
        }
        return schedules;
    }

    /**
     * Retrieves schedules for a specific user.
     *
     * @param user the user
     * @return the list of schedules
     */
    public List<Schedule> getSchedulesForUser(User user) {
        List<Schedule> schedules;
        if (user.getRoles().contains("ADMIN")) {
            schedules = getAllSchedules(); // admin can see all schedules
        } else {
            // user can see schedules they are a participant in or are the trainer for
            schedules = scheduleRepository.findByParticipantsContaining(user);
            schedules.addAll(scheduleRepository.findByTrainerId(user.getUserId()));
        }

        schedules.forEach(this::setScheduleDetails); // for each schedule, pass to setScheduleDetails

        return schedules;
    }

    /**
     * Deletes a schedule.
     *
     * @param scheduleId the ID of the schedule
     */
    public void deleteSchedule(Long scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }

    /**
     * Finds a schedule by its ID.
     *
     * @param scheduleId the ID of the schedule
     * @return the schedule
     * @throws IllegalArgumentException if the schedule is not found
     */
    public Schedule findById(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
        setScheduleDetails(schedule);
        return schedule;
    }

    /**
     * Sets the details of a schedule, including the trainer name and participants.
     *
     * @param schedule the schedule to set details for
     * @throws IllegalArgumentException if the trainer is not found
     */
    private void setScheduleDetails(Schedule schedule) {
        User trainer = userRepository.findById(schedule.getTrainerId())
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));
        schedule.setTrainerName(trainer.getUsername());

        List<User> sortedParticipants = userRepository.findParticipantsByScheduleID(schedule.getScheduleId());
        schedule.setParticipants(new LinkedHashSet<>(sortedParticipants));
    }

}
