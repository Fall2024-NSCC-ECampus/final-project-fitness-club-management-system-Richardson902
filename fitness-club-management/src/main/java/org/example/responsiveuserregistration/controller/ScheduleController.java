package org.example.responsiveuserregistration.controller;

import org.example.responsiveuserregistration.model.Schedule;
import org.example.responsiveuserregistration.model.User;
import org.example.responsiveuserregistration.service.ScheduleService;
import org.example.responsiveuserregistration.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class ScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private UserService userService;

    @GetMapping("/schedule/edit")
    public String showSchedulePage(Model model) {
        List<User> trainers = userService.getUsersByRole("TRAINER");
        List<User> users = userService.getAllUsers();
        model.addAttribute("trainers", trainers);
        model.addAttribute("users", users);
        return "schedule";
    }

    @GetMapping("/schedule/view")
    public String viewSchedule(Model model) {
        List<Schedule> schedules = scheduleService.getAllSchedules();
        model.addAttribute("schedules", schedules);
        return "viewschedule";
    }

    @PostMapping("/schedule")
    public String scheduleSession(@RequestParam Long trainerId,
                                  @RequestParam List<Long> userIds,
                                  @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
                                  @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
                                  @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime endTime) {
        logger.info("Received request to schedule session with trainerId: {}, userIds: {}, date: {}, startTime: {}, endTime: {}",
                trainerId, userIds, date, startTime, endTime);
        scheduleService.scheduleSession(trainerId, userIds, date, startTime, endTime);
        return "redirect:/schedule/edit";
    }
}
