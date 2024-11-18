package org.example.responsiveuserregistration.controller;

import jakarta.validation.Valid;
import org.example.responsiveuserregistration.model.Schedule;
import org.example.responsiveuserregistration.model.User;
import org.example.responsiveuserregistration.payload.*;
import org.example.responsiveuserregistration.service.ScheduleService;
import org.example.responsiveuserregistration.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
public class ScheduleController {

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
        return "newsession";
    }

    @GetMapping("/schedule/view")
    public String viewSchedule(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        List<Schedule> schedules = scheduleService.getSchedulesForUser(user);

        model.addAttribute("schedules", schedules);
        return "viewschedule";
    }

    @GetMapping("/schedule/edit/{scheduleId}/times")
    public String showEditTimePage(@PathVariable Long scheduleId, Model model) {
        Schedule schedule = scheduleService.findById(scheduleId);
        model.addAttribute("schedule", schedule);
        return "editsessiontimes";
    }

    @GetMapping("/schedule/edit/{scheduleId}/participants")
    public String showEditParticipantsPage(@PathVariable Long scheduleId, Model model) {
        List<User> users = userService.getAllUsers();
        Schedule schedule = scheduleService.findById(scheduleId);
        model.addAttribute("users", users);
        model.addAttribute("schedule", schedule);
        return "editsessionparticipants";
    }

    @GetMapping("/schedule/edit/{scheduleId}/trainer")
    public String showEditTrainerPage(@PathVariable Long scheduleId, Model model) {
        List<User> trainers = userService.getUsersByRole("TRAINER");
        Schedule schedule = scheduleService.findById(scheduleId);
        model.addAttribute("trainers", trainers);
        model.addAttribute("schedule", schedule);
        return "editsessiontrainer";
    }

    @GetMapping("/schedule/mark-attendance/{scheduleId}")
    public String showMarkAttendancePage(@PathVariable Long scheduleId, Model model) {
        Schedule schedule = scheduleService.findById(scheduleId);
        model.addAttribute("schedule", schedule);
        return "editattendance";
    }

    @PostMapping("/schedule")
    public String scheduleSession(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                  @RequestParam("trainerId") Long trainerId,
                                  @RequestParam("userIds") List<Long> userIds,
                                  @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                                  @RequestParam("endTime")@DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        scheduleService.scheduleSession(trainerId, userIds, date, startTime, endTime);
        return "redirect:/schedule/edit";
    }

    @PostMapping("/schedule/edit/{scheduleId}/time")
    public String updateClassTime(@PathVariable("scheduleId") Long scheduleId, @Valid @ModelAttribute("request") TimeRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return "editsessiontimes";
        }
        scheduleService.updateClassTime(scheduleId, request.getStartTime(), request.getEndTime());
        return "redirect:/schedule/view";
    }

    @PostMapping("/schedule/edit/{scheduleId}/trainer")
    public String updateClassTrainer(@PathVariable("scheduleId") Long scheduleId, @RequestParam Long trainerId) {
        scheduleService.updateClassTrainer(scheduleId, trainerId);
        return "redirect:/schedule/view";
    }

    @PostMapping("/schedule/edit/{scheduleId}/participants")
    public String updateScheduleParticipants(@PathVariable("scheduleId") Long scheduleId, @RequestParam List<Long> userIds) {
        scheduleService.updateScheduleParticipants(scheduleId, userIds);
        return "redirect:/schedule/view";
    }

    @PostMapping("/schedule/mark-attendance/{scheduleId}")
    public String markAttendance(@PathVariable("scheduleId") Long scheduleId, @RequestParam("userIds") List<Long> userIds) {
        scheduleService.markAttendance(scheduleId, userIds);
        return "redirect:/schedule/view";
    }
}
