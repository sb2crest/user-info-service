package com.example.user_info_service.service;

import com.example.user_info_service.entity.BookingEntity;
import com.example.user_info_service.entity.SlotsEntity;
import com.example.user_info_service.entity.UserEntity;
import com.example.user_info_service.entity.VehicleEntity;
import com.example.user_info_service.exception.BookingException;
import com.example.user_info_service.exception.ResStatus;
import com.example.user_info_service.model.BookingStatusEnum;
import com.example.user_info_service.model.GmailValidator;
import com.example.user_info_service.pojo.*;
import com.example.user_info_service.repository.BookingRepo;
import com.example.user_info_service.repository.SlotsRepo;
import com.example.user_info_service.repository.UserRepo;
import com.example.user_info_service.repository.VehicleInfoRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;
import java.io.File;
import java.text.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    @Autowired
    UserRepo userRepo;
    @Autowired
    SlotsRepo slotsRepo;
    @Autowired
    BookingRepo bookingRepo;
    @Autowired
    VehicleInfoRepo vehicleInfoRepo;

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${mail.to.username}")
    private String toEmailAddress;

    private final DateTimeFormatter localDateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final DateTimeFormatter localDateFormatForBookingDate = DateTimeFormatter.ofPattern("EEEE, MMMM dd yyyy");

    @Override
    @Transactional
    public BookingResponse bookingVehicle(BookingPojo bookingPojo) throws ParseException {
        checkUserDetails(bookingPojo.getUser());

        BookingResponse bookingResponse = new BookingResponse();
        BookingEntity bookingEntity = new BookingEntity();
        SlotsEntity slotsEntity = new SlotsEntity();

        boolean vehicleAvailability = slotsRepo.findVehicleAvailabilityOnRequiredDate(bookingPojo.getVehicleNumber(), bookingPojo.getFromDate(), bookingPojo.getToDate());
        if (!vehicleAvailability) {
            saveUser(bookingPojo);
            saveBooking(bookingEntity, bookingPojo);
            saveSlot(slotsEntity, bookingEntity);
        } else {
            bookingResponse.setBookingId(null);
            bookingResponse.setMessage("Slots already Booked");
            bookingResponse.setStatusCode(HttpStatus.IM_USED.value());
            return bookingResponse;
        }
        bookingResponse.setBookingId(bookingEntity.getBookingId());
        bookingResponse.setMessage("Booking successful");
        bookingResponse.setStatusCode(HttpStatus.OK.value());
        return bookingResponse;
    }


    private void checkUserDetails(UserPojo userPojo) {
        if (userPojo.getMobile().isEmpty()) {
            throw new BookingException(ResStatus.ENTER_NUMBER);
        }
        if (userPojo.getMobile().length() != 10) {
            throw new BookingException(ResStatus.MOBILE_DIGIT);
        }
        userEmailValidation(userPojo.getEmail());
    }

    private void userEmailValidation(String email) {
        if (email != null && !GmailValidator.isValidGmail(email)) {
            throw new BookingException(ResStatus.INVALID_EMAIL);
        }
    }

    private void saveBooking(BookingEntity bookingEntity, BookingPojo bookingPojo) {
        bookingEntity.setVehicleNumber(bookingPojo.getVehicleNumber());
        bookingEntity.setFromDate(bookingPojo.getFromDate());
        bookingEntity.setToDate(bookingPojo.getToDate());
        bookingEntity.setBookingId(generateBookingId());
        bookingEntity.setMobile(bookingPojo.getUser().getMobile());
        bookingEntity.setBookingStatus(BookingStatusEnum.ENQUIRY.getCode());
        bookingEntity.setBookingDate(LocalDate.now());
        bookingRepo.save(bookingEntity);
    }

    private void saveSlot(SlotsEntity slotsEntity, BookingEntity bookingEntity) {
        slotsEntity.setIsAvailable(false);
        slotsEntity.setFromDate(bookingEntity.getFromDate());
        slotsEntity.setToDate(bookingEntity.getToDate());
        slotsEntity.setVehicleNumber(bookingEntity.getVehicleNumber());
        slotsEntity.setBookingId(bookingEntity.getBookingId());
        slotsRepo.save(slotsEntity);
    }

    private void saveUser(BookingPojo bookingPojo) {
        UserEntity user = userRepo.getUserByMobileNumber(bookingPojo.getUser().getMobile());
        if (user == null) {
            UserEntity userEntity = new UserEntity();
            userEntity.setFirstName(bookingPojo.getUser().getFirstName());
            userEntity.setMiddleName(bookingPojo.getUser().getMiddleName());
            userEntity.setLastName(bookingPojo.getUser().getLastName());
            userEntity.setEmail(bookingPojo.getUser().getEmail());
            userEntity.setMobile(bookingPojo.getUser().getMobile());
            userRepo.save(userEntity);
        }
    }

    private String generateId() {
        UUID uuid = UUID.randomUUID();
        return "NB" + uuid;
    }

    private String generateBookingId() {
        String str = generateId();
        return str.substring(0, 6);
    }

    @Override
    public BookingDetails getBookingDetails(String bookingId) {
        BookingEntity bookingEntity = bookingRepo.getByBookingId(bookingId);
        validateBookingEntity(bookingEntity);
        BookingDetails bookingDetails = new BookingDetails();
        getUser(bookingEntity, bookingDetails);
        getSlot(bookingId, bookingDetails);
        getVehicleDetails(bookingDetails);
        return bookingDetails;
    }

    @Override
    public BookingInfo getBookingInfoByBookingId(String bookingId) {

        BookingEntity bookingEntity = bookingRepo.getByBookingId(bookingId);
        validateBookingEntity(bookingEntity);
        VehicleEntity vehicleEntity = vehicleInfoRepo.getByVehicleNumber(bookingEntity.getVehicleNumber());
        validateVehicleEntity(vehicleEntity);
        return getBookingInfo(vehicleEntity, bookingEntity);
    }

    @Override
    public void getInTouch(UserData userData) throws Exception {
        userEmailValidation(userData.getEmail());
        String localLogoPath = System.getProperty("user.dir") + "/src/main/resources/images/LOGO.png";

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        StringBuilder messageBody = new StringBuilder();
        messageBody.append("<html><body>");
        messageBody.append("<h2>User Details ").append(" :</h2>");

        messageBody.append("<table border='1' width='80%'>");

        messageBody.append("<style>td { text-align: center; }</style>");

        messageBody.append("<tr>");
        messageBody.append("<th><b>User Name</b></th>");
        messageBody.append("<th><b>Email</b></th>");
        messageBody.append("<th><b>Message</b></th>");
        messageBody.append("</tr>");

        messageBody.append("<tr>");
        messageBody.append("<td><div style='text-align: center;'>").append(userData.getName()).append("</div></td>");
        messageBody.append("<td><div style='text-align: center;'>").append(userData.getEmail()).append("</div></td>");
        messageBody.append("<td><div style='text-align: center;'>").append(userData.getMessage()).append("</div></td>");
        messageBody.append("</tr>");

        messageBody.append("</table>");

        messageBody.append("<br><br><br><br>");
        messageBody.append("<p>Best Regards<br><strong>NanduBus.in</strong></p>");
        messageBody.append("<br>");

        messageBody.append("<img src='cid:logoImage' width='80' height='75'>");
        messageBody.append("</body></html>");

        helper.setTo(toEmailAddress);
        helper.setSubject("Pay attention: User Details to get in touch ");
        helper.setText(messageBody.toString(), true);
        helper.addInline("logoImage", new File(localLogoPath));

        javaMailSender.send(message);
    }

    private BookingInfo getBookingInfo(VehicleEntity vehicleEntity, BookingEntity bookingEntity) {
        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setVehicleNumber(bookingEntity.getVehicleNumber());
        bookingInfo.setToDate(localDateFormat.format(bookingEntity.getToDate()));
        bookingInfo.setFromDate(localDateFormat.format(bookingEntity.getFromDate()));
        bookingInfo.setBookingDate(localDateFormatForBookingDate.format(bookingEntity.getBookingDate()));
        bookingInfo.setDriverName(vehicleEntity.getDriverName());
        bookingInfo.setDriverNumber(vehicleEntity.getDriverNumber());
        bookingInfo.setAlternateNumber(vehicleEntity.getAlternateNumber());
        return bookingInfo;
    }

    @Override
    public String confirmBooking(String bookingId) {
        BookingEntity bookingEntity = bookingRepo.getByBookingId(bookingId);
        validateBookingEntity(bookingEntity);
        bookingEntity.setBookingStatus(BookingStatusEnum.CONFIRMED.getCode());
        bookingRepo.save(bookingEntity);
        return "Booking is Confirmed";
    }

    @Override
    public String declineBooking(String bookingId) {
        BookingEntity bookingEntity = bookingRepo.getByBookingId(bookingId);
        validateBookingEntity(bookingEntity);
        bookingEntity.setBookingStatus(BookingStatusEnum.DECLINED.getCode());
        bookingRepo.save(bookingEntity);
        return "Booking is Declined";
    }

    @Override
    public VehicleBooked getBookedSlotsByVehicleNumber(String vehicleNumber) {
        VehicleBooked vehicleBooked = new VehicleBooked();
        Slots slots = new Slots();
        List<BookedDates> bookedDatesList = new ArrayList<>();

        List<SlotsEntity> slotsEntityList = slotsRepo.getByVehicleNUmber(vehicleNumber);
        if (slotsEntityList.isEmpty()) {
            throw new BookingException(ResStatus.SLOTS_NOT_FOUND);
        }

        for (SlotsEntity slotsEntity : slotsEntityList) {
            bookedDatesList.add(new BookedDates(slotsEntity.getFromDate(), Boolean.TRUE));

            List<LocalDate> inBetweenDates = generateInBetweenDates(slotsEntity.getFromDate(), slotsEntity.getToDate());
            for (LocalDate date : inBetweenDates) {
                bookedDatesList.add(new BookedDates(date, Boolean.TRUE));
            }

            bookedDatesList.add(new BookedDates(slotsEntity.getToDate(), Boolean.TRUE));
        }

        slots.setVehicleNumber(vehicleNumber);
        slots.setDates(bookedDatesList);
        vehicleBooked.setSlots(slots);
        return vehicleBooked;
    }

    @Override
    public List<VehiclePojo> getVehicleAvailability(VehiclesAvailable vehiclesAvailable) {
        List<VehiclePojo> vehiclePojos = new ArrayList<>();
        List<String> unavailableVehicleList = slotsRepo.getUnavailableList(vehiclesAvailable.getFromDate(), vehiclesAvailable.getToDate());
        List<VehicleEntity> vehicleEntities = vehicleInfoRepo.getAvailableVehicle(unavailableVehicleList, vehiclesAvailable.getIsAC(), vehiclesAvailable.getIsSleeper());
        getVehiclePojo(vehiclePojos, vehicleEntities);
        return vehiclePojos;

    }

    private void getVehiclePojo(List<VehiclePojo> vehiclePojos, List<VehicleEntity> vehicleEntities) {
        for (VehicleEntity vehicleEntity : vehicleEntities) {
            VehiclePojo vehiclePojo = new VehiclePojo();
            vehiclePojo.setSeatCapacity(vehicleEntity.getSeatCapacity());
            vehiclePojo.setVehicleNumber(vehicleEntity.getVehicleNumber());
            vehiclePojo.setImageUrl(vehicleEntity.getS3ImageUrl());
            vehiclePojo.setIsVehicleAC(vehicleEntity.getIsVehicleAC());
            vehiclePojo.setIsVehicleSleeper(vehicleEntity.getIsVehicleSleeper());

            vehiclePojos.add(vehiclePojo);
        }
    }

    private List<LocalDate> generateInBetweenDates(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> inBetweenDates = new ArrayList<>();

        LocalDate currentDate = startDate.plusDays(1);

        while (currentDate.isBefore(endDate)) {
            inBetweenDates.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        return inBetweenDates;
    }

    private void getVehicleDetails(BookingDetails bookingDetails) {
        VehicleEntity vehicleEntity = vehicleInfoRepo.getByVehicleNumber(bookingDetails.getSlots().getVehicleNumber());
        validateVehicleEntity(vehicleEntity);
        VehiclePojo vehiclePojo = new VehiclePojo();
        vehiclePojo.setVehicleNumber(vehicleEntity.getVehicleNumber());
        vehiclePojo.setSeatCapacity(vehicleEntity.getSeatCapacity());
        vehiclePojo.setIsVehicleSleeper(vehicleEntity.getIsVehicleSleeper());
        vehiclePojo.setIsVehicleAC(vehicleEntity.getIsVehicleAC());
        vehiclePojo.setImageUrl(vehicleEntity.getS3ImageUrl());
        bookingDetails.setVehicle(vehiclePojo);
    }

    private void getSlot(String bookingId, BookingDetails bookingDetails) {
        SlotsEntity slotsEntity = slotsRepo.findByBookingId(bookingId);
        validateSlotEntity(slotsEntity);
        SlotsPojo slotsPojo = new SlotsPojo();
        slotsPojo.setVehicleNumber(slotsEntity.getVehicleNumber());
        slotsPojo.setFromDate(localDateFormat.format(slotsEntity.getFromDate()));
        slotsPojo.setToDate(localDateFormat.format(slotsEntity.getToDate()));
        bookingDetails.setSlots(slotsPojo);
    }

    private void getUser(BookingEntity bookingEntity, BookingDetails bookingDetails) {
        UserEntity user = userRepo.getUserByMobileNumber(bookingEntity.getMobile());
        validateUserEntity(user);
        UserPojo userPojo = new UserPojo();
        userPojo.setMobile(user.getMobile());
        userPojo.setFirstName(user.getFirstName());
        userPojo.setMiddleName(user.getMiddleName());
        userPojo.setLastName(user.getLastName());
        userPojo.setEmail(user.getEmail());
        bookingDetails.setUser(userPojo);
    }

    private void validateBookingEntity(BookingEntity bookingEntity) {
        if (bookingEntity == null) {
            throw new BookingException(ResStatus.BOOKING_NOT_FOUND);
        }
    }

    private void validateVehicleEntity(VehicleEntity vehicleEntity) {
        if (vehicleEntity == null) {
            throw new BookingException(ResStatus.VEHICLE_NOT_FOUND);
        }
    }

    private void validateUserEntity(UserEntity userEntity) {
        if (userEntity == null) {
            throw new BookingException(ResStatus.USER_NOT_FOUND);
        }
    }

    private void validateSlotEntity(SlotsEntity slotsEntity) {
        if (slotsEntity == null) {
            throw new BookingException(ResStatus.SLOTS_NOT_FOUND);
        }
    }
}
