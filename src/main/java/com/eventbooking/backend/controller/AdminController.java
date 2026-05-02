package com.eventbooking.backend.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders; // Required for Login
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping; // Required for Login
import org.springframework.web.bind.annotation.PostMapping; // Required for Login
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventbooking.backend.model.Booking;
import com.eventbooking.backend.repository.BookingRepository;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
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
        
        // Calculate Total Revenue
        BigDecimal totalRevenue = allBookings.stream()
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Count Total Students Registered
        long totalStudents = allBookings.size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalStudents", totalStudents);
        
        return stats;
    }

    // 3. THE SECURITY LOCK: Admin Login Endpoint
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // The secure credentials to access your Vel Tech dashboard
        String correctUser = "admin";
        String correctPass = "VelTech@Admin"; 

        if (correctUser.equals(username) && correctPass.equals(password)) {
            // Give them the green light
            return ResponseEntity.ok().body(Map.of("success", true));
        } else {
            // Reject the login attempt
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid credentials"));
        }
    }
    // 4. NEW TIER 2 FEATURE: Export to Excel/CSV
    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportBookingsToCSV() {
        List<Booking> bookings = bookingRepository.findAll();
        StringBuilder csvContent = new StringBuilder();
        
        // 1. Create the Header Row for Excel
        csvContent.append("Booking ID,Student Name,Roll No,Department,Tickets Booked,Total Amount Paid\n");

        // 2. Loop through every student and add their data as a new row
        for (Booking b : bookings) {
            csvContent.append(b.getId()).append(",")
                      // We replace commas in the name with spaces so it doesn't break the CSV columns
                      .append(b.getUserName() != null ? b.getUserName().replace(",", " ") : "").append(",")
                      .append(b.getStudentRollNo()).append(",")
                      .append(b.getUserDepartment()).append(",")
                      .append(b.getTicketsBooked()).append(",")
                      .append(b.getTotalAmount()).append("\n");
        }

        // 3. Package it as a downloadable file
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=campus_events_report.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvContent.toString().getBytes());
    }
}