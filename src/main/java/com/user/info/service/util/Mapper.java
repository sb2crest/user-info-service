package com.user.info.service.util;

import com.example.user_info_service.dto.*;
import com.example.user_info_service.entity.*;
import com.user.info.service.dto.*;
import com.user.info.service.entity.*;
import com.user.info.service.exception.ACType;
import com.user.info.service.exception.SleeperType;
import com.user.info.service.model.BookingStatusEnum;
import com.example.user_info_service.repository.*;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.user.info.service.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.user.info.service.util.Validation.*;


@Component
public class Mapper {

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private SlotsRepo slotsRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    VehicleInfoRepo vehicleInfoRepo;

    private final DateTimeFormatter localDateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final DateTimeFormatter localDateFormatForBookingDate = DateTimeFormatter.ofPattern("EEEE, MMMM dd yyyy");


    public void saveBooking(BookingEntity bookingEntity, BookingDto bookingDto) {
        bookingEntity.setVehicleNumber(bookingDto.getVehicleNumber());
        bookingEntity.setFromDate(bookingDto.getFromDate());
        bookingEntity.setToDate(bookingDto.getToDate());
        bookingEntity.setBookingId(generateId());
        bookingEntity.setMobile(bookingDto.getUser().getMobile());
        bookingEntity.setBookingStatus(BookingStatusEnum.ENQUIRY.getCode());

        LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        bookingEntity.setBookingDate(currentDateTime);

        bookingEntity.setTotalAmount(bookingDto.getTotalAmount());
        bookingRepo.save(bookingEntity);
    }

    public void saveUser(BookingDto bookingDto) {
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

    public void saveSlot(BookingEntity bookingEntity) {
        SlotsEntity slotsEntity = new SlotsEntity();
        slotsEntity.setIsAvailable(false);
        slotsEntity.setFromDate(bookingEntity.getFromDate());
        slotsEntity.setToDate(bookingEntity.getToDate());
        slotsEntity.setVehicleNumber(bookingEntity.getVehicleNumber());
        slotsEntity.setBookingId(bookingEntity.getBookingId());
        slotsRepo.save(slotsEntity);
    }

    private String generateId() {
        UUID uuid = UUID.randomUUID();
        String uuId = uuid.toString().replaceAll("-", "").substring(0, 9);
        return "NB" + uuId;
    }

    public void getBookingData(BookingDetails bookingDetails, BookingEntity bookingEntity) {
        bookingDetails.setBookingId(bookingEntity.getBookingId());
        bookingDetails.setBookingStatus(BookingStatusEnum.getDesc(bookingEntity.getBookingStatus()));
        bookingDetails.setBookingDate(LocalDate.from(bookingEntity.getBookingDate()));
        if (BookingStatusEnum.BOOKED.getCode().equalsIgnoreCase(bookingEntity.getBookingStatus()) || BookingStatusEnum.COMPLETED.getCode().equalsIgnoreCase(bookingEntity.getBookingStatus())) {
            bookingDetails.setTotalAmt(bookingEntity.getTotalAmount());
            bookingDetails.setAdvancedPaid(getAmount(bookingEntity));
            bookingDetails.setRemainingAmt(bookingDetails.getTotalAmt() - bookingDetails.getAdvancedPaid());
        }
        SlotsDto slotsDto = new SlotsDto();
        slotsDto.setFromDate(localDateFormat.format(bookingEntity.getFromDate()));
        slotsDto.setToDate(localDateFormat.format(bookingEntity.getToDate()));
        bookingDetails.setSlots(slotsDto);
    }

    public Double getAmount(BookingEntity bookingEntity) {
        List<PaymentEntity> paymentEntities = paymentRepository.findByBookingId(bookingEntity.getBookingId());
        validatePaymentEntityList(paymentEntities);
        return paymentEntities.stream().mapToDouble(PaymentEntity::getAmount).sum();
    }

    public void getUser(BookingEntity bookingEntity, BookingDetails bookingDetails) {
        UserEntity user = userRepo.getUserByMobileNumber(bookingEntity.getMobile());
        validateUserEntity(user);
        UserDto userDto = new UserDto();
        userDto.setFirstName(user.getFirstName());
        userDto.setMiddleName(user.getMiddleName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        bookingDetails.setUser(userDto);
    }

    public BookingInfo getBookingInfo(VehicleEntity vehicleEntity, BookingEntity bookingEntity) {
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
        bookingInfo.setTotalAmt(bookingEntity.getTotalAmount());
        bookingInfo.setAdvancedPaid(bookingEntity.getAdvanceAmountPaid());
        bookingInfo.setRemainingAmt(bookingEntity.getRemainingAmount());
        return bookingInfo;
    }

    public void getVehicleDetails(BookingDetails bookingDetails, String vehicleNumber) {
        VehicleEntity vehicleEntity = vehicleInfoRepo.getByVehicleNumber(vehicleNumber);
        validateVehicleEntity(vehicleEntity);
        VehicleDto vehicleDto = new VehicleDto();
        vehicleDto.setVehicleNumber(vehicleEntity.getVehicleNumber());
        vehicleDto.setSeatCapacity(vehicleEntity.getSeatCapacity());
        getVehicleFilterDetails(vehicleDto, vehicleEntity);
        bookingDetails.setVehicle(vehicleDto);
    }

    public VehicleDto getVehiclePojo(DestinationResponse destinationResponses, VehicleEntity vehicleEntity) {
        VehicleDto vehicleDto = new VehicleDto();
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
        getVehicleFilterDetails(vehicleDto, vehicleEntity);
        return vehicleDto;
    }

    public void getVehicleFilterDetails(VehicleDto vehicleDto, VehicleEntity vehicleEntity) {
        if (vehicleEntity.getFilter() != null) {
            String[] filterDetails = CommonFunction.splitUsingSlash(vehicleEntity.getFilter());
            vehicleDto.setVehicleAC(ACType.getDescByCode(filterDetails[0]));
            vehicleDto.setSleeper(SleeperType.getDescByCode(filterDetails[1]));
        }
    }

    public void getFilterDetailsForBooking(BookingInfo bookingInfo, VehicleEntity vehicleEntity) {
        String[] filterDetails = CommonFunction.splitUsingSlash(vehicleEntity.getFilter());
        bookingInfo.setVehicleAC(ACType.getDescByCode(filterDetails[0]));
        bookingInfo.setSleeper(SleeperType.getDescByCode(filterDetails[1]));
    }

    public DistanceRequest getDistanceRequestDetails(VehiclesAvailable vehiclesAvailable, List<String> vehicleNumbers) {
        DistanceRequest distanceRequest = new DistanceRequest();
        distanceRequest.setSource(vehiclesAvailable.getDistanceRequest().getSource());
        distanceRequest.setDestination(vehiclesAvailable.getDistanceRequest().getDestination());
        distanceRequest.setSourceLatitude(vehiclesAvailable.getDistanceRequest().getSourceLatitude());
        distanceRequest.setSourceLongitude(vehiclesAvailable.getDistanceRequest().getSourceLongitude());
        distanceRequest.setDestinationLatitude(vehiclesAvailable.getDistanceRequest().getDestinationLatitude());
        distanceRequest.setDestinationLongitude(vehiclesAvailable.getDistanceRequest().getDestinationLongitude());
        distanceRequest.setMultipleDestination(vehiclesAvailable.getDistanceRequest().getMultipleDestination());
        distanceRequest.setVehicleNumbers(vehicleNumbers);
        return distanceRequest;
    }

    public List<LocalDate> generateInBetweenDates(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> inBetweenDates = new ArrayList<>();

        LocalDate currentDate = startDate.plusDays(1);

        while (currentDate.isBefore(endDate)) {
            inBetweenDates.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        return inBetweenDates;
    }

    public static class HeaderFooterEventHandler implements IEventHandler {
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
