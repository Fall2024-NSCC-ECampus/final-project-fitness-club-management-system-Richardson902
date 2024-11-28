# Fitness Club Management System

This Spring Boot application provides management for a fitness club, including user registration, schedule management, attendance tracking, and role-based access control.

## Features

- User management (CRUD operations)
- Schedule management (CRUD operations)
- Attendance tracking
- User authentication and authorization
- Role-based access control

## Technologies Used

- Java
- Spring Boot
- Spring Security
- Thymeleaf
- Maven
- Bootstrap

## Getting Started

1. Clone the repository:

    ```bash
    git clone https://github.com/Fall2024-NSCC-ECampus/final-project-fitness-club-management-system-Richardson902
   ```

2. Change into the project directory:

    ```bash
    cd final-project-fitness-club-management-system-Richardson902
    ```

3. Build the project:
    ```bash
    mvn clean install
    ```
   
4. Run the application:
    ```bash
    mvn spring-boot:run
    ```

## Endpoints

### User Endpoints

- `GET /users` - Display a list of all users (admin)
- `GET /users/{userId}` - Display details for a specific user (admin)
- `POST /users/{userId}/update` - Update details of a specific user (admin)
- `POST /users/{userId}/delete` - Delete a specific user (admin)

### Schedule Endpoints

- `GET /schedule/edit` - Display the schedule editing page (admin)
- `GET /schedule/view` - Display the schedule viewing page (authenticated users)
- `GET /schedule/edit/{scheduleId}/times` - Display the page for editing schedule times (admin)
- `GET /schedule/edit/{scheduleId}/participants` - Display the page for editing schedule participants (admin)
- `GET /schedule/edit/{scheduleId}/trainer` - Display the page for editing the schedule trainer (admin)
- `GET /schedule/mark-attendance/{scheduleId}` - Display the page for marking attendance (admin, trainer)
- `POST /schedule` - Schedule a new session (admin)
- `POST /schedule/edit/{scheduleId}/time` - Update the time of an existing schedule (admin)
- `POST /schedule/edit/{scheduleId}/trainer` - Update the trainer of an existing schedule (admin)
- `POST /schedule/edit/{scheduleId}/participants` - Update the participants of an existing schedule (admin)
- `POST /schedule/mark-attendance/{scheduleId}` - Mark attendance for a schedule (admin, trainer)
- `POST /schedule/delete/{scheduleId}` - Delete a schedule (admin)

### Authentication Endpoints

- `GET /login` - Display the login form
- `GET /register` - Display the registration form (admin)
- `POST /register` - Handle user registration (admin)

### Account Endpoints

- `GET /account` - Display account details for the authenticated user
- `POST /account/updatePassword` - Update password for the authenticated user
