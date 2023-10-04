package com.example.user_info_service.scheduler;

import com.example.user_info_service.entity.BookingEntity;
import com.example.user_info_service.model.BookingStatusEnum;
import com.example.user_info_service.repository.BookingRepo;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
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
import java.util.List;
import java.util.Properties;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.element.Cell;

@Service
@PropertySource("classpath:application.properties")
public class DailyReportScheduler {

    @Autowired
    BookingRepo bookingRepo;

    @Autowired
    private Environment env;

    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // Watermark logo file path
    private final String watermarkImagePath = "D:\\projects\\Vehicle-project\\user-info-service\\src\\main\\resources\\images\\logo.jpg";

    @Scheduled(cron = "0 0 0 * * ?")
    public void sendDailyReportEmail() {
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

            message.setSubject("Daily Report");

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Please find the daily report attached.");

            MimeBodyPart attachmentPart = new MimeBodyPart();
            DataSource source = new ByteArrayDataSource(outputStream.toByteArray(), "application/pdf");
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName("daily_report.pdf");

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

            // Set header text and font
            String headerText = "Header Text";
            PdfFont headerFont = PdfFontFactory.createFont();

            // Set footer text and font
            String footerText = "Footer Text";
            PdfFont footerFont = PdfFontFactory.createFont();

            // Set border properties
            Color borderColor = new DeviceRgb(250, 0, 0); // Border color
            float borderWidth = 4f; // Border width (adjust as needed)

            // Set header and footer properties for all pages
            pdfDoc.addEventHandler(PdfDocumentEvent.START_PAGE, new HeaderFooterEventHandler(headerText, headerFont, footerText, footerFont, borderColor, borderWidth));
            List<BookingEntity> bookingEntityList = bookingRepo.getTodaysReport(format.format(LocalDateTime.now().minusDays(1)));

            // Create a table for the Booking Date
            Table dateTable = new Table(1);
            dateTable.setWidth(400);

            Paragraph dateParagraph = new Paragraph();
            dateParagraph.add("Booking Date: ");
            dateParagraph.add(bookingEntityList.get(0).getBookingDate());

            dateTable.addCell(new Cell().add(dateParagraph)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setBorder(Border.NO_BORDER));

            doc.add(dateTable);

            float[] bookingInfoColumnWidths = {140, 140, 140, 140, 140, 140};
            Table bookingTable = new Table(bookingInfoColumnWidths);
            bookingTable.setTextAlignment(TextAlignment.CENTER);
            bookingTable.addCell(new Cell().add(new Paragraph("ID")));
            bookingTable.addCell(new Cell().add(new Paragraph("Booking ID")));
            bookingTable.addCell(new Cell().add(new Paragraph("Vehicle Number")));
            bookingTable.addCell(new Cell().add(new Paragraph("From Date")));
            bookingTable.addCell(new Cell().add(new Paragraph("To Date")));
            bookingTable.addCell(new Cell().add(new Paragraph("Booking Status")));

            for (BookingEntity entity : bookingEntityList) {
                bookingTable.addCell(new Cell().add(new Paragraph(String.valueOf(entity.getId()))));
                bookingTable.addCell(new Cell().add(new Paragraph(entity.getBookingId())));
                bookingTable.addCell(new Cell().add(new Paragraph(entity.getVehicleNumber())));
                bookingTable.addCell(new Cell().add(new Paragraph(entity.getFromDate())));
                bookingTable.addCell(new Cell().add(new Paragraph(entity.getToDate())));
                bookingTable.addCell(new Cell().add(new Paragraph(BookingStatusEnum.getDesc(entity.getBookingStatus()))));
            }

            doc.add(bookingTable);

            // Add watermark image to each page
            PdfPage firstPage = pdfDoc.getFirstPage();
            float pageWidth = firstPage.getPageSize().getWidth();
            float pageHeight = firstPage.getPageSize().getHeight();
            PdfCanvas canvas = new PdfCanvas(firstPage.newContentStreamBefore(), firstPage.getResources(), pdfDoc);
            float watermarkWidth = UnitValue.createPointValue(1500).getValue(); // Adjust width as needed
            float watermarkHeight = UnitValue.createPointValue(1000).getValue(); // Adjust height as needed
            float x = (pageWidth - watermarkWidth) / 2;
            float y = (pageHeight - watermarkHeight) / 3;
            Image watermarkImage = new Image(ImageDataFactory.create(watermarkImagePath));
            watermarkImage.scaleToFit(watermarkWidth, watermarkHeight);
            watermarkImage.setFixedPosition(x, y);
            doc.add(watermarkImage);

            writer.close();
            pdfDoc.close();
            doc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("PDF generated successfully.");
        return outputStream;
    }

    public class HeaderFooterEventHandler implements IEventHandler {
        private String headerText;
        private PdfFont headerFont;
        private String footerText;
        private PdfFont footerFont;
        private Color borderColor;
        private float borderWidth;

        public HeaderFooterEventHandler(String headerText, PdfFont headerFont, String footerText, PdfFont footerFont, Color borderColor, float borderWidth) {
            this.headerText = headerText;
            this.headerFont = headerFont;
            this.footerText = footerText;
            this.footerFont = footerFont;
            this.borderColor = borderColor;
            this.borderWidth = borderWidth;
        }

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();

            // Create a Document object for the page
            Document doc = new Document(pdfDoc);

            // Draw borders on the left and right sides of the page
            PdfCanvas canvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), pdfDoc);
            Rectangle pageSize = page.getPageSize();
            float leftX = pageSize.getLeft() + 10 + borderWidth / 2;
            float rightX = pageSize.getRight() - 10 - borderWidth / 2;
            float topY = pageSize.getTop() - 25 - borderWidth / 2;
            float bottomY = pageSize.getBottom() + 25 + borderWidth / 2;

            // Draw top border line
            canvas.setStrokeColor(borderColor);
            canvas.setLineWidth(borderWidth);
            canvas.moveTo(leftX, topY);
            canvas.lineTo(rightX, topY);
            canvas.stroke();

            // Draw left border line
            canvas.moveTo(leftX, topY);
            canvas.lineTo(leftX, bottomY);
            canvas.stroke();

            // Draw right border line
            canvas.moveTo(rightX, topY);
            canvas.lineTo(rightX, bottomY);
            canvas.stroke();

            // Draw bottom border line
            canvas.moveTo(leftX, bottomY);
            canvas.lineTo(rightX, bottomY);
            canvas.stroke();

            // Add header
            Paragraph header = new Paragraph(headerText).setFont(headerFont);
            header.setFontSize(12);
            header.setTextAlignment(TextAlignment.CENTER);
            doc.showTextAligned(header, page.getPageSize().getWidth() / 2, topY + 20, pdfDoc.getPageNumber(page), TextAlignment.CENTER, VerticalAlignment.TOP, 0);

            // Add footer
            Paragraph footer = new Paragraph(footerText).setFont(footerFont);
            footer.setFontSize(12);
            footer.setTextAlignment(TextAlignment.CENTER);
            doc.showTextAligned(footer, page.getPageSize().getWidth() / 2, bottomY - 20, pdfDoc.getPageNumber(page), TextAlignment.CENTER, VerticalAlignment.BOTTOM, 0);
        }
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

