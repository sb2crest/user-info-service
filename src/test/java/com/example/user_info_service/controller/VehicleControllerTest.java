package com.example.user_info_service.controller;

import com.example.user_info_service.dto.VehicleDto;
import com.example.user_info_service.exception.VehicleNumberException;
import com.example.user_info_service.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VehicleController.class)
@RunWith(SpringRunner.class)
@EnableWebMvc
class VehicleControllerTest {
    @MockBean
    VehicleService vehicleService;
    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    void testAddVehicle_Success() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile("files", "image.jpg", "image/jpeg", new byte[0]);
        Mockito.when(vehicleService.addVehicle(any(), any())).thenReturn(getVehiclePojo());
        mvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, "/addVehicle")
                        .file(imageFile)
                        .param("isVehicleAC", String.valueOf(false))
                        .param("isVehicleSleeper", String.valueOf(true))
                        .param("seatCapacity", "4")
                        .param("vehicleNumber", "KA09A2313")
                )
                .andExpect(status().isOk());
        verify(vehicleService, times(1)).addVehicle(any(), any());
    }

    @Test
    void addVehicleForInvalidNumber() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile("files", "image.jpg", "image/jpeg", new byte[0]);
        Mockito.when(vehicleService.addVehicle(any(), any())).thenReturn(getVehiclePojo());
        mvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, "/addVehicle")
                        .file(imageFile)
                        .param("isVehicleAC", String.valueOf(false))
                        .param("isVehicleSleeper", String.valueOf(true))
                        .param("seatCapacity", "4")
                        .param("vehicleNumber", "K313")
                )
                .andExpect(status().isInternalServerError());
    }

    @Test
    void addVehicleWithNoNumber() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile("files", "image.jpg", "image/jpeg", new byte[0]);
        Mockito.when(vehicleService.addVehicle(any(), any())).thenReturn(getVehiclePojo());
        mvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, "/addVehicle")
                        .file(imageFile)
                        .param("isVehicleAC", String.valueOf(true))
                        .param("isVehicleSleeper", String.valueOf(false))
                        .param("seatCapacity", "4")
                        .param("vehicleNumber", (String) null)
                )
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateVehicleWithNoNumber() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "files",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );
        VehicleDto vehiclePojo = new VehicleDto();
        vehiclePojo.setSeatCapacity(4);
        vehiclePojo.setIsVehicleAC(true);
        vehiclePojo.setIsVehicleSleeper(true);
        when(vehicleService.updateVehicle(any(VehicleDto.class), Collections.singletonList(any(MultipartFile.class))))
                .thenThrow(VehicleNumberException.class);
        mvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, "/updateVehicle")
                        .file(image)
                        .param("vehicleNumber", vehiclePojo.getVehicleNumber())
                        .param("seatCapacity", String.valueOf(vehiclePojo.getSeatCapacity()))
                        .param("isVehicleAC", String.valueOf(vehiclePojo.getIsVehicleAC()))
                        .param("isVehicleSleeper", String.valueOf(vehiclePojo.getIsVehicleSleeper()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void updateVehicleForInvalidNumber() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "files",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );
        VehicleDto vehiclePojo = new VehicleDto();
        vehiclePojo.setVehicleNumber("AB02C123");
        vehiclePojo.setSeatCapacity(4);
        vehiclePojo.setIsVehicleAC(true);
        vehiclePojo.setIsVehicleSleeper(true);
        when(vehicleService.updateVehicle(any(VehicleDto.class), Collections.singletonList(any(MultipartFile.class))))
                .thenThrow(VehicleNumberException.class);
        mvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, "/updateVehicle")
                        .file(image)
                        .param("vehicleNumber", vehiclePojo.getVehicleNumber())
                        .param("seatCapacity", String.valueOf(vehiclePojo.getSeatCapacity()))
                        .param("isVehicleAC", String.valueOf(vehiclePojo.getIsVehicleAC()))
                        .param("isVehicleSleeper", String.valueOf(vehiclePojo.getIsVehicleSleeper()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void updateVehicle() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "files",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );
        VehicleDto vehiclePojo = new VehicleDto();
        vehiclePojo.setVehicleNumber("AB02C1234");
        vehiclePojo.setSeatCapacity(4);
        vehiclePojo.setIsVehicleAC(true);
        vehiclePojo.setIsVehicleSleeper(true);
        doReturn(getVehiclePojo()).when(vehicleService).updateVehicle(any(VehicleDto.class), anyList());
        mvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, "/updateVehicle")
                        .file(image)
                        .param("vehicleNumber", vehiclePojo.getVehicleNumber())
                        .param("seatCapacity", String.valueOf(vehiclePojo.getSeatCapacity()))
                        .param("isVehicleAC", String.valueOf(vehiclePojo.getIsVehicleAC()))
                        .param("isVehicleSleeper", String.valueOf(vehiclePojo.getIsVehicleSleeper()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
        verify(vehicleService, Mockito.times(1))
                .updateVehicle(any(VehicleDto.class), anyList());

    }

    @Test
    void deleteBooking() throws Exception {
        Mockito.when(vehicleService.deleteVehicle(Mockito.anyString())).thenReturn("deleted Successfully");
        mvc.perform(delete("/deleteVehicle").param("vehicleNumber", "12")
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    void deleteBookingWhenVehicleDoesNotExistInDB() throws Exception {
        Mockito.when(vehicleService.deleteVehicle(Mockito.anyString())).thenReturn("no vehicle with this number 12");
        mvc.perform(delete("/deleteVehicle").param("vehicleNumber", "12")
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    void getVehicle() throws Exception {
        Mockito.when(vehicleService.getVehicle(Mockito.anyString())).thenReturn(getVehiclePojo());
        mvc.perform(get("/getVehicle").param("vehicleNumber", "12")
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    void getVehicleWhenNoMatchingDataExists() throws Exception {
        Mockito.when(vehicleService.getVehicle(Mockito.anyString())).thenReturn(null);
        mvc.perform(get("/getVehicle").param("vehicleNumber", "12")
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    VehicleDto getVehiclePojo() {
        VehicleDto vehiclePojo = new VehicleDto();
        vehiclePojo.setSeatCapacity(4);
        vehiclePojo.setVehicleNumber("KA12ab1234");
        return vehiclePojo;
    }
}