package com.eventbooking.backend.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventbooking.backend.model.Booking;
import com.eventbooking.backend.repository.BookingRepository;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*") // RECTIFIED: Allows Vercel and mobile devices to connect
public class AdminController {

    @Autowired
    private BookingRepository bookingRepository;

    // 1. Fetch All Bookings for the Master List
    @GetMapping("/bookings")
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // 2. Dashboard Stats (Total Revenue & Total Students)
    @GetMapping("/stats")
    public Map<String, Object> getDashboardStats() {
        List<Booking> allBookings = bookingRepository.findAll();
        
        BigDecimal totalRevenue = allBookings.stream()
                .map(Booking::getTotalAmount)
                .filter(amount -> amount != null) // Safety check
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalStudents = allBookings.size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalStudents", totalStudents);
        
        return stats;
    }

    // 3. Admin Login Endpoint
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // The secure credentials to access your Vel Tech dashboard
        String correctUser = "admin";
        String correctPass = "VelTech@Admin"; 

        if (correctUser.equals(username) && correctPass.equals(password)) {
            return ResponseEntity.ok().body(Map.of("success", true));
        } else {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid credentials"));
        }
    }

    // 4. Export to CSV (Great for the project review!)
    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportBookingsToCSV() {
        List<Booking> bookings = bookingRepository.findAll();
        StringBuilder csvContent = new StringBuilder();
        
        csvContent.append("Booking ID,Student Name,Roll No,Department,Tickets Booked,Total Amount Paid\n");

        for (Booking b : bookings) {
            csvContent.append(b.getId()).append(",")
                      .append(b.getUserName() != null ? b.getUserName().replace(",", " ") : "").append(",")
                      .append(b.getStudentRollNo() != null ? b.getStudentRollNo() : "").append(",")
                      .append(b.getUserDepartment() != null ? b.getUserDepartment() : "").append(",")
                      .append(b.getTicketsBooked()).append(",")
                      .append(b.getTotalAmount()).append("\n");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=campus_events_report.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvContent.toString().getBytes());
    }
}