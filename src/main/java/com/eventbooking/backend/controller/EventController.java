package com.eventbooking.backend.controller;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import com.eventbooking.backend.model.Booking;
import com.eventbooking.backend.model.Event;
import com.eventbooking.backend.repository.BookingRepository;
import com.eventbooking.backend.repository.EventRepository;
import com.eventbooking.backend.service.EmailService;
import com.eventbooking.backend.service.FileService;

@RestController
@RequestMapping("/api")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private FileService fileService; 

    @GetMapping("/health")
    public String healthCheck() {
        return "Backend is UP and Running for Vel Tech Event Portal!";
    }

    @GetMapping("/events")
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    /**
     * Phase 1: Public Verification Endpoint
     * Allows mobile scanners to verify tickets without logging in.
     */
    @GetMapping("/public/verify/{id}")
    @CrossOrigin(origins = "*") 
    public ResponseEntity<?> verifyTicket(@PathVariable @NonNull Long id) {
        return bookingRepository.findById(id)
            .map(booking -> {
                // Manual null-check to satisfy strict compiler safety
                Long eid = booking.getEventId();
                if (eid == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Booking data integrity error: No linked event ID.");
                }

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

    @PostMapping("/book")
    public ResponseEntity<?> createBooking(@RequestBody Booking bookingRequest) {
        // Null-safety check for Event ID
        Long reqEventId = bookingRequest.getEventId();
        if (reqEventId == null) {
            return ResponseEntity.badRequest().body("Error: Event ID is missing in the request.");
        }

        Optional<Event> eventOptional = eventRepository.findById(reqEventId);

        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();

            // --- 1. Validation Logic ---
            if (!event.isGroupEvent() && bookingRequest.getTicketsBooked() > 1) {
                return ResponseEntity.badRequest().body("Error: This event only allows Individual (Solo) entry.");
            }

            if (event.isGroupEvent() && bookingRequest.getTicketsBooked() > event.getMaxGroupSize()) {
                return ResponseEntity.badRequest().body("Error: Group size exceeds the limit of " + event.getMaxGroupSize() + " members.");
            }

            if (event.getAvailableTickets() < bookingRequest.getTicketsBooked()) {
                return ResponseEntity.badRequest().body("Error: Not enough tickets available.");
            }

            // --- 2. Financials & Persistence ---
            BigDecimal ticketPrice = BigDecimal.valueOf(event.getTicketPrice());
            BigDecimal total = ticketPrice.multiply(BigDecimal.valueOf(bookingRequest.getTicketsBooked()));
            bookingRequest.setTotalAmount(total);

            event.setAvailableTickets(event.getAvailableTickets() - bookingRequest.getTicketsBooked());
            eventRepository.save(event);
            
            bookingRequest.setUserDepartment(event.getDepartment());
            Booking savedBooking = bookingRepository.save(bookingRequest);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
            String formattedDate = event.getEventDate().format(formatter);

            // --- 3. Smart QR & Asset Generation ---
            try {
                // Link that phone scanners will open
                String qrData = "https://event-portal-frontend-six.vercel.app/verify/" + savedBooking.getId();
                
                String qrCodeBase64 = fileService.generateQRCodeBase64(qrData);

                byte[] pdfTicket = fileService.generateTicketPDF(
                    savedBooking.getUserName(),
                    event.getEventName(),
                    event.getVenue(),
                    formattedDate,
                    qrCodeBase64
                );

                emailService.sendTicketEmail(
                    savedBooking.getEmailId(),
                    savedBooking.getUserName(),
                    event.getEventName(),
                    savedBooking.getTicketsBooked(),
                    event.getVenue(),           
                    formattedDate,
                    savedBooking.getTotalAmount().toString(),
                    qrCodeBase64, 
                    pdfTicket,
                    event.getCategory() 
                );

            } catch (Exception e) {
                System.err.println("Booking successful, but email assets failed: " + e.getMessage());
            }
            
            return ResponseEntity.ok(savedBooking);

        } else {
            return ResponseEntity.badRequest().body("Error: Event not found.");
        }
    }
}