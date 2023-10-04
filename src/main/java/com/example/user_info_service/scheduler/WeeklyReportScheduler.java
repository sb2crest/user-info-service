package com.example.user_info_service.scheduler;

import com.example.user_info_service.entity.BookingEntity;
import com.example.user_info_service.model.BookingStatusEnum;
import com.example.user_info_service.repository.BookingRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;

@Service
@PropertySource("classpath:application.properties")
public class WeeklyReportScheduler {


    @Autowired
    BookingRepo bookingRepo;

    @Autowired
    private Environment env;

    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Scheduled(cron = "0 0 0 * * SUN")
    public void sendWeeklyReportEmail() {
        ByteArrayOutputStream outputStream = generateByteArray();
        Properties properties = new Properties();

        getPropertiesFromEnv(properties);
        String to = env.getProperty("mail.to.username");
        String from = env.getProperty("spring.mail.username");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(env.getProperty("spring.mail.username"), env.getProperty("spring.mail.password"));
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            message.setSubject("Weekly Report");

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Please find the weekly report attached.");

            MimeBodyPart attachmentPart = new MimeBodyPart();
            DataSource source = new ByteArrayDataSource(outputStream.toByteArray(), "application/pdf");
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName("weekly_report.pdf");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            Transport.send(message);

            System.out.println("Email sent successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ByteArrayOutputStream generateByteArray() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            PdfFont font = PdfFontFactory.createFont(StandardFonts.COURIER_BOLD);
            Document doc = new Document(pdfDoc).setFont(font);

            LocalDateTime date = LocalDateTime.now().minusDays(1);
            List<BookingEntity> bookingEntityList = bookingRepo.getWeeklyReport(format.format(date),format.format(date.minusWeeks(1)));

            float[] bookingInfoColumnWidths = { 140, 140, 140, 140, 140, 140, 140 };
            Table bookingTable = new Table(bookingInfoColumnWidths);
            bookingTable.setTextAlignment(TextAlignment.CENTER);
            bookingTable.addCell(new Cell().add(new Paragraph("ID")));
            bookingTable.addCell(new Cell().add(new Paragraph("Booking ID")));
            bookingTable.addCell(new Cell().add(new Paragraph("Vehicle Number")));
            bookingTable.addCell(new Cell().add(new Paragraph("From Date")));
            bookingTable.addCell(new Cell().add(new Paragraph("To Date")));
            bookingTable.addCell(new Cell().add(new Paragraph("Booking Status")));
            bookingTable.addCell(new Cell().add(new Paragraph("Booking Date")));

            for (BookingEntity entity : bookingEntityList) {
                bookingTable.addCell(new Cell().add(new Paragraph(String.valueOf(entity.getId()))));
                bookingTable.addCell(new Cell().add(new Paragraph(entity.getBookingId())));
                bookingTable.addCell(new Cell().add(new Paragraph(entity.getVehicleNumber())));
                bookingTable.addCell(new Cell().add(new Paragraph(entity.getFromDate())));
                bookingTable.addCell(new Cell().add(new Paragraph(entity.getToDate())));
                bookingTable.addCell(new Cell().add(new Paragraph(BookingStatusEnum.getDesc(entity.getBookingStatus()))));
                bookingTable.addCell(new Cell().add(new Paragraph(entity.getBookingDate())));
            }
            doc.add(bookingTable);
            writer.close();
            pdfDoc.close();
            doc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("PDF generated successfully.");
        return outputStream;
    }

    private void getPropertiesFromEnv(Properties properties) {
        properties.put("mail.smtp.host", env.getProperty("spring.mail.host"));
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", Integer.parseInt(env.getProperty("spring.mail.port")));
        properties.put("mail.smtp.starttls.required", Boolean.parseBoolean(env.getProperty("spring.mail.properties.mail.smtp.starttls.required")));
        properties.put("mail.smtp.starttls.enable", Boolean.parseBoolean(env.getProperty("spring.mail.properties.mail.smtp.starttls.enable")));
        properties.put("mail.smtp.socketFactory.class", env.getProperty("spring.mail.properties.mail.smtp.socketFactory.class"));
        properties.put("mail.debug", Boolean.parseBoolean(env.getProperty("spring.mail.properties.mail.debug")));
    }
}
