package com.eventbooking.backend.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

@Service
public class FileService {

    /**
     * Generates a QR code image as a Base64 string.
     */
    public String generateQRCodeBase64(String data) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 250, 250);
            
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
                return Base64.getEncoder().encodeToString(outputStream.toByteArray());
            }
        } catch (WriterException | IOException e) {
            System.err.println("QR Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generates a professional PDF ticket for the Technical Hub Portal.
     * Satisfies VS Code hints by using try-with-resources for the writers.
     */
    public byte[] generateTicketPDF(String userName, String eventName, String venue, String date, String qrBase64) {
        // Step 1: Initialize the stream outside the try-with-resources
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Step 2: Use try-with-resources for the PDF Writer, Document, and Kernel
        // This ensures they are CLOSED automatically before the code leaves this block.
        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // Header - Technical Hub Branding
            document.add(new Paragraph("TECHNICAL HUB - OFFICIAL E-TICKET")
                    .setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("____________________________________________________")
                    .setTextAlignment(TextAlignment.CENTER));

            // Ticket Details
            document.add(new Paragraph("\nAttendee: " + userName).setFontSize(14));
            document.add(new Paragraph("Event: " + eventName).setBold().setFontSize(16));
            document.add(new Paragraph("Venue: " + venue));
            document.add(new Paragraph("Date & Time: " + date));
            
            // Add QR Code to PDF
            byte[] qrBytes = Base64.getDecoder().decode(qrBase64);
            Image qrImage = new Image(ImageDataFactory.create(qrBytes));
            qrImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
            document.add(new Paragraph("\nScan for Entry:").setTextAlignment(TextAlignment.CENTER));
            document.add(qrImage);

            // Footer
            document.add(new Paragraph("\n\nNote: Please carry a valid ID proof along with this ticket.")
                    .setFontSize(10).setItalic().setTextAlignment(TextAlignment.CENTER));

            // Logic: Once we hit this closing brace '}', the 'document' and 'writer' 
            // are automatically closed and flushed into the 'baos'.
        } catch (Exception e) {
            System.err.println("PDF Generation Error: " + e.getMessage());
            return null;
        }

        // Step 3: Now it is 100% safe to fetch the bytes because the PDF is sealed.
        return baos.toByteArray();
    }
}