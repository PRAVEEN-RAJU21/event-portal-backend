package com.eventbooking.backend.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends a Tier-1 Premium Digital Pass with Dynamic Hub Branding.
     */
    public void sendTicketEmail(String toEmail, String userName, String eventName, 
                                int tickets, String venue, String dateTime, 
                                String totalAmount, String qrCodeBase64, byte[] pdfContent, String category) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 1. DYNAMIC BRANDING LOGIC
            boolean isTechnical = category != null && category.equalsIgnoreCase("technical");
            String hubTitle = isTechnical ? "TECHNICAL HUB" : "NON-TECHNICAL HUB";
            String accentColor = isTechnical ? "#00f6ff" : "#ffcc00"; // Cyan for Tech, Yellow for Non-Tech
            String gradient = isTechnical 
                ? "linear-gradient(135deg, #00f6ff 0%, #0072ff 100%)" 
                : "linear-gradient(135deg, #ffcc00 0%, #ff8800 100%)";

            helper.setFrom("rajupraveenkumarrr@gmail.com"); 
            helper.setTo(toEmail);
            helper.setSubject("🎟️ Your Official Pass: " + eventName);

            // Calendar link remains as it is useful
            String calendarUrl = "https://www.google.com/calendar/render?action=TEMPLATE&text=" + 
                                 URLEncoder.encode(eventName, StandardCharsets.UTF_8);

            // 2. UPDATED HTML TEMPLATE (Maps Removed)
            String htmlContent = "<html><body style='margin:0; padding:0; background-color: #0a0a0a; font-family: Segoe UI, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 20px auto; background: #161616; border: 1px solid #333; border-radius: 20px; overflow: hidden; color: #ffffff; box-shadow: 0 10px 30px rgba(0, 246, 255, 0.1);'>" +
                    
                    // --- DYNAMIC NEON HEADER ---
                    "<div style='background: " + gradient + "; padding: 35px; text-align: center;'>" +
                        "<h1 style='margin:0; font-size: 26px; letter-spacing: 3px; color: #000; font-weight: 800;'>" + hubTitle + "</h1>" +
                        "<p style='margin:5px 0 0; font-weight: 600; color: #000; opacity: 0.8; letter-spacing: 1px;'>OFFICIAL EVENT PASS</p>" +
                    "</div>" +

                    // --- CONTENT SECTION ---
                    "<div style='padding: 40px;'>" +
                        "<h2 style='color: " + accentColor + "; margin-top: 0;'>Hello " + userName.toUpperCase() + "!</h2>" +
                        "<p style='color: #aaa; line-height: 1.6; font-size: 15px;'>Your registration is confirmed. Below is your unique digital pass for the upcoming event at the <strong>Vel Tech Campus</strong>.</p>" +

                        // --- THE TICKET CARD ---
                        "<div style='background: #222; border: 1px solid #333; border-radius: 12px; padding: 25px; margin: 30px 0; border-left: 5px solid " + accentColor + ";'>" +
                            "<table width='100%'>" +
                                "<tr>" +
                                    "<td style='padding-bottom: 20px;'><span style='color: #666; font-size: 11px; font-weight: bold;'>EVENT</span><br><b style='font-size: 18px; color: #fff;'>" + eventName + "</b></td>" +
                                    "<td style='padding-bottom: 20px; text-align: right;'><span style='color: #666; font-size: 11px; font-weight: bold;'>QTY</span><br><b style='font-size: 18px; color: #fff;'>" + tickets + " Person(s)</b></td>" +
                                "</tr>" +
                                "<tr>" +
                                    "<td><span style='color: #666; font-size: 11px; font-weight: bold;'>SCHEDULE</span><br><b style='font-size: 15px; color: #fff;'>" + dateTime + "</b></td>" +
                                    "<td style='text-align: right;'><span style='color: #666; font-size: 11px; font-weight: bold;'>LOCATION</span><br><b style='font-size: 15px; color: #fff;'>" + venue + "</b></td>" +
                                "</tr>" +
                            "</table>" +
                            
                            // --- SECURE QR PASS ---
                            "<div style='text-align: center; margin-top: 30px; padding-top: 30px; border-top: 1px solid #333;'>" +
                                "<div style='background: white; padding: 15px; display: inline-block; border-radius: 15px;'>" +
                                    "<img src='cid:qrImage' width='220' height='220' style='display: block;' alt='Pass ID' />" +
                                "</div>" +
                                "<p style='color: #555; font-size: 10px; margin-top: 15px; letter-spacing: 2px;'>ENCRYPTED CAMPUS ENTRY PASS</p>" +
                            "</div>" +
                        "</div>" +

                        // --- INTERACTIVE ACTIONS (Maps Removed) ---
                        "<div style='text-align: center; margin: 30px 0;'>" +
                            "<a href='" + calendarUrl + "' style='background: #1a1a1a; color: " + accentColor + "; padding: 14px 25px; border-radius: 10px; text-decoration: none; font-weight: bold; display: inline-block; border: 1px solid " + accentColor + "; font-size: 13px;'>📅 SAVE TO CALENDAR</a>" +
                        "</div>" +

                        "<p style='font-size: 12px; color: #444; text-align: center; font-style: italic;'>An official PDF ticket has been attached for offline access.</p>" +
                    "</div>" +

                    // --- DYNAMIC FOOTER ---
                    "<div style='background: #000; padding: 25px; text-align: center; border-top: 1px solid #333;'>" +
                        "<p style='margin: 0; font-size: 13px; color: #666;'>Developed by <strong>Raju Praveen Kumar Reddy</strong></p>" +
                        "<p style='margin: 5px 0 0; font-size: 11px; color: #444; letter-spacing: 1px;'>" + hubTitle + " | UNIVERSITY EVENT MANAGEMENT</p>" +
                    "</div>" +

                "</div>" +
            "</body></html>";

            helper.setText(htmlContent, true);

            // Inline Asset Attachment (QR Code)
            byte[] qrBytes = Base64.getDecoder().decode(qrCodeBase64);
            helper.addInline("qrImage", new ByteArrayResource(qrBytes), "image/png");

            // Document Attachment (PDF Ticket)
            if (pdfContent != null) {
                String fileName = hubTitle.replace(" ", "") + "_Pass_" + userName.replace(" ", "_") + ".pdf";
                helper.addAttachment(fileName, new ByteArrayResource(pdfContent));
            }

            mailSender.send(message);
            System.out.println("Email delivered for: " + hubTitle);
            
        } catch (MessagingException e) {
            System.err.println("Email Delivery Error: " + e.getMessage());
        }
    }
}