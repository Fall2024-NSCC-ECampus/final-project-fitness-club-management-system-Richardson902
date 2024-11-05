package org.example.responsiveuserregistration.service;

import org.example.responsiveuserregistration.model.Attendance;
import org.example.responsiveuserregistration.model.AttendanceId;
import org.example.responsiveuserregistration.model.Schedule;
import org.example.responsiveuserregistration.repository.AttendanceRepository;
import org.example.responsiveuserregistration.repository.ScheduleRepository;
import org.example.responsiveuserregistration.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    public void scheduleSession(Long trainerId, List<Long> userIds, Date date, LocalTime startTime, LocalTime endTime) {
        logger.info("Scheduling session with trainerId: {}, userIds: {}, date: {}, startTime: {}, endTime: {}",
                trainerId, userIds, date, startTime, endTime);
        // Create a new schedule
        Schedule schedule = new Schedule();
        schedule.setDate(date);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setTrainerId(trainerId);
        scheduleRepository.save(schedule);

        for (Long userId : userIds) {
            // Create an attendance for each user
            AttendanceId attendanceId = new AttendanceId(userId, schedule.getScheduleId());
            Attendance attendance = new Attendance(attendanceId, userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found")), schedule, false);
            attendanceRepository.save(attendance);
        }

        logger.info("Session scheduled successfully");
    }

    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }
}
