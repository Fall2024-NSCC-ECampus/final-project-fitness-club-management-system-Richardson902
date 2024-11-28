package org.example.responsiveuserregistration.repository;

import org.example.responsiveuserregistration.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing User entities.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Checks if a user with the given username or email already exists.
     *
     * @param username the username to check
     * @param email the email to check
     * @return true if a user with the given username or email already exists, false otherwise
     */
    boolean existsByUsernameOrEmail(String username, String email);

    /**
     * Checks if a user with the given username or email exists.
     * @param email the email to check
     * @return true if a user with the given username or email exists, false otherwise
     */
    boolean existsByUsername(String email);

    /**
     * Checks if a user with the given email exists.
     * @param email the email to check
     * @return true if a user with the given email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Finds a user by their username.
     *
     * @param username the username to search for
     * @return an Optional containing the user if found, or an empty if not found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their role.
     *
     * @param role the role to search for
     * @return a list of users with the given role
     */
    List<User> findByRolesContaining(String role);

    /**
     * Finds all users ordered by their username.
     *
     * @return a list of all users ordered by their username
     */
    @Query("SELECT u FROM User u ORDER BY u.username")
    List<User> findAllOrderedByUsername();

    /**
     * Finds all users with the given role ordered by their username.
     *
     * @param role the role to search for
     * @return a list of users with the given role ordered by their username
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = 'TRAINER' ORDER BY u.username")
    List<User> findUserByRoleOrderedByUsername(@Param("role") String role);

    /**
     * Finds all participants of a schedule by the schedule ID, ordered by username.
     *
     * @param scheduleId the ID of the schedule
     * @return a list of participants ordered by username
     */
    @Query("SELECT u FROM User u JOIN u.schedules s WHERE s.scheduleId = :scheduleId ORDER BY u.username")
    List<User> findParticipantsByScheduleID(@Param("scheduleId") Long scheduleId);

    /**
     * Finds absent users of a schedule by the schedule ID, ordered by username.
     *
     * @param scheduleId the ID of the schedule
     * @return a list of absent users ordered by username
     */
    @Query("SELECT u FROM User u JOIN Schedule s ON s.scheduleId = :scheduleId AND u MEMBER OF s.absentUsers ORDER BY u.username")
    List<User> findAbsentUsersByScheduleID(@Param("scheduleId") Long scheduleId);

}
