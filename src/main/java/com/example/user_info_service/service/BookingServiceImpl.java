package com.example.user_info_service.service;

import com.example.user_info_service.entity.*;
import com.example.user_info_service.exception.BookingException;
import com.example.user_info_service.exception.ResStatus;
import com.example.user_info_service.model.BookingStatusEnum;
import com.example.user_info_service.dto.*;
import com.example.user_info_service.repository.*;
import com.example.user_info_service.util.CommonFunction;
import com.example.user_info_service.util.Mapper;
import com.example.user_info_service.util.Validation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.user_info_service.util.Validation.*;


@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    @Autowired
    SlotsRepo slotsRepo;

    @Autowired
    BookingRepo bookingRepo;

    @Autowired
    VehicleInfoRepo vehicleInfoRepo;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private DestinationServiceImpl destinationServiceImpl;

    @Autowired
    Mapper mapper;

    @Value("${mail.to.username}")
    private String toEmailAddress;

    @Value("${nandu.bus.image}")
    private String logo;

    @Value("${getInTouch.email.template}")
    private String emailPath;


    private final DateTimeFormatter localDateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");

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
            mapper.saveUser(bookingDto);
            mapper.saveBooking(bookingEntity, bookingDto);
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
            mapper.getBookingData(bookingDetails, bookingEntity);
            mapper.getUser(bookingEntity, bookingDetails);
            mapper.getSlot(bookingEntity.getBookingId(), bookingDetails);
            mapper.getVehicleDetails(bookingDetails, bookingEntity.getVehicleNumber());
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
            BookingInfo bookingInfo = mapper.getBookingInfo(vehicleEntity, bookingEntity);
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

    @Override
    public void getInTouch(UserData userData) {
        userEmailValidation(userData.getEmail());
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String emailTemplate = readEmailTemplate();

            String messageBody = emailTemplate
                    .replace("{user_name}", Objects.toString(userData.getName(), ""))
                    .replace("{user_email}", Objects.toString(userData.getEmail(), ""))
                    .replace("{user_message}", Objects.toString(userData.getMessage(), ""))
                    .replace("{logo}", Objects.toString(logo, ""));

            helper.setTo(toEmailAddress);
            helper.setSubject("Pay attention: User Details to get in touch ");
            helper.setText(messageBody, true);

            javaMailSender.send(message);
        } catch (Exception e) {
            throw new BookingException(ResStatus.ERROR_WHILE_SENDING_EMAIL);
        }
    }

    private String readEmailTemplate() {
        try {
            InputStream inputStream = new ClassPathResource(emailPath).getInputStream();
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error reading email template", e);
            throw new BookingException(ResStatus.ERROR_WHILE_READING_EMAIL_PATH);
        }
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

            List<LocalDate> inBetweenDates = mapper.generateInBetweenDates(slotsEntity.getFromDate(), slotsEntity.getToDate());
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
            LocalDate fromDate = LocalDate.parse(vehiclesAvailable.getFromDate(), localDateFormat);
            LocalDate toDate = LocalDate.parse(vehiclesAvailable.getToDate(), localDateFormat);
            List<VehicleEntity> vehicleEntities = vehicleInfoRepo.getAvailableVehicle(filterDetails, fromDate, toDate);

            if (vehicleEntities == null || vehicleEntities.isEmpty()) {
                throw new BookingException(ResStatus.VEHICLE_NOT_AVAILABLE);
            }
            List<String> uniqueVehicleNumbers = vehicleEntities.stream()
                    .map(VehicleEntity::getVehicleNumber)
                    .collect(Collectors.toList());

            DistanceRequest distanceRequest = mapper.getDistanceRequestDetails(vehiclesAvailable, uniqueVehicleNumbers);
            List<DestinationResponse> destinationResponses = destinationServiceImpl.getAmountDetails(distanceRequest);

            Map<String, DestinationResponse> destinationResponseMap = destinationResponses.stream()
                    .filter(response -> response.getVehicleNumber() != null && !response.getVehicleNumber().isEmpty())
                    .collect(Collectors.toMap(DestinationResponse::getVehicleNumber, Function.identity()));

            for (VehicleEntity vehicleEntity : vehicleEntities) {
                String vehicleNumber = vehicleEntity.getVehicleNumber();
                DestinationResponse destinationResponse = destinationResponseMap.get(vehicleNumber);

                if (destinationResponse != null) {
                    VehicleDto vehicleDto = mapper.getVehiclePojo(destinationResponse, vehicleEntity);
                    vehicleDtos.add(vehicleDto);
                }
            }
            return vehicleDtos;
        } catch (Exception e) {
            throw new BookingException(ResStatus.VEHICLE_NOT_AVAILABLE);
        }
    }
}