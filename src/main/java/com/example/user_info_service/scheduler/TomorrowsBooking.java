package com.example.user_info_service.scheduler;

import com.example.user_info_service.entity.BookingEntity;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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

    @Value("${nandu.bus.image}")
    private String logo;

    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yy");

    private final LocalDate tomorrow = LocalDate.now().plusDays(1);

    @Scheduled(cron = "0 0 10 * * ?",zone = "Asia/Kolkata")
    public void tomorrowsBookingDetails() throws Exception {

        List<BookingEntity> bookingEntityList = bookingRepo.getTomorrowsBooking(tomorrow);

        if (!bookingEntityList.isEmpty()) {
            Map<String, List<BookingEntity>> listMap = new HashMap<>();
            addDataToMap(listMap, bookingEntityList);
            for (Map.Entry<String, List<BookingEntity>> listEntry : listMap.entrySet()) {
                List<BookingEntity> userBookingList = listEntry.getValue();
                List<String> vehicleNumbers = userBookingList.stream()
                        .map(BookingEntity::getVehicleNumber)
                        .collect(Collectors.toList());
                List<VehicleEntity> vehicles = vehicleInfoRepo.getByVehicleNumbers(vehicleNumbers);
                sendEmailToUser(userBookingList, vehicles);
            }
        }
        sendEmailToAdmin(bookingEntityList);
    }

    private void addDataToMap(Map<String, List<BookingEntity>> listMap, List<BookingEntity> bookingEntityList) {
        for (BookingEntity bookingEntity : bookingEntityList) {
            String email = bookingEntity.getUserEntity().getEmail();
            if (email !=null) {
                if (listMap.containsKey(email)) {
                    List<BookingEntity> existingBookingDetails = listMap.get(email);
                    existingBookingDetails.add(bookingEntity);
                    listMap.put(email, existingBookingDetails);
                } else {
                    listMap.put(email, new ArrayList<>(List.of(bookingEntity)));
                }
            }
        }
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
                messageBody.append("<td><div style='text-align: center;'>").append(bookingEntity.getUserEntity().getFirstName())
                        .append(" ").append(bookingEntity.getUserEntity().getMiddleName())
                        .append(" ").append(bookingEntity.getUserEntity().getLastName())
                        .append("</td>");
                messageBody.append("<td><div style='text-align: center;'>").append(bookingEntity.getMobile()).append("</div></td>");
                messageBody.append("<td><div style='text-align: center;'>").append(bookingEntity.getVehicleNumber()).append("</div></td>");
                messageBody.append("<td><div style='text-align: center;'>").append(format.format(tomorrow)).append("</div></td>");
                messageBody.append("</tr>");
            }

            messageBody.append("</table>");
            messageBody.append("</body></html>");

        } else {
            messageBody.append("There is no Booking Details found on this date.");

        }

        messageBody.append("<br><br><br><br>");
        messageBody.append("<p>Best Regards<br><strong>NanduBus.in</strong></p>");
        messageBody.append("<br>");

        messageBody.append("<img src='").append(logo).append("' width='80' height='75'>");

        helper.setTo(toEmailAddress);
        helper.setSubject("Pay attention Booking Details of " + format.format(tomorrow));
        helper.setText(messageBody.toString(), true);

        javaMailSender.send(message);
    }

    private void sendEmailToUser(List<BookingEntity> bookingEntityList, List<VehicleEntity> vehicles) throws Exception {

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<html><body>");
        emailContent.append("<p>Dear <strong>")
                .append(bookingEntityList.get(0).getUserEntity().getFirstName())
                .append(" ")
                .append(bookingEntityList.get(0).getUserEntity().getMiddleName())
                .append(" ")
                .append(bookingEntityList.get(0).getUserEntity().getLastName())
                .append("</strong>,</p>");

        emailContent.append("<p>Your Booking Details for Tomorrow</p>");
        emailContent.append("<table border='1'>");
        emailContent.append("<style>td { text-align: center; }</style>");
        emailContent.append("<tr><th>Booking ID</th><th>Vehicle Number</th><th>Driver Name</th><th>Driver Number</th><th>Alternate Number</th></tr>");
        emailContent.append("<tr>");
        for (BookingEntity booking : bookingEntityList) {
            VehicleEntity vehicle = vehicles.stream().filter(v-> v.getVehicleNumber().equalsIgnoreCase(booking.getVehicleNumber())).findFirst().get();
            emailContent.append("<td><div style='text-align: center;'>").append(booking.getBookingId()).append("</div></td>");
            emailContent.append("<td><div style='text-align: center;'>").append(booking.getVehicleNumber()).append("</div></td>");
            emailContent.append("<td><div style='text-align: center;'>").append(vehicle.getDriverName()).append("</div></td>");
            emailContent.append("<td><div style='text-align: center;'>").append(vehicle.getDriverNumber()).append("</div></td>");
            emailContent.append("<td><div style='text-align: center;'>").append(vehicle.getAlternateNumber()).append("</div></td>");
            emailContent.append("</tr>");
        }
        emailContent.append("</table>");

        emailContent.append("<br><br><br>");

        emailContent.append("<img src='").append(logo).append("' width='200' height='100'>");
        emailContent.append("<br>");

        emailContent.append("<p>In case of any emergency or if you need immediate assistance during your journey, please don't hesitate to call our emergency contact number at: <strong>").append(vehicles.get(0).getEmergencyNumber()).append("</strong>.</p>");
        emailContent.append("<p>We look forward to serving you and ensuring a safe and comfortable experience during your trip. Should you have any further questions or require additional information, please do not hesitate to contact us.</p>");
        emailContent.append("<p>Thank you for choosing <strong>NanduBus</strong> for your travel needs.</p>");

        emailContent.append("<br>");
        emailContent.append("<p><strong>Nandu Kasaram</strong><br><strong>General Manager</strong><br><strong>SeaBed2Crest Technologies Private Limited</strong><br><strong>Near Suryodaya School, Hesaraghatta Hobli, Rajanukunte, Yelahanka Taluk, Bangalore North, Karnataka - 560064</strong></p>");
        emailContent.append("</body></html>");

        helper.setTo(bookingEntityList.get(0).getUserEntity().getEmail());
        helper.setSubject("Booking Details for Tomorrow");
        helper.setText(emailContent.toString(), true);

        javaMailSender.send(message);
    }
}

