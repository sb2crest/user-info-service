package com.example.user_info_service.scheduler;

import com.example.user_info_service.entity.BookingEntity;
import com.example.user_info_service.model.BookingStatusEnum;
import com.example.user_info_service.model.EmailTransport;
import com.example.user_info_service.repository.BookingRepo;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.properties.TextAlignment;

@Service
@PropertySource("classpath:application.properties")
public class MonthlyReportScheduler {

    @Autowired
    BookingRepo bookingRepo;

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

    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final String watermarkImagePath = "D:\\projects\\Vehicle-project\\user-info-service\\src\\main\\resources\\images\\LOGO1.png";

    public MonthlyReportScheduler(EmailTransport emailTransport, BookingRepo bookingRepo) {
        this.emailTransport = emailTransport;
        this.bookingRepo = bookingRepo;
    }

    private boolean firstPage = true; // To check if it's the first page

    @Scheduled(cron = "0 0 0 1 * ?")
    public void sendMonthlyReportEmail() throws Exception {
        Session session = SessionProvider.createSession(mailHost, mailPort, emailUsername, emailPassword,
                mailStartTlsRequired, mailStartTlsEnable, mailSocketFactoryClass, mailDebug);

        LocalDate currentDate = LocalDate.now();
        LocalDate yesterday = currentDate.minusDays(1);
        LocalDate startDate = yesterday.minusMonths(1);
        LocalDate endDate = yesterday;

        ByteArrayOutputStream outputStream = generateByteArray(startDate, endDate, currentDate);

        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(emailUsername));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmailAddress));

        message.setSubject("Monthly Report for " + startDate.format(format) + " to " + endDate.format(format));

        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText("Please find the monthly report attached.");

        MimeBodyPart attachmentPart = new MimeBodyPart();
        DataSource source = new ByteArrayDataSource(outputStream.toByteArray(), "application/pdf");
        attachmentPart.setDataHandler(new DataHandler(source));
        attachmentPart.setFileName("monthly_report_" + startDate.format(format) + "_to_" + endDate.format(format) + ".pdf");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        multipart.addBodyPart(attachmentPart);

        message.setContent(multipart);

        emailTransport.send(message);

        System.out.println("Email sent successfully for " + startDate.format(format) + " to " + endDate.format(format));

    }

    public ByteArrayOutputStream generateByteArray(LocalDate startDate, LocalDate endDate, LocalDate currentDate) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        pdfDoc.setDefaultPageSize(PageSize.A4); // Set the default page size

        PdfFont font = PdfFontFactory.createFont(StandardFonts.COURIER_BOLD);
        Document doc = new Document(pdfDoc).setFont(font);

        // Set border properties
        Color borderColor = new DeviceRgb(0, 0, 0); // Border color
        float borderWidth = 2f; // Border width (adjust as needed)
        pdfDoc.addEventHandler(PdfDocumentEvent.START_PAGE, new HeaderFooterEventHandler(borderColor, borderWidth));

        // Add watermark image and company details on the first page
        if (firstPage) {
            Table companyDetailsTable = new Table(UnitValue.createPercentArray(new float[]{4, 1})); // Adjust the array values
            companyDetailsTable.setWidth(UnitValue.createPercentValue(100));

            PdfFont companyFont = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);

            // Create a cell for "Nandu Tours & Travels" text
            Cell textCell = new Cell();
            Paragraph companyParagraph = new Paragraph()
                    .add("\n")
                    .add(new Text("NANDU TOURS & TRAVELS").setFont(companyFont).setFontSize(20)) // Set font size to 20px
                    .add("\n") // Add a new line
                    .add(new Text("Yelahanka New Town, Bangalore, 560064\n\n\n").setFont(companyFont).setFontSize(10)); // Set font size to 10px
            textCell.add(companyParagraph).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER).setMarginTop(20f);

            // Create a cell for the logo (image) with margin
            Cell logoCell = new Cell();
            Image watermarkImage = new Image(ImageDataFactory.create(watermarkImagePath));
            watermarkImage.setWidth(UnitValue.createPointValue(100));
            logoCell.add(watermarkImage).setBorder(Border.NO_BORDER).setMarginRight(20f).setTextAlignment(TextAlignment.RIGHT); // Adjust the margin as needed

            companyDetailsTable.addCell(textCell);
            companyDetailsTable.addCell(logoCell);

            float pageWidth = doc.getPdfDocument().getDefaultPageSize().getWidth();
            companyDetailsTable.setMarginTop(1 * pageWidth / 100);

            doc.add(companyDetailsTable);

            // Add space
            doc.add(new Paragraph().setMarginTop(20));

            // Add "Monthly Report" line with the date range
            Paragraph monthlyReportLine = new Paragraph("Monthly Report: " + startDate.format(format) + " to " + endDate.format(format) + "\n\n")
                    .setTextAlignment(TextAlignment.LEFT);
            doc.add(monthlyReportLine);

            firstPage = false; // Mark as not the first page
        }

        // Create a table for the Booking Date
        float[] bookingInfoColumnWidths = {140, 140, 140, 140, 140, 140, 140};
        Table bookingTable = new Table(bookingInfoColumnWidths);
        bookingTable.setTextAlignment(TextAlignment.CENTER);

        // Add table headers
        bookingTable.addCell(new Cell().add(new Paragraph("ID")));
        bookingTable.addCell(new Cell().add(new Paragraph("Booking ID")));
        bookingTable.addCell(new Cell().add(new Paragraph("Vehicle Number")));
        bookingTable.addCell(new Cell().add(new Paragraph("From Date")));
        bookingTable.addCell(new Cell().add(new Paragraph("To Date")));
        bookingTable.addCell(new Cell().add(new Paragraph("Booking Status")));
        bookingTable.addCell(new Cell().add(new Paragraph("Booking Date")));

        boolean hasData = false; // Flag to track if there is data in the report

        while (!startDate.isAfter(endDate)) {
            List<BookingEntity> bookingEntityList = bookingRepo.getReport(startDate);

            if (!bookingEntityList.isEmpty()) { // Check if there is data for this date
                hasData = true; // Set the flag to true
                for (BookingEntity entity : bookingEntityList) {
                    // Add data rows to the table
                    bookingTable.addCell(new Cell().add(new Paragraph(String.valueOf(entity.getId()))));
                    bookingTable.addCell(new Cell().add(new Paragraph(entity.getBookingId())));
                    bookingTable.addCell(new Cell().add(new Paragraph(entity.getVehicleNumber())));
                    bookingTable.addCell(new Cell().add(new Paragraph(format.format(entity.getFromDate()))));
                    bookingTable.addCell(new Cell().add(new Paragraph(format.format(entity.getToDate()))));
                    bookingTable.addCell(new Cell().add(new Paragraph(BookingStatusEnum.getDesc(entity.getBookingStatus()))));
                    bookingTable.addCell(new Cell().add(new Paragraph(format.format(entity.getBookingDate()))));
                }
            }

            startDate = startDate.plusDays(1);
        }

        if (hasData) {
            doc.add(bookingTable); // Add the table if there is data
        } else {
            // No data for the entire week; remove the first (empty) page
            bookingTable.addCell(new Cell().add(new Paragraph("There is no data")));
        }

        writer.close();
        pdfDoc.close();
        doc.close();

        return outputStream;
    }

    public class HeaderFooterEventHandler implements IEventHandler {
        private Color borderColor;
        private float borderWidth;

        public HeaderFooterEventHandler(Color borderColor, float borderWidth) {
            this.borderColor = borderColor;
            this.borderWidth = borderWidth;
        }

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();

            Document doc = new Document(pdfDoc);

            PdfCanvas canvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), pdfDoc);
            Rectangle pageSize = page.getPageSize();
            float leftX = pageSize.getLeft() + 10 + borderWidth / 2;
            float rightX = pageSize.getRight() - 10 - borderWidth / 2;
            float topY = pageSize.getTop() - 25 - borderWidth / 2;
            float bottomY = pageSize.getBottom() + 25 + borderWidth / 2;

            canvas.setStrokeColor(borderColor);
            canvas.setLineWidth(borderWidth);
            canvas.moveTo(leftX, topY);
            canvas.lineTo(rightX, topY);
            canvas.stroke();

            canvas.moveTo(leftX, topY);
            canvas.lineTo(leftX, bottomY);
            canvas.stroke();

            canvas.moveTo(rightX, topY);
            canvas.lineTo(rightX, bottomY);
            canvas.stroke();

            canvas.moveTo(leftX, bottomY);
            canvas.lineTo(rightX, bottomY);
            canvas.stroke();
        }
    }
}
