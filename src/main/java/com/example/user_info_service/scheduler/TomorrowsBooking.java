package com.example.user_info_service.scheduler;

import com.example.user_info_service.entity.BookingEntity;
import com.example.user_info_service.entity.UserEntity;
import com.example.user_info_service.entity.VehicleEntity;
import com.example.user_info_service.repository.BookingRepo;
import com.example.user_info_service.repository.VehicleInfoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
@PropertySource("classpath:application.properties")
public class TomorrowsBooking {

    @Autowired
    BookingRepo bookingRepo;

    @Autowired
    private VehicleInfoRepo vehicleInfoRepo;

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${mail.to.username}")
    private String toEmailAddress;

    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final LocalDate tomorrow = LocalDate.now().plusDays(1);

    @Scheduled(cron = "0 0 10 * * ?")
    public void tomorrowsBookingDetails() throws Exception {

        List<BookingEntity> bookingEntityList = bookingRepo.getTomorrowsBooking(tomorrow);

            if (!bookingEntityList.isEmpty()) {
            for (BookingEntity bookingEntity : bookingEntityList) {
                if(bookingEntity.getUserEntity().getEmail() != null) {
                    VehicleEntity vehicle = vehicleInfoRepo.getByVehicleNumber(bookingEntity.getVehicleNumber());
                    sendEmailToUser(bookingEntity.getUserEntity(), bookingEntity, vehicle);
                }
            }
        }
        sendEmailToAdmin(bookingEntityList);
    }

    private void sendEmailToAdmin(List<BookingEntity> bookingEntityList) throws Exception {

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        StringBuilder messageBody = new StringBuilder();
        messageBody.append("<html><body>");
        messageBody.append("<h2>Booking Details of ").append(format.format(tomorrow)).append(" :</h2>");

        if (!bookingEntityList.isEmpty()) {
            messageBody.append("<table border='1' width='80%'>");

            messageBody.append("<style>td { text-align: center; }</style>");

            messageBody.append("<tr>");
            messageBody.append("<th><b>Booking ID</b></th>");
            messageBody.append("<th><b>User Name</b></th>");
            messageBody.append("<th><b>Mobile Number</b></th>");
            messageBody.append("<th><b>Booked Vehicle Number</b></th>");
            messageBody.append("<th><b>Booked Date</b></th>");
            messageBody.append("</tr>");

            for (BookingEntity bookingEntity : bookingEntityList) {
                messageBody.append("<tr>");
                messageBody.append("<td><div style='text-align: center;'>").append(bookingEntity.getBookingId()).append("</div></td>");
                messageBody.append("<td><div style='text-align: center;'>").append(bookingEntity.getUserEntity().getFirstName()).append("</div></td>");
                messageBody.append("<td><div style='text-align: center;'>").append(bookingEntity.getUserEntity().getMiddleName()).append("</div></td>");
                messageBody.append("<td><div style='text-align: center;'>").append(bookingEntity.getUserEntity().getLastName()).append("</div></td>");
                messageBody.append("<td><div style='text-align: center;'>").append(bookingEntity.getMobile()).append("</div></td>");
                messageBody.append("<td><div style='text-align: center;'>").append(bookingEntity.getVehicleNumber()).append("</div></td>");
                messageBody.append("<td><div style='text-align: center;'>").append(format.format(tomorrow)).append("</div></td>");
                messageBody.append("</tr>");
            }

            messageBody.append("</table>");
            messageBody.append("</body></html>");

        } else {
            messageBody.append("There is no Booking Details found on  this date.");

        }
        helper.setTo(toEmailAddress);
        helper.setSubject("Pay attention Booking Details of " + format.format(tomorrow));
        helper.setText(messageBody.toString(), true);

        javaMailSender.send(message);
    }

    private void sendEmailToUser(UserEntity user, BookingEntity booking, VehicleEntity vehicle) throws Exception {

        String localLogoPath = System.getProperty("user.dir") + "/src/main/resources/images/LOGO.png";

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<html><body>");
        emailContent.append("<p>Dear Customer <strong>")
                .append(user.getFirstName())
                .append(" ")
                .append(user.getMiddleName())
                .append(" ")
                .append(user.getLastName())
                .append("</strong>,</p>");

        emailContent.append("<p>Your Booking Details for Tomorrow</p>");
        emailContent.append("<table border='1'>");
        emailContent.append("<style>td { text-align: center; }</style>");
        emailContent.append("<tr><th>Booking ID</th><th>Vehicle Number</th><th>Driver Name</th><th>Driver Number</th><th>Alternate Number</th></tr>");
        emailContent.append("<tr>");
        emailContent.append("<td><div style='text-align: center;'>").append(booking.getBookingId()).append("</div></td>");
        emailContent.append("<td><div style='text-align: center;'>").append(booking.getVehicleNumber()).append("</div></td>");
        emailContent.append("<td><div style='text-align: center;'>").append(vehicle.getDriverName()).append("</div></td>");
        emailContent.append("<td><div style='text-align: center;'>").append(vehicle.getDriverNumber()).append("</div></td>");
        emailContent.append("<td><div style='text-align: center;'>").append(vehicle.getAlternateNumber()).append("</div></td>");
        emailContent.append("</tr>");
        emailContent.append("</table>");

        emailContent.append("<br><br><br>");

        emailContent.append("<img src='cid:logoImage' width='200' height='100'>");
        emailContent.append("<br>");

        emailContent.append("<p>In case of any emergency or if you need immediate assistance during your journey, please don't hesitate to call our emergency contact number at: <strong>").append(vehicle.getEmergencyNumber()).append("</strong>.</p>");
        emailContent.append("<p>We look forward to serving you and ensuring a safe and comfortable experience during your trip. Should you have any further questions or require additional information, please do not hesitate to contact us.</p>");
        emailContent.append("<p>Thank you for choosing <strong>NanduBus</strong> for your travel needs.</p>");

        emailContent.append("<br>");
        emailContent.append("<p><strong>Nandu Kasaram</strong><br><strong>General Manager</strong><br><strong>SeaBed2Crest Technologies Private Limited</strong><br><strong>Rajanukunte, Bangalore - 560064</strong></p>");
        emailContent.append("</body></html>");

        helper.setTo(user.getEmail());
        helper.setSubject("Booking Details for Tomorrow");
        helper.setText(emailContent.toString(), true);
        helper.addInline("logoImage", new File(localLogoPath));

        javaMailSender.send(message);
    }
}

