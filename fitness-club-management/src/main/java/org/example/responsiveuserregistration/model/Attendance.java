package org.example.responsiveuserregistration.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    @EmbeddedId
    private AttendanceId attendanceId;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private User user;

    @ManyToOne
    @MapsId("scheduleId")
    @JoinColumn(name = "schedule_id", referencedColumnName = "scheduleId")
    private Schedule schedule;

    @Column(name = "attended")
    private boolean attended;
}
