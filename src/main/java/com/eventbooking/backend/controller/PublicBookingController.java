package com.eventbooking.backend.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull; // Added for Null-Safety
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventbooking.backend.model.Event;
import com.eventbooking.backend.repository.BookingRepository;
import com.eventbooking.backend.repository.EventRepository;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*") // Allows mobile phones to scan from any network
public class PublicBookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EventRepository eventRepository;

    /**
     * Public verification endpoint for QR code scanning.
     * Rectified: Resolved Null Type Safety warnings for JSR-305 compliance.
     */
    @GetMapping("/verify/{id}")
    public ResponseEntity<?> verifyTicket(@PathVariable @NonNull Long id) { // Added @NonNull here
        return bookingRepository.findById(id)
            .map(booking -> {
                // Rectified: Check if eventId is null before repository lookup
                Long eid = booking.getEventId();
                if (eid == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Data Integrity Error: Booking record is missing a linked Event ID.");
                }

                // Now eid is guaranteed non-null for the repository call
                Optional<Event> eventOpt = eventRepository.findById(eid);
                String eventName = eventOpt.map(Event::getEventName).orElse("Unknown Event");

                Map<String, Object> details = new HashMap<>();
                details.put("userName", booking.getUserName());
                details.put("eventName", eventName);
                details.put("tickets", booking.getTicketsBooked());
                details.put("dept", booking.getUserDepartment());
                details.put("status", "VALID ✅");
                
                return ResponseEntity.ok((Object) details); 
            })
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid Ticket ❌"));
    }
}