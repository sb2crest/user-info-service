package com.example.user_info_service.service;

import com.example.user_info_service.entity.*;
import com.example.user_info_service.exception.ACType;
import com.example.user_info_service.exception.BookingException;
import com.example.user_info_service.exception.ResStatus;
import com.example.user_info_service.exception.SleeperType;
import com.example.user_info_service.model.BookingStatusEnum;
import com.example.user_info_service.model.GmailValidator;
import com.example.user_info_service.dto.*;
import com.example.user_info_service.repository.*;
import com.example.user_info_service.util.CommonFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;
import java.text.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


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

    @Autowired
    private DestinationServiceImpl destinationServiceImpl;

    @Value("${mail.to.username}")
    private String toEmailAddress;

    @Value("${nandu.bus.image}")
    private String logo;

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
        bookingEntity.setBookingId(generateId());
        bookingEntity.setMobile(bookingDto.getUser().getMobile());
        bookingEntity.setBookingStatus(BookingStatusEnum.ENQUIRY.getCode());
        bookingEntity.setBookingDate(LocalDate.now());
        bookingEntity.setTotalAmount(bookingDto.getTotalAmount());
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
        String uuId = uuid.toString().replaceAll("-", "").substring(0, 9);
        return "NB" + uuId;
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
            if (BookingStatusEnum.BOOKED.getCode().equalsIgnoreCase(bookingEntity.getBookingStatus()) || BookingStatusEnum.ENQUIRY.getCode().equalsIgnoreCase(bookingEntity.getBookingStatus())) {
                enquiryAndBookedList.add(bookingDetails);
            } else {
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
        bookingDetails.setTotalAmt(bookingEntity.getTotalAmount());
        if (BookingStatusEnum.BOOKED.getCode().equalsIgnoreCase(bookingEntity.getBookingStatus())) {
            bookingDetails.setAdvancedPaid(getAmount(bookingEntity));
            bookingDetails.setRemainingAmt(bookingEntity.getTotalAmount() - bookingEntity.getAdvanceAmountPaid());
        }
    }

    Double getAmount(BookingEntity bookingEntity) {
        if (BookingStatusEnum.BOOKED.getCode().equalsIgnoreCase(bookingEntity.getBookingStatus()) || BookingStatusEnum.COMPLETED.getCode().equalsIgnoreCase(bookingEntity.getBookingStatus())) {
            List<PaymentEntity> paymentEntities = paymentRepository.findByBookingId(bookingEntity.getBookingId());
            validatePaymentEntityList(paymentEntities);
            return paymentEntities.stream().mapToDouble(PaymentEntity::getAmount).sum();
        }
        return null;
    }

    @Override
    public BookingAccess getBookingInfoByMobile(String mobile) {
        BookingAccess bookingAccess = new BookingAccess();
        List<BookingInfo> enquiryAndBookedList = new ArrayList<>();
        List<BookingInfo> historyList = new ArrayList<>();
        List<BookingEntity> bookingEntityList = bookingRepo.getByMobileNumber(mobile);
        validateBookingEntityList(bookingEntityList);
        for (BookingEntity bookingEntity : bookingEntityList) {
            VehicleEntity vehicleEntity = vehicleInfoRepo.getByVehicleNumber(bookingEntity.getVehicleNumber());
            validateVehicleEntity(vehicleEntity);
            BookingInfo bookingInfo = getBookingInfo(vehicleEntity, bookingEntity);
            if (BookingStatusEnum.BOOKED.getCode().equalsIgnoreCase(bookingEntity.getBookingStatus()) || BookingStatusEnum.ENQUIRY.getCode().equalsIgnoreCase(bookingEntity.getBookingStatus())) {
                enquiryAndBookedList.add(bookingInfo);
            } else {
                historyList.add(bookingInfo);
            }
        }
        bookingAccess.setUpcoming(enquiryAndBookedList);
        bookingAccess.setHistory(historyList);
        return bookingAccess;
    }

    private BookingInfo getBookingInfo(VehicleEntity vehicleEntity, BookingEntity bookingEntity) {
        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setVehicleNumber(bookingEntity.getVehicleNumber());
        bookingInfo.setToDate(localDateFormat.format(bookingEntity.getToDate()));
        bookingInfo.setFromDate(localDateFormat.format(bookingEntity.getFromDate()));
        bookingInfo.setBookingDate(localDateFormatForBookingDate.format(bookingEntity.getBookingDate()));
        if (BookingStatusEnum.BOOKED.getCode().equalsIgnoreCase(bookingEntity.getBookingStatus()) || BookingStatusEnum.COMPLETED.getCode().equalsIgnoreCase(bookingEntity.getBookingStatus())) {
            bookingInfo.setDriverName(vehicleEntity.getDriverName());
            bookingInfo.setDriverNumber(vehicleEntity.getDriverNumber());
            bookingInfo.setAlternateNumber(vehicleEntity.getAlternateNumber());
        }
        bookingInfo.setUserName(bookingEntity.getUserEntity().getFirstName() + " " + bookingEntity.getUserEntity().getLastName());
        bookingInfo.setMobile(bookingEntity.getUserEntity().getMobile());
        bookingInfo.setSeatCapacity(vehicleEntity.getSeatCapacity());
        getFilterDetailsForBooking(bookingInfo, vehicleEntity);
        bookingInfo.setBookingId(bookingEntity.getBookingId());
        bookingInfo.setBookingStatus(BookingStatusEnum.getDesc(bookingEntity.getBookingStatus()));
        bookingInfo.setAmount(getAmount(bookingEntity));
        bookingInfo.setTotalAmt(bookingInfo.getTotalAmt());
        bookingInfo.setAdvancedPaid(bookingEntity.getAdvanceAmountPaid());
        bookingInfo.setRemainingAmt(bookingInfo.getRemainingAmt());
        return bookingInfo;
    }

    @Override
    public void getInTouch(UserData userData) throws Exception {
        userEmailValidation(userData.getEmail());

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

        messageBody.append("<img src='").append(logo).append("' width='80' height='75'>");
        messageBody.append("</body></html>");

        helper.setTo(toEmailAddress);
        helper.setSubject("Pay attention: User Details to get in touch ");
        helper.setText(messageBody.toString(), true);

        javaMailSender.send(message);
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
            bookedDatesList.add(new BookedDates(localDateFormat.format(slotsEntity.getFromDate()), Boolean.TRUE));

            List<LocalDate> inBetweenDates = generateInBetweenDates(slotsEntity.getFromDate(), slotsEntity.getToDate());
            for (LocalDate date : inBetweenDates) {
                bookedDatesList.add(new BookedDates(localDateFormat.format(date), Boolean.TRUE));
            }

            bookedDatesList.add(new BookedDates(localDateFormat.format(slotsEntity.getToDate()), Boolean.TRUE));
        }

        slots.setVehicleNumber(vehicleNumber);
        slots.setDates(bookedDatesList);
        vehicleBooked.setSlots(slots);
        return vehicleBooked;
    }

    @Override
    public List<VehicleDto> getVehicleAvailability(VehiclesAvailable vehiclesAvailable) {
        try {
            List<VehicleDto> vehicleDtos = new ArrayList<>();
            List<String> filterDetails = CommonFunction.getFilterDetails(vehiclesAvailable.getFilter());
            LocalDate fromDate = vehiclesAvailable.getFromDate() == null ? LocalDate.now() : LocalDate.parse(vehiclesAvailable.getFromDate(), localDateFormat);
            LocalDate toDate = vehiclesAvailable.getToDate() == null ? fromDate.plusDays(1).plusWeeks(2) : LocalDate.parse(vehiclesAvailable.getToDate(), localDateFormat);
            List<VehicleEntity> vehicleEntities = vehicleInfoRepo.getAvailableVehicle(filterDetails, toDate, fromDate);

            if (vehicleEntities == null || vehicleEntities.isEmpty()) {
                throw new BookingException(ResStatus.VEHICLE_NOT_AVAILABLE);
            }
            List<String> uniqueVehicleNumbers = vehicleEntities.stream()
                    .map(VehicleEntity::getVehicleNumber)
                    .collect(Collectors.toList());

            DistanceRequest distanceRequest = getDistanceRequestDetails(vehiclesAvailable, uniqueVehicleNumbers);
            try {
                List<DestinationResponse> destinationResponses = destinationServiceImpl.getAmountDetails(distanceRequest);

                Map<String, DestinationResponse> destinationResponseMap = destinationResponses.stream()
                        .filter(response -> response.getVehicleNumber() != null && !response.getVehicleNumber().isEmpty())
                        .collect(Collectors.toMap(DestinationResponse::getVehicleNumber, Function.identity()));

                for (VehicleEntity vehicleEntity : vehicleEntities) {
                    String vehicleNumber = vehicleEntity.getVehicleNumber();
                    DestinationResponse destinationResponse = destinationResponseMap.get(vehicleNumber);

                    if (destinationResponse != null) {
                        VehicleDto vehicleDto = getVehiclePojo(destinationResponse, vehicleEntity);
                        vehicleDtos.add(vehicleDto);
                    }
                }
            } catch (Exception e) {
                throw new NullPointerException(e.getMessage());
            }
            return vehicleDtos;
        } catch (BookingException bookingException) {
            throw new BookingException(ResStatus.VEHICLE_NOT_AVAILABLE);
        }
    }

    private VehicleDto getVehiclePojo(DestinationResponse destinationResponses, VehicleEntity vehicleEntity) {
        VehicleDto vehicleDto = new VehicleDto();
        if (vehicleEntity != null) {
            vehicleDto.setSeatCapacity(vehicleEntity.getSeatCapacity());
            vehicleDto.setVehicleNumber(vehicleEntity.getVehicleNumber());
            vehicleDto.setS3ImageUrl(vehicleEntity.getS3ImageUrl());
            vehicleDto.setDriverName(vehicleEntity.getDriverName());
            vehicleDto.setDriverNumber(vehicleEntity.getDriverNumber());
            vehicleDto.setAlternateNumber(vehicleEntity.getAlternateNumber());
            vehicleDto.setEmergencyNumber(vehicleEntity.getEmergencyNumber());
            vehicleDto.setSource(destinationResponses.getSource());
            vehicleDto.setDestination(destinationResponses.getDestination());
            vehicleDto.setTotalAmount(destinationResponses.getTotalAmount());
            vehicleDto.setAdvanceAmt(destinationResponses.getAdvanceAmt());
            vehicleDto.setRemainingAmt(destinationResponses.getRemainingAmt());
            vehicleDto.setAmtPerKM(destinationResponses.getAmtPerKM());
        }
        getVehicleFilterDetails(vehicleDto, vehicleEntity);
        return vehicleDto;
    }

    private DistanceRequest getDistanceRequestDetails(VehiclesAvailable vehiclesAvailable, List<String> vehicleNumbers) {
        DistanceRequest distanceRequest = new DistanceRequest();
        if (vehiclesAvailable != null) {
            distanceRequest.setSource(vehiclesAvailable.getDistanceRequest().getSource());
            distanceRequest.setDestination(vehiclesAvailable.getDistanceRequest().getDestination());
            distanceRequest.setSourceLatitude(vehiclesAvailable.getDistanceRequest().getSourceLatitude());
            distanceRequest.setSourceLongitude(vehiclesAvailable.getDistanceRequest().getSourceLongitude());
            distanceRequest.setDestinationLatitude(vehiclesAvailable.getDistanceRequest().getDestinationLatitude());
            distanceRequest.setDestinationLongitude(vehiclesAvailable.getDistanceRequest().getDestinationLongitude());
            distanceRequest.setMultipleDestination(vehiclesAvailable.getDistanceRequest().getMultipleDestination());
            distanceRequest.setVehicleNumbers(vehicleNumbers);
        }
        return distanceRequest;
    }


//    private void getVehiclePojo(List<VehicleDto> vehicleDtos, List<VehicleEntity> vehicleEntities) {
//        for (VehicleEntity vehicleEntity : vehicleEntities) {
//            VehicleDto vehicleDto = new VehicleDto();
//            vehicleDto.setSeatCapacity(vehicleEntity.getSeatCapacity());
//            vehicleDto.setVehicleNumber(vehicleEntity.getVehicleNumber());
//            vehicleDto.setS3ImageUrl(vehicleEntity.getS3ImageUrl());
//            getVehicleFilterDetails(vehicleDto, vehicleEntity);
//
//            vehicleDtos.add(vehicleDto);
//        }
//    }

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
        getVehicleFilterDetails(vehicleDto, vehicleEntity);
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

    private void validatePaymentEntityList(List<PaymentEntity> paymentEntity) {
        if (paymentEntity == null) {
            throw new BookingException(ResStatus.PAYMENT_DETAILS_NOT_FOUND);
        }
    }

    private void getVehicleFilterDetails(VehicleDto vehicleDto, VehicleEntity vehicleEntity) {
        if (vehicleEntity.getFilter() != null) {
            String[] filterDetails = CommonFunction.splitUsingSlash(vehicleEntity.getFilter());
            vehicleDto.setVehicleAC(ACType.getDescByCode(filterDetails[0]));
            vehicleDto.setSleeper(SleeperType.getDescByCode(filterDetails[1]));
        }
    }

    private void getFilterDetailsForBooking(BookingInfo bookingInfo, VehicleEntity vehicleEntity) {
        String[] filterDetails = CommonFunction.splitUsingSlash(vehicleEntity.getFilter());
        bookingInfo.setVehicleAC(ACType.getDescByCode(filterDetails[0]));
        bookingInfo.setSleeper(SleeperType.getDescByCode(filterDetails[1]));
    }
}