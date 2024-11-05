package org.example.responsiveuserregistration.service;

import org.example.responsiveuserregistration.model.Schedule;
import org.example.responsiveuserregistration.model.User;
import org.example.responsiveuserregistration.repository.ScheduleRepository;
import org.example.responsiveuserregistration.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    public void scheduleSession(Long trainerId, List<Long> userIds, Date date, LocalTime startTime, LocalTime endTime) {
        logger.info("Scheduling session with trainerId: {}, userIds: {}, date: {}, startTime: {}, endTime: {}",
                trainerId, userIds, date, startTime, endTime);

        Schedule schedule = new Schedule();
        schedule.setDate(date);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setTrainerId(trainerId);

        logger.info("Attempting to add participants to the schedule");
        Set<User> participants = new HashSet<>();
        for (Long userId : userIds) {
            logger.info("Adding participant with ID: {}", userId);
            User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
            participants.add(user);
        }
        schedule.setParticipants(participants);
        scheduleRepository.save(schedule);

        for (User Participant : participants) {
            logger.info ("Added participant with ID: {}", Participant.getUserId());
        }

        logger.info("List of participants: {}", participants);

        logger.info("Session scheduled successfully");
    }

    public List<Schedule> getAllSchedules() {
        List<Schedule> schedules = scheduleRepository.findAll();
        for (Schedule schedule : schedules) {
            logger.info("Schedule ID: {}", schedule.getScheduleId());
            for (User participant : schedule.getParticipants()) {
                logger.info("Participant ID: {}", participant.getUserId());
            }
        }
        return schedules;
    }
}
