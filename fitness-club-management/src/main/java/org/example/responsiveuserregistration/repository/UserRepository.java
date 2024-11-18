package org.example.responsiveuserregistration.repository;

import org.example.responsiveuserregistration.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsernameOrEmail(String username, String email);
    Optional<User> findByUsername(String username);
    List<User> findByRolesContaining(String role);

    @Query("SELECT u FROM User u ORDER BY u.username")
    List<User> findAllOrderedByUsername();

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = 'TRAINER' ORDER BY u.username")
    List<User> findUserByRoleOrderedByUsername(@Param("role") String role);

    @Query("SELECT u FROM User u JOIN u.schedules s WHERE s.scheduleId = :scheduleId ORDER BY u.username")
    List<User> findParticipantsByScheduleID(@Param("scheduleId") Long scheduleId);

    @Query("SELECT u FROM User u JOIN Schedule s ON s.scheduleId = :scheduleId AND u MEMBER OF s.absentUsers ORDER BY u.username")
    List<User> findAbsentUsersByScheduleID(@Param("scheduleId") Long scheduleId);

}
