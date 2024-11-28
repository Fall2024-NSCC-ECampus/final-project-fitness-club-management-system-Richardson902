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
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for managing schedules.
 */
@Controller
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private UserService userService;

    /**
     * Displays the schedule editing page.
     *
     * @param model the model to hold attributes for the view
     * @return the name of the view to render
     */
    @GetMapping("/schedule/edit")
    public String showSchedulePage(Model model) {
        List<User> trainers = userService.getUsersByRole("TRAINER");
        List<User> users = userService.getAllUsers();
        model.addAttribute("trainers", trainers);
        model.addAttribute("users", users);
        model.addAttribute("trainerId", null); // God I hate thymeleaf
        return "newsession";
    }

    /**
     * Displays the schedule viewing page.
     *
     * @param userDetails the authenticated user's details
     * @param model the model to hold attributes for the view
     * @return the name of the view to render
     */
    @GetMapping("/schedule/view")
    public String viewSchedule(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        List<Schedule> schedules = scheduleService.getSchedulesForUser(user);

        model.addAttribute("schedules", schedules);
        return "viewschedule";
    }

    /**
     * Displays the page for editing scheule times.
     *
     * @param scheduleId the ID of the schedule to be edited
     * @param model the model to hold attributes for the view
     * @return the name of the view to render
     */
    @GetMapping("/schedule/edit/{scheduleId}/times")
    public String showEditTimePage(@PathVariable Long scheduleId, Model model) {
        Schedule schedule = scheduleService.findById(scheduleId);
        model.addAttribute("schedule", schedule);
        return "editsessiontimes";
    }

    /**
     * Displays the page for editing schedule participants.
     *
     * @param scheduleId the ID of the schedule to be edited
     * @param model the model to hold attributes for the view
     * @return the name of the view to render
     */
    @GetMapping("/schedule/edit/{scheduleId}/participants")
    public String showEditParticipantsPage(@PathVariable Long scheduleId, Model model) {
        List<User> users = userService.getAllUsers();
        Schedule schedule = scheduleService.findById(scheduleId);
        model.addAttribute("users", users);
        model.addAttribute("schedule", schedule);
        return "editsessionparticipants";
    }

    /**
     * Displays the page for editing the schedule trainer.
     *
     * @param scheduleId the ID of the schedule to be edited
     * @param model the model to hold attributes for the view
     * @return the name of the view to render
     */
    @GetMapping("/schedule/edit/{scheduleId}/trainer")
    public String showEditTrainerPage(@PathVariable Long scheduleId, Model model) {
        List<User> trainers = userService.getUsersByRole("TRAINER");
        Schedule schedule = scheduleService.findById(scheduleId);
        model.addAttribute("trainers", trainers);
        model.addAttribute("schedule", schedule);
        return "editsessiontrainer";
    }

    /**
     * Displays the page for marking attendance.
     *
     * @param scheduleId the ID of the schedule to be edited
     * @param model the model to hold attributes for the view
     * @return the name of the view to render
     */
    @GetMapping("/schedule/mark-attendance/{scheduleId}")
    public String showMarkAttendancePage(@PathVariable Long scheduleId, Model model) {
        Schedule schedule = scheduleService.findById(scheduleId);
        model.addAttribute("schedule", schedule);
        return "editattendance";
    }

    /**
     * Schedules a new session.
     *
     * @param date the date of the session
     * @param trainerId the ID of the trainer
     * @param userIds the IDs of the participants
     * @param startTime the start time of the session
     * @param endTime the end time of the session
     * @return a redirect to the schedule viewing page
     */
    @PostMapping("/schedule")
    public String scheduleSession(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                  @RequestParam("trainerId") Long trainerId,
                                  @RequestParam("userIds") List<Long> userIds,
                                  @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                                  @RequestParam("endTime")@DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        scheduleService.scheduleSession(trainerId, userIds, date, startTime, endTime);
        return "redirect:/schedule/view";
    }

    /**
     * Updates the time of an existing schedule.
     *
     * @param scheduleId the ID of the schedule to be updated
     * @param request the request containing the new start and end times
     * @param result the binding result
     * @return a redirect to the schedule viewing page
     */
    @PostMapping("/schedule/edit/{scheduleId}/time")
    public String updateClassTime(@PathVariable("scheduleId") Long scheduleId, @Valid @ModelAttribute("request") TimeRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return "editsessiontimes";
        }
        scheduleService.updateSchedule(scheduleId, request.getStartTime(), request.getEndTime(), null, null);
        return "redirect:/schedule/view";
    }

    /**
     * Updates the trainer of an existing schedule.
     *
     * @param scheduleId the ID of the schedule to be updated
     * @param trainerId the ID of the new trainer
     * @return a redirect to the schedule viewing page
     */
    @PostMapping("/schedule/edit/{scheduleId}/trainer")
    public String updateClassTrainer(@PathVariable("scheduleId") Long scheduleId, @RequestParam Long trainerId) {
        scheduleService.updateSchedule(scheduleId, null, null, trainerId, null);
        return "redirect:/schedule/view";
    }

    /**
     * Updates the participants of an existing schedule.
     *
     * @param scheduleId the ID of the schedule to be updated
     * @param userIds the IDs of the new participants
     * @return a redirect to the schedule viewing page
     */
    @PostMapping("/schedule/edit/{scheduleId}/participants")
    public String updateScheduleParticipants(@PathVariable("scheduleId") Long scheduleId, @RequestParam List<Long> userIds) {
        scheduleService.updateSchedule(scheduleId, null, null, null, userIds);
        return "redirect:/schedule/view";
    }

    /**
     * Marks attendance for a schedule.
     *
     * @param scheduleId the ID of the schedule
     * @param userIds the IDs of the users who attended
     * @return a redirect to the schedule viewing page
     */
    @PostMapping("/schedule/mark-attendance/{scheduleId}")
    public String markAttendance(@PathVariable("scheduleId") Long scheduleId, @RequestParam(value = "userIds", required = false) List<Long> userIds) {
        if (userIds == null) {
            userIds = new ArrayList<>(); // ugly hack to avoid non-populated list when everyone absent from session
        }
        scheduleService.markAttendance(scheduleId, userIds);
        return "redirect:/schedule/view";
    }

    /**
     * Deletes a schedule.
     *
     * @param scheduleId the ID of the schedule to be deleted
     * @return a redirect to the schedule viewing page
     */
    @PostMapping("/schedule/delete/{scheduleId}")
    public String deleteSchedule(@PathVariable("scheduleId") Long scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return "redirect:/schedule/view";
    }
}
