package org.example.responsiveuserregistration.service;

import org.example.responsiveuserregistration.model.Schedule;
import org.example.responsiveuserregistration.model.User;
import org.example.responsiveuserregistration.repository.ScheduleRepository;
import org.example.responsiveuserregistration.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ScheduleService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    public void scheduleSession(Long trainerId, List<Long> userIds, LocalDate date, LocalTime startTime, LocalTime endTime) {

        Schedule schedule = new Schedule();
        schedule.setTrainerId(trainerId);
        schedule.setDate(date);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);

        Set<User> participants = new HashSet<>();
        for (Long userId : userIds) {
            User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
            user.getSchedules().add(schedule);
            participants.add(user);
        }
        schedule.setParticipants(participants);
        scheduleRepository.save(schedule);
    }

    public void updateClassTime(Long scheduleId, LocalTime startTime, LocalTime endTime) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        scheduleRepository.save(schedule);
    }

    public void updateClassTrainer(Long scheduleId, Long trainerId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
        schedule.setTrainerId(trainerId);
        scheduleRepository.save(schedule);
    }

    public void updateScheduleParticipants(Long scheduleId, List<Long> userIds) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
        Set<User> participants = new HashSet<>();
        for (Long userId : userIds) {
            User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
            user.getSchedules().add(schedule);
            participants.add(user);
        }
        schedule.setParticipants(participants);
        scheduleRepository.save(schedule);
    }


    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    public Schedule findById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId).orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
    }

    public List<Schedule> findByParticipantsContaining(User user) {
        return scheduleRepository.findByParticipantsContaining(user);
    }

    public List<Schedule> getSchedulesForUser(User user) {
        if (user.getRoles().contains("ADMIN")) {
            return getAllSchedules();
        } else {
            return findByParticipantsContaining(user);
        }
    }
}
