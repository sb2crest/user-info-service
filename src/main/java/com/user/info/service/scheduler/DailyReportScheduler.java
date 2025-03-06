package com.user.info.service.scheduler;

import com.user.info.service.entity.BookingEntity;
import com.user.info.service.model.BookingStatusEnum;
import com.user.info.service.model.EmailTransport;
import com.user.info.service.repository.BookingRepo;
import com.user.info.service.util.Mapper;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;

@Service
@PropertySource("classpath:application.properties")
public class DailyReportScheduler {

    @Autowired
    BookingRepo bookingRepo;

    @Autowired
    Mapper mapper;

    @Autowired
    @Qualifier("defaultEmailTransport")
    private EmailTransport emailTransport;

    @Value("${spring.mail.username}")
    private String emailUsername;

    @Value("${spring.mail.password}")
    private String emailPassword;

    @Value("${mail.to.username}")
    private String toEmailAddress;

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    @Value("${spring.mail.properties.mail.smtp.starttls.required}")
    private boolean mailStartTlsRequired;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private boolean mailStartTlsEnable;

    @Value("${spring.mail.properties.mail.smtp.socketFactory.class}")
    private String mailSocketFactoryClass;

    @Value("${spring.mail.properties.mail.debug}")
    private boolean mailDebug;

    @Value("${nandu.bus.image}")
    private String logo;

    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yy");

    public DailyReportScheduler(EmailTransport emailTransport, BookingRepo bookingRepo) {
        this.emailTransport = emailTransport;
        this.bookingRepo = bookingRepo;
    }

    @Scheduled(cron = "0 0 0 * * ?",zone = "Asia/Kolkata")
    public void sendDailyReportEmail() throws MessagingException, IOException {
        ByteArrayOutputStream outputStream = generateByteArray();

        Session session = SessionProvider.createSession(mailHost, mailPort, emailUsername, emailPassword,
                mailStartTlsRequired, mailStartTlsEnable, mailSocketFactoryClass, mailDebug);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailUsername));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmailAddress));
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

        emailTransport.send(message);

        System.out.println("Email sent successfully.");
    }


    ByteArrayOutputStream generateByteArray() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        PdfFont font = PdfFontFactory.createFont(StandardFonts.COURIER_BOLD);
        Document doc = new Document(pdfDoc).setFont(font);

        // Set border properties
        Color borderColor = new DeviceRgb(0, 0, 0); // Border color
        float borderWidth = 2f; // Border width (adjust as needed)

        // Set header and footer properties for all pages
        pdfDoc.addEventHandler(PdfDocumentEvent.START_PAGE, new Mapper.HeaderFooterEventHandler(borderColor, borderWidth));
        List<BookingEntity> bookingEntityList = bookingRepo.getReport(LocalDate.now().minusDays(1));

        // Create a table for company details and watermark image
        Table companyDetailsTable = new Table(UnitValue.createPercentArray(new float[]{5, 1}));
        companyDetailsTable.setWidth(UnitValue.createPercentValue(100));

        PdfFont companyFont = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
        Paragraph companyParagraph = new Paragraph()
                .add("\n")
                .add(new Text("NANDUBUS").setFont(companyFont).setFontSize(20)) // Set font size to 20px
                .add("\n") // Add a new line
                .add(new Text("#584 (Seabed2Crest Pvt Ltd) Near Suryodaya School, Hesaraghatta Hobli, Rajanukunte, Yelahanka Taluk, Bangalore North, Karnataka - 560064 \n\n\n").setFont(companyFont).setFontSize(10)); // Set font size to 10px

        companyDetailsTable.addCell(new Cell().add(companyParagraph).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER).setMarginTop(20f));

        // Add watermark image to the top-right corner with a negative right margin
        Image watermarkImage = new Image(ImageDataFactory.create(logo));
        watermarkImage.setWidth(UnitValue.createPointValue(100));

        companyDetailsTable.addCell(new Cell().add(watermarkImage).setBorder(Border.NO_BORDER).setMarginTop(20f).setTextAlignment(TextAlignment.RIGHT));
        doc.add(companyDetailsTable);

        // Create a table for the Booking Date
        Table dateTable = new Table(1);
        dateTable.setWidth(400);

        Paragraph dateParagraph = new Paragraph();
        if (bookingEntityList.size() > 0) {
            dateParagraph.add("Booking Date: ");
            dateParagraph.add(format.format(bookingEntityList.get(0).getBookingDate()));
            dateParagraph.add("\n");

            dateTable.addCell(new Cell().add(dateParagraph)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setBorder(Border.NO_BORDER));

            doc.add(dateTable);

            float[] bookingInfoColumnWidths = {130, 130, 145, 150, 150, 130, 130, 130, 130};
            Table bookingTable = new Table(bookingInfoColumnWidths);
            bookingTable.setTextAlignment(TextAlignment.CENTER);
            bookingTable.addCell(new Cell().add(new Paragraph("Booking ID")));
            bookingTable.addCell(new Cell().add(new Paragraph("Vehicle Number")));
            bookingTable.addCell(new Cell().add(new Paragraph("Booked Date")));
            bookingTable.addCell(new Cell().add(new Paragraph("From Date")));
            bookingTable.addCell(new Cell().add(new Paragraph("To Date")));
            bookingTable.addCell(new Cell().add(new Paragraph("Booking Status")));
            bookingTable.addCell(new Cell().add(new Paragraph("Total Amount")));
            bookingTable.addCell(new Cell().add(new Paragraph("Advance Paid")));
            bookingTable.addCell(new Cell().add(new Paragraph("Balance Amount")));


            for (BookingEntity entity : bookingEntityList) {
                bookingTable.addCell(new Cell().add(new Paragraph(entity.getBookingId()).setFontSize(8)));
                bookingTable.addCell(new Cell().add(new Paragraph(entity.getVehicleNumber()).setFontSize(8)));
                bookingTable.addCell(new Cell().add(new Paragraph(format.format(entity.getBookingDate())).setFontSize(8)));
                bookingTable.addCell(new Cell().add(new Paragraph(format.format(entity.getFromDate())).setFontSize(8)));
                bookingTable.addCell(new Cell().add(new Paragraph(format.format(entity.getToDate())).setFontSize(8)));
                bookingTable.addCell(new Cell().add(new Paragraph(BookingStatusEnum.getDesc(entity.getBookingStatus())).setFontSize(8)));
                bookingTable.addCell(new Cell().add(new Paragraph(String.valueOf(entity.getTotalAmount() != null ? entity.getTotalAmount() : 0.00)).setFontSize(8)));
                bookingTable.addCell(new Cell().add(new Paragraph(String.valueOf(entity.getAdvanceAmountPaid() != null ? entity.getAdvanceAmountPaid() : 0.00)).setFontSize(8)));
                bookingTable.addCell(new Cell().add(new Paragraph(String.valueOf(entity.getRemainingAmount() != null ? entity.getRemainingAmount() : 0.00)).setFontSize(8)));

            }

            doc.add(bookingTable);
        } else {
            dateParagraph.add("\n There is no Booking Details found on " + format.format(LocalDateTime.now().minusDays(1)));
            dateTable.addCell(new Cell().add(dateParagraph)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setBorder(Border.NO_BORDER));

            doc.add(dateTable);
        }
        writer.close();
        pdfDoc.close();
        doc.close();

        System.out.println("PDF generated successfully.");
        return outputStream;
    }
}
