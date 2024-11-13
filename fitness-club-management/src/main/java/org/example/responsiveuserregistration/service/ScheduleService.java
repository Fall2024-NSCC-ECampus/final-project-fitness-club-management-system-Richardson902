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


@Service
public class ScheduleService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    public void scheduleSession(Long trainerId, List<Long> userIds, LocalDate date, LocalTime startTime, LocalTime endTime) {

        Schedule schedule = new Schedule(trainerId, date, startTime, endTime);
        Set<User> participants = getParticipantsFromUserIds(userIds, schedule);
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

    public void markAttendance(Long scheduleId, List<Long> absentUserIds) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
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

    private Set<User> getParticipantsFromUserIds(List<Long> userIds, Schedule schedule) {
        Set<User> participants = new HashSet<>();
        for (Long userId : userIds) {
            User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
            user.getSchedules().add(schedule);
            participants.add(user);
        }
        return participants;
    }

}
