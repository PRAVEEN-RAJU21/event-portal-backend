package com.eventbooking.backend.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventbooking.backend.model.Event;
// Removed the unused Booking import to clear the IDE warning
import com.eventbooking.backend.repository.BookingRepository;
import com.eventbooking.backend.repository.EventRepository;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*") 
public class PublicBookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EventRepository eventRepository;

    @GetMapping("/verify/{id}")
    public ResponseEntity<?> verifyTicket(@PathVariable @NonNull Long id) {
        return bookingRepository.findById(id)
            .map(booking -> {
                Long eid = booking.getEventId();
                if (eid == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Data Integrity Error: Missing Event ID.");
                }

                Optional<Event> eventOpt = eventRepository.findById(eid);
                String eventName = eventOpt.map(Event::getEventName).orElse("Unknown Event");

                Map<String, Object> details = new HashMap<>();
                details.put("userName", booking.getUserName());
                details.put("emailId", booking.getEmailId());
                details.put("eventName", eventName);
                details.put("ticketsBooked", booking.getTicketsBooked());
                details.put("dept", booking.getUserDepartment());
                details.put("status", "VALID ✅");
                
                return ResponseEntity.ok((Object) details); 
            })
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid Ticket ❌"));
    }
}