package com.example.user_info_service.service;

import com.example.user_info_service.entity.*;
import com.example.user_info_service.exception.BookingException;
import com.example.user_info_service.exception.ResStatus;
import com.example.user_info_service.model.BookingStatusEnum;
import com.example.user_info_service.model.GmailValidator;
import com.example.user_info_service.dto.*;
import com.example.user_info_service.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

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
    PaymentRepository paymentRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${mail.to.username}")
    private String toEmailAddress;

    private final DateTimeFormatter localDateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final DateTimeFormatter localDateFormatForBookingDate = DateTimeFormatter.ofPattern("EEEE, MMMM dd yyyy");

    @Override
    @Transactional
    public BookingResponse bookingVehicle(BookingDto bookingDto) throws ParseException {
        userMobileValidation(bookingDto.getUser().getMobile());
        userEmailValidation(bookingDto.getUser().getEmail());

        BookingResponse bookingResponse = new BookingResponse();
        BookingEntity bookingEntity = new BookingEntity();
        SlotsEntity slotsEntity = new SlotsEntity();

        boolean vehicleAvailability = slotsRepo.findVehicleAvailabilityOnRequiredDate(bookingDto.getVehicleNumber(), bookingDto.getFromDate(), bookingDto.getToDate());
        if (!vehicleAvailability) {
            saveUser(bookingDto);
            saveBooking(bookingEntity, bookingDto);
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

    private void userMobileValidation(String mobile) {
        if (mobile.isEmpty()) {
            throw new BookingException(ResStatus.ENTER_NUMBER);
        }
        if (mobile.length() != 10) {
            throw new BookingException(ResStatus.MOBILE_DIGIT);
        }
    }

    private void userEmailValidation(String email) {
        if (email != null && !GmailValidator.isValidGmail(email)) {
            throw new BookingException(ResStatus.INVALID_EMAIL);
        }
    }

    private void saveBooking(BookingEntity bookingEntity, BookingDto bookingDto) {
        bookingEntity.setVehicleNumber(bookingDto.getVehicleNumber());
        bookingEntity.setFromDate(bookingDto.getFromDate());
        bookingEntity.setToDate(bookingDto.getToDate());
        bookingEntity.setBookingId(generateBookingId());
        bookingEntity.setMobile(bookingDto.getUser().getMobile());
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

    private void saveUser(BookingDto bookingDto) {
        UserEntity user = userRepo.getUserByMobileNumber(bookingDto.getUser().getMobile());
        if (user == null) {
            UserEntity userEntity = new UserEntity();
            userEntity.setFirstName(bookingDto.getUser().getFirstName());
            userEntity.setMiddleName(bookingDto.getUser().getMiddleName());
            userEntity.setLastName(bookingDto.getUser().getLastName());
            userEntity.setEmail(bookingDto.getUser().getEmail());
            userEntity.setMobile(bookingDto.getUser().getMobile());
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
    public BookingData getBookingDetails(String mobile) {
        BookingData bookingData = new BookingData();
        List<BookingDetails> enquiryAndBookedList = new ArrayList<>();
        List<BookingDetails> historyList = new ArrayList<>();

        userMobileValidation(mobile);

        List<BookingEntity> bookingEntityList = bookingRepo.getByMobileNumber(mobile);
        validateBookingEntityList(bookingEntityList);
        for (BookingEntity bookingEntity : bookingEntityList) {
            BookingDetails bookingDetails = new BookingDetails();
            getBookingData(bookingDetails, bookingEntity);
            getUser(bookingEntity, bookingDetails);
            getSlot(bookingEntity.getBookingId(), bookingDetails);
            getVehicleDetails(bookingDetails, bookingEntity.getVehicleNumber());
            if(BookingStatusEnum.BOOKED.getCode().equalsIgnoreCase(bookingEntity.getBookingStatus()) || BookingStatusEnum.ENQUIRY.getCode().equalsIgnoreCase(bookingEntity.getBookingStatus())){
                enquiryAndBookedList.add(bookingDetails);
            }
            else {
                historyList.add(bookingDetails);
            }
        }
        bookingData.setEnquiryAndBookedList(enquiryAndBookedList);
        bookingData.setHistoryList(historyList);
        return bookingData;
    }

    private void getBookingData(BookingDetails bookingDetails, BookingEntity bookingEntity) {
        bookingDetails.setBookingId(bookingEntity.getBookingId());
        bookingDetails.setBookingStatus(BookingStatusEnum.getDesc(bookingEntity.getBookingStatus()));
        bookingDetails.setBookingDate(bookingEntity.getBookingDate());
        if(BookingStatusEnum.BOOKED.getCode().equalsIgnoreCase(bookingEntity.getBookingStatus()) || BookingStatusEnum.COMPLETED.getCode().equalsIgnoreCase(bookingEntity.getBookingStatus())){
            PaymentEntity paymentEntity = paymentRepository.findByBookingId(bookingDetails.getBookingId());
            bookingDetails.setAmountPaid(paymentEntity.getAmount());
        }
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
        bookingEntity.setBookingStatus(BookingStatusEnum.BOOKED.getCode());
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
    public List<VehicleDto> getVehicleAvailability(VehiclesAvailable vehiclesAvailable) {
        List<VehicleDto> vehicleDtos = new ArrayList<>();
        List<String> unavailableVehicleList = slotsRepo.getUnavailableList(vehiclesAvailable.getFromDate(), vehiclesAvailable.getToDate());
        List<VehicleEntity> vehicleEntities = vehicleInfoRepo.getAvailableVehicle(unavailableVehicleList, vehiclesAvailable.getIsAC(), vehiclesAvailable.getIsSleeper());
        getVehiclePojo(vehicleDtos, vehicleEntities);
        return vehicleDtos;

    }

    private void getVehiclePojo(List<VehicleDto> vehicleDtos, List<VehicleEntity> vehicleEntities) {
        for (VehicleEntity vehicleEntity : vehicleEntities) {
            VehicleDto vehicleDto = new VehicleDto();
            vehicleDto.setSeatCapacity(vehicleEntity.getSeatCapacity());
            vehicleDto.setVehicleNumber(vehicleEntity.getVehicleNumber());
            vehicleDto.setImageUrl(vehicleEntity.getS3ImageUrl());
            vehicleDto.setIsVehicleAC(vehicleEntity.getIsVehicleAC());
            vehicleDto.setIsVehicleSleeper(vehicleEntity.getIsVehicleSleeper());

            vehicleDtos.add(vehicleDto);
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

    private void getVehicleDetails(BookingDetails bookingDetails, String vehicleNumber) {
        VehicleEntity vehicleEntity = vehicleInfoRepo.getByVehicleNumber(vehicleNumber);
        validateVehicleEntity(vehicleEntity);
        VehicleDto vehicleDto = new VehicleDto();
        vehicleDto.setVehicleNumber(vehicleEntity.getVehicleNumber());
        vehicleDto.setSeatCapacity(vehicleEntity.getSeatCapacity());
        vehicleDto.setIsVehicleSleeper(vehicleEntity.getIsVehicleSleeper());
        vehicleDto.setIsVehicleAC(vehicleEntity.getIsVehicleAC());
        bookingDetails.setVehicle(vehicleDto);
    }

    private void getSlot(String bookingId, BookingDetails bookingDetails) {
        SlotsEntity slotsEntity = slotsRepo.findByBookingId(bookingId);
        validateSlotEntity(slotsEntity);
        SlotsDto slotsDto = new SlotsDto();
        slotsDto.setFromDate(localDateFormat.format(slotsEntity.getFromDate()));
        slotsDto.setToDate(localDateFormat.format(slotsEntity.getToDate()));
        bookingDetails.setSlots(slotsDto);
    }

    private void getUser(BookingEntity bookingEntity, BookingDetails bookingDetails) {
        UserEntity user = userRepo.getUserByMobileNumber(bookingEntity.getMobile());
        validateUserEntity(user);
        UserDto userDto = new UserDto();
        userDto.setFirstName(user.getFirstName());
        userDto.setMiddleName(user.getMiddleName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        bookingDetails.setUser(userDto);
    }

    private void validateBookingEntity(BookingEntity bookingEntity) {
        if (bookingEntity == null) {
            throw new BookingException(ResStatus.BOOKING_NOT_FOUND);
        }
    }

    private void validateBookingEntityList(List<BookingEntity> bookingEntityList) {
        if (bookingEntityList == null) {
            throw new BookingException(ResStatus.BOOKING_DATA_NOT_FOUND_WITH_MOBILE);
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
