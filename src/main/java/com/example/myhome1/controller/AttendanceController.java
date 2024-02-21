package com.example.myhome1.controller;

import com.example.myhome1.model.Attendance;
import com.example.myhome1.model.User;
import com.example.myhome1.service.AttendanceService;
import com.example.myhome1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    private final UserService userService;
    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(UserService userService, AttendanceService attendanceService) {
        this.userService = userService;
        this.attendanceService = attendanceService;
    }

    @GetMapping("/record")
    public String showRecordForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // 현재 로그인된 사용자의 username 가져오기
        // UserService에서 사용자 정보를 가져오는 메소드를 사용하여 사용자 정보를 가져옴
        User loggedInUser = userService.getUserByUsername(username);
        model.addAttribute("loggedInUser", loggedInUser);
        return "work/record-form"; // Thymeleaf 템플릿 파일 이름
    }

    @PostMapping("/record")
    public String recordAttendance(@RequestParam("checkInTime") String checkInTime,
                                   @RequestParam("workDetails") String workDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // 현재 로그인된 사용자의 username 가져오기
        User loggedInUser = userService.getUserByUsername(username);
        if (loggedInUser != null) {
            // Check if the incoming date/time string conforms to a specific format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime parsedDateTime = LocalDateTime.parse(checkInTime, formatter);
            Attendance attendance = new Attendance(loggedInUser, parsedDateTime, workDetails);
            attendanceService.recordAttendance(attendance);
        }
        return "redirect:/attendance/list";
    }


    @GetMapping("/list")
    public String showAttendanceList(Model model) {
        // AttendanceService를 사용하여 출근 기록 리스트를 가져옴
        List<Attendance> attendanceList = attendanceService.getAllAttendances();
        model.addAttribute("attendanceList", attendanceList);
        return "work/attendance-list";  // Thymeleaf 템플릿 파일 이름
    }

    @PostMapping("/leave")
    public String leave(@RequestParam("attendanceId") Long attendanceId) {
        // Find the attendance record based on the ID
        Attendance attendance = attendanceService.getAttendanceById(attendanceId);
        if (attendance != null) {
            // Set the check-out time for the found attendance record
            attendance.setCheckOutTime(LocalDateTime.now()); // Or specify your logic for check-out time
            // Update the attendance record in the database
            attendanceService.updateAttendance(attendance);
        }
        return "redirect:/attendance/list";
    }

}
