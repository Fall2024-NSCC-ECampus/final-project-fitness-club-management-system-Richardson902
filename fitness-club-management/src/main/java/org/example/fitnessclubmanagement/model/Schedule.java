package org.example.fitnessclubmanagement.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Schedule entity
 */
@Entity
@Table(name="schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "participants")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "trainer_id", nullable = false)
    private Long trainerId;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<User> participants;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<User> absentUsers = new HashSet<>();

    @Transient
    private String trainerName;

    public Schedule(Long trainerId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.trainerId = trainerId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
