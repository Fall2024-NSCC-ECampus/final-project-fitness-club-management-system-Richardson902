package org.example.responsiveuserregistration.model;


import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceId implements Serializable {
    private Long userId;
    private Long scheduleId;
}
