package com.eventbooking.backend.service;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailService {

    @Value("${BREVO_API_KEY}")
    private String apiKey;

    private final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    public void sendTicketEmail(String toEmail, String userName, String eventName, 
                                int tickets, String venue, String dateTime, 
                                String totalAmount, String qrCodeBase64, byte[] pdfContent, String category) {
        
        RestTemplate restTemplate = new RestTemplate();
        
        // 1. DYNAMIC BRANDING LOGIC (Kept your design!)
        boolean isTechnical = category != null && category.equalsIgnoreCase("technical");
        String hubTitle = isTechnical ? "TECHNICAL HUB" : "NON-TECHNICAL HUB";
        String accentColor = isTechnical ? "#00f6ff" : "#ffcc00";
        String gradient = isTechnical 
            ? "linear-gradient(135deg, #00f6ff 0%, #0072ff 100%)" 
            : "linear-gradient(135deg, #ffcc00 0%, #ff8800 100%)";

        // 2. HEADERS
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        // 3. HTML CONTENT (Your exact template)
        String htmlContent = "<html><body style='margin:0; padding:0; background-color: #0a0a0a; font-family: Segoe UI, sans-serif;'>" +
            "<div style='max-width: 600px; margin: 20px auto; background: #161616; border: 1px solid #333; border-radius: 20px; overflow: hidden; color: #ffffff;'>" +
                "<div style='background: " + gradient + "; padding: 35px; text-align: center;'>" +
                    "<h1 style='margin:0; font-size: 26px; color: #000; font-weight: 800;'>" + hubTitle + "</h1>" +
                "</div>" +
                "<div style='padding: 40px;'>" +
                    "<h2 style='color: " + accentColor + ";'>Hello " + userName.toUpperCase() + "!</h2>" +
                    "<div style='background: #222; border-radius: 12px; padding: 25px; border-left: 5px solid " + accentColor + ";'>" +
                        "<p><b>Event:</b> " + eventName + "<br><b>QTY:</b> " + tickets + " Person(s)</p>" +
                        "<p><b>Schedule:</b> " + dateTime + "<br><b>Venue:</b> " + venue + "</p>" +
                        "<div style='text-align: center; margin-top: 20px;'>" +
                            "<img src='cid:qrImage' width='200' height='200' />" +
                        "</div>" +
                    "</div>" +
                "</div>" +
            "</div></body></html>";

        // 4. ATTACHMENTS (QR Code + PDF)
        List<Map<String, String>> attachments = new java.util.ArrayList<>();
        
        // Inline QR Code
        attachments.add(Map.of(
            "content", qrCodeBase64,
            "name", "qr.png",
            "contentId", "qrImage"
        ));

        // PDF Ticket
        if (pdfContent != null) {
            String base64Pdf = Base64.getEncoder().encodeToString(pdfContent);
            String fileName = hubTitle.replace(" ", "") + "_Pass.pdf";
            attachments.add(Map.of(
                "content", base64Pdf,
                "name", fileName
            ));
        }

        // 5. PREPARE PAYLOAD
        Map<String, Object> payload = new HashMap<>();
        payload.put("sender", Map.of("name", "Campus Event Portal", "email", "rajupraveenkumarrr@gmail.com"));
        payload.put("to", List.of(Map.of("email", toEmail, "name", userName)));
        payload.put("subject", "🎟️ Your Official Pass: " + eventName);
        payload.put("htmlContent", htmlContent);
        payload.put("attachment", attachments);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(BREVO_API_URL, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Email delivered via Brevo API for: " + hubTitle);
            }
        } catch (Exception e) {
            System.err.println("❌ Email Delivery Error: " + e.getMessage());
        }
    }
}