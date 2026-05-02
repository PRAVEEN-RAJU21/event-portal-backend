package com.eventbooking.backend.controller;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.eventbooking.backend.model.Booking;
import com.eventbooking.backend.model.Event;
import com.eventbooking.backend.repository.BookingRepository;
import com.eventbooking.backend.repository.EventRepository;
import com.eventbooking.backend.service.EmailService;
import com.eventbooking.backend.service.FileService;

@RestController
@RequestMapping("/api")
// 🛑 REMOVED: @CrossOrigin(origins = "*") - This was causing the 500 error
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

    @PostMapping("/book")
    public ResponseEntity<?> createBooking(@RequestBody Booking bookingRequest) {
        Optional<Event> eventOptional = eventRepository.findById(bookingRequest.getEventId());

        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();

            if (!event.isGroupEvent() && bookingRequest.getTicketsBooked() > 1) {
                return ResponseEntity.badRequest().body("Error: This event only allows Individual (Solo) entry.");
            }

            if (event.isGroupEvent() && bookingRequest.getTicketsBooked() > event.getMaxGroupSize()) {
                return ResponseEntity.badRequest().body("Error: Group size exceeds the limit of " + event.getMaxGroupSize() + " members.");
            }

            if (event.getAvailableTickets() < bookingRequest.getTicketsBooked()) {
                return ResponseEntity.badRequest().body("Error: Not enough tickets available. Remaining: " + event.getAvailableTickets());
            }

            BigDecimal ticketPrice = BigDecimal.valueOf(event.getTicketPrice());
            BigDecimal total = ticketPrice.multiply(BigDecimal.valueOf(bookingRequest.getTicketsBooked()));
            bookingRequest.setTotalAmount(total);

            event.setAvailableTickets(event.getAvailableTickets() - bookingRequest.getTicketsBooked());
            eventRepository.save(event);
            
            bookingRequest.setUserDepartment(event.getDepartment());
            Booking savedBooking = bookingRepository.save(bookingRequest);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
            String formattedDate = event.getEventDate().format(formatter);

            try {
                String qrData = "TICKET-ID: " + savedBooking.getId() + 
                                " | LEAD: " + savedBooking.getUserName() + 
                                " | EVENT: " + event.getEventName() +
                                " | QTY: " + savedBooking.getTicketsBooked();
                
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
                System.err.println("Booking saved, but assets failed: " + e.getMessage());
            }
            
            return ResponseEntity.ok(savedBooking);

        } else {
            return ResponseEntity.badRequest().body("Error: Event not found.");
        }
    }
}