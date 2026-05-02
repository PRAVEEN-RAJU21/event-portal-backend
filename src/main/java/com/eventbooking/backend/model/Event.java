package com.eventbooking.backend.model;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty; // Added for precise JSON mapping
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_name")
    private String eventName;

    private String category;

    private String department;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    private String venue;

    @Column(name = "ticket_price")
    private double ticketPrice;

    @Column(name = "available_tickets")
    private int availableTickets;

    // --- UPDATED FIELDS WITH JSON PROPERTY PROTECTION ---

    @Column(name = "is_group_event")
    @JsonProperty("isGroupEvent") // Forces Jackson to keep the 'is' prefix
    private boolean isGroupEvent;

    @Column(name = "max_group_size")
    @JsonProperty("maxGroupSize") // Ensures camelCase consistency in JSON
    private int maxGroupSize;

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(double ticketPrice) { this.ticketPrice = ticketPrice; }

    public int getAvailableTickets() { return availableTickets; }
    public void setAvailableTickets(int availableTickets) { this.availableTickets = availableTickets; }

    // --- UPDATED GETTERS AND SETTERS ---

    public boolean isGroupEvent() { return isGroupEvent; }
    public void setGroupEvent(boolean isGroupEvent) { this.isGroupEvent = isGroupEvent; }

    public int getMaxGroupSize() { return maxGroupSize; }
    public void setMaxGroupSize(int maxGroupSize) { this.maxGroupSize = maxGroupSize; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}