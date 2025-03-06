package com.user.info.service.service;

import com.amazonaws.services.s3.AmazonS3;
import com.user.info.service.dto.VehicleDto;
import com.user.info.service.entity.VehicleEntity;
import com.user.info.service.exception.VehicleNumberException;
import com.user.info.service.repository.VehicleInfoRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class VehicleServiceImplementationTest {

    @Mock
    VehicleInfoRepo vehicleInfoRepo;

    @InjectMocks
    VehicleServiceImplementation bookingServiceImplementation;

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private ModelMapper modelMapper;

    @BeforeEach
    public void setUp() {
        Mockito.reset(vehicleInfoRepo);
        ReflectionTestUtils.setField(bookingServiceImplementation,"s3BucketName","abc");
    }

    @Test
    void testAddVehicle_Success() throws IOException {
        VehicleDto vehiclePojo = getVehiclePojo();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "testImage.jpg", "image/jpeg", "your file content".getBytes());
        VehicleEntity entity = getVehicleEntity();
        when(modelMapper.map(entity, VehicleDto.class)).thenReturn(vehiclePojo);
        when(vehicleInfoRepo.save(any())).thenReturn(entity);
        when(vehicleInfoRepo.getByVehicleNumber(any())).thenReturn(null);
        when(amazonS3.getUrl(anyString(), anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(1);
            return new URL("image1.jpg" + key);
        });
        VehicleDto result = bookingServiceImplementation.addVehicle(vehiclePojo, Collections.singletonList(mockMultipartFile));
        assertEquals(vehiclePojo.getVehicleNumber(), result.getVehicleNumber());
        assertEquals(vehiclePojo.getSeatCapacity(), result.getSeatCapacity());
        verify(vehicleInfoRepo, times(1)).getByVehicleNumber(any());
    }

    @Test
    void testAddVehicle_SuccessWhenFilterDataNotProvided() throws IOException {
        VehicleDto vehiclePojo = getVehiclePojo();
        VehicleEntity entity = getVehicleEntity();
        when(modelMapper.map(entity, VehicleDto.class)).thenReturn(vehiclePojo);
        when(vehicleInfoRepo.save(any())).thenReturn(entity);
        when(vehicleInfoRepo.getByVehicleNumber(any())).thenReturn(null);
        vehiclePojo.setSleeper(null);
        vehiclePojo.setVehicleAC(null);
        VehicleDto result = bookingServiceImplementation.addVehicle(vehiclePojo, null);
        assertEquals(vehiclePojo.getVehicleNumber(), result.getVehicleNumber());
        assertEquals(vehiclePojo.getSeatCapacity(), result.getSeatCapacity());
        verify(vehicleInfoRepo, times(1)).getByVehicleNumber(any());
    }

    @Test()
    void testAddVehicle_DuplicateNumber() {
        VehicleDto vehiclePojo = getVehiclePojo();
        MultipartFile image = new MockMultipartFile("testImage.jpg", "testImage.jpg", "image/jpeg", "test".getBytes());
        when(vehicleInfoRepo.getByVehicleNumber(any())).thenReturn(new VehicleEntity());
        assertThrows(VehicleNumberException.class, () ->bookingServiceImplementation.addVehicle(vehiclePojo, Collections.singletonList(image)));
    }

    @Test
    void getVehicle(){
        VehicleEntity vehicleEntity = getVehicleEntity();
        VehicleDto vehicleDto = getVehiclePojo();
        when(vehicleInfoRepo.getByVehicleNumber(Mockito.anyString())).thenReturn(vehicleEntity);
        when(modelMapper.map(vehicleEntity, VehicleDto.class)).thenReturn(vehicleDto);
        VehicleDto vehicle = bookingServiceImplementation.getVehicle("12");
        assertEquals(vehicle.getSeatCapacity(),4);
    }
    @Test
    void getVehicleWHenNoMatchingRecord(){
        when(vehicleInfoRepo.getByVehicleNumber(Mockito.anyString())).thenReturn(null);
        assertThrows(VehicleNumberException.class, ()-> bookingServiceImplementation.getVehicle("12"));
    }

    @Test
    void testUpdateVehicle_IfDataExist() throws IOException {
        VehicleDto vehiclePojo = getVehiclePojo();
        MultipartFile image = new MockMultipartFile("image.jpg", new byte[0]);
        VehicleEntity existingEntity = getVehicleEntity();
        when(modelMapper.map(existingEntity, VehicleDto.class)).thenReturn(vehiclePojo);
        when(vehicleInfoRepo.getByVehicleNumber(Mockito.anyString())).thenReturn(existingEntity);
        doNothing().when(amazonS3).deleteObject(anyString(), anyString());
        when(amazonS3.getUrl(anyString(), anyString())).thenReturn(new URL("https://bucket.s3.amazonaws.com/image.jpg"));
        when(vehicleInfoRepo.save(any(VehicleEntity.class))).thenReturn(existingEntity);
        VehicleDto result = bookingServiceImplementation.updateVehicle(vehiclePojo, Collections.singletonList(image));
        assertNotNull(result);
        assertEquals("12", result.getVehicleNumber());
    }

    @Test
    void testUpdateVehicle_When_S3ImagerUrl_Is_Null() throws IOException {
        VehicleDto vehiclePojo = getVehiclePojo();
        MultipartFile image = new MockMultipartFile("image.jpg", new byte[0]);
        VehicleEntity existingEntity = getVehicleEntity();
        existingEntity.setS3ImageUrl(null);
        when(modelMapper.map(existingEntity, VehicleDto.class)).thenReturn(vehiclePojo);
        when(vehicleInfoRepo.getByVehicleNumber(Mockito.anyString())).thenReturn(existingEntity);
        doNothing().when(amazonS3).deleteObject(anyString(), anyString());
        when(amazonS3.getUrl(anyString(), anyString())).thenReturn(new URL("https://bucket.s3.amazonaws.com/image.jpg"));
        when(vehicleInfoRepo.save(any(VehicleEntity.class))).thenReturn(existingEntity);
        VehicleDto result = bookingServiceImplementation.updateVehicle(vehiclePojo, Collections.singletonList(image));
        assertNotNull(result);
        assertEquals("12", result.getVehicleNumber());
    }

    @Test
    void testUpdateVehicle_When_Images_Is_Empty() throws IOException {
        VehicleDto vehiclePojo = getVehiclePojo();
        VehicleEntity existingEntity = getVehicleEntity();
        when(modelMapper.map(existingEntity, VehicleDto.class)).thenReturn(vehiclePojo);
        when(vehicleInfoRepo.getByVehicleNumber(Mockito.anyString())).thenReturn(existingEntity);
        doNothing().when(amazonS3).deleteObject(anyString(), anyString());
        when(amazonS3.getUrl(anyString(), anyString())).thenReturn(new URL("https://bucket.s3.amazonaws.com/image.jpg"));
        when(vehicleInfoRepo.save(any(VehicleEntity.class))).thenReturn(existingEntity);
        VehicleDto result = bookingServiceImplementation.updateVehicle(vehiclePojo, List.of());
        assertNotNull(result);
        assertEquals("12", result.getVehicleNumber());
    }
    @Test
    void testUpdateVehicle_whenItNotExist() throws IOException {
        VehicleDto vehiclePojo = getVehiclePojo();
        MultipartFile image = new MockMultipartFile("image.jpg", new byte[0]);
        VehicleEntity existingEntity = getVehicleEntity();
        when(modelMapper.map(existingEntity, VehicleDto.class)).thenReturn(vehiclePojo);
        when(vehicleInfoRepo.getByVehicleNumber(Mockito.anyString())).thenReturn(null);
        doNothing().when(amazonS3).deleteObject(anyString(), anyString());
        when(amazonS3.getUrl(anyString(), anyString())).thenReturn(new URL("https://bucket.s3.amazonaws.com/image.jpg"));
        when(vehicleInfoRepo.save(any(VehicleEntity.class))).thenReturn(existingEntity);
        VehicleDto result = bookingServiceImplementation.updateVehicle(vehiclePojo, Collections.singletonList(image));
        assertNotNull(result);
        assertEquals("12", result.getVehicleNumber());
    }

    @Test
    void testUpdateVehicle_WhichDoesNotExist() throws IOException {
        VehicleEntity existingEntity = getVehicleEntity();
        MultipartFile image = new MockMultipartFile("image.jpg", new byte[0]);
        existingEntity.setS3ImageUrl(Collections.singletonList("s3://bucket/old-image.jpg"));
        String s3ImageUrl = "https://s3.example.com/testImage.jpg";
        VehicleDto vehiclePojo = getVehiclePojo();
        when(amazonS3.getUrl(any(), any())).thenReturn(new URL(s3ImageUrl));
        when(vehicleInfoRepo.getByVehicleNumber("12")).thenReturn(existingEntity);
        VehicleDto result = bookingServiceImplementation.updateVehicle(vehiclePojo, Collections.singletonList(image));
        assertNull(result);
        verify(vehicleInfoRepo, times(1)).getByVehicleNumber(any());
    }

    @Test
    void testExtractS3KeyFromUrl() {
        List<String> s3Url = Collections.singletonList("https://bucket.s3.amazonaws.com/image.jpg");
        String result = bookingServiceImplementation.extractS3KeyFromUrl(s3Url).toString();
        assertEquals("[image.jpg]", result);
    }

    @Test
    void deleteBooking() {
        when(vehicleInfoRepo.getByVehicleNumber(Mockito.anyString())).thenReturn(getVehicleEntity());
        assertEquals("deleted Successfully",bookingServiceImplementation.deleteVehicle("12"));
    }

    @Test
    void deleteBookingForNull() {
        when(vehicleInfoRepo.getByVehicleNumber(Mockito.anyString())).thenReturn(null);
        assertEquals("no vehicle with this number 12",bookingServiceImplementation.deleteVehicle("12"));
    }
    @Test
    public void testListAllVehicles_Success() {
        List<VehicleEntity> vehicleEntityList = new ArrayList<>();
        VehicleEntity entity1 = getVehicleEntity();
        entity1.setS3ImageUrl(Collections.singletonList("image1.jpg"));

        VehicleEntity entity2 = new VehicleEntity();
        entity2.setVehicleNumber("XYZ789");
        entity2.setSeatCapacity(6);
        entity2.setFilter("NS/NC");
        entity2.setS3ImageUrl(Collections.singletonList("image2.jpg"));
        VehicleDto vehicleDto = getVehiclePojo();

        vehicleEntityList.add(entity1);
        vehicleEntityList.add(entity2);
        when(modelMapper.map(entity1, VehicleDto.class)).thenReturn(vehicleDto);
        when(modelMapper.map(entity2, VehicleDto.class)).thenReturn(vehicleDto);
        when(vehicleInfoRepo.findAll()).thenReturn(vehicleEntityList);
        List<VehicleDto> result = bookingServiceImplementation.listAllVehicles();
        assertEquals(2, result.size());

        VehicleDto pojo1 = result.get(0);
        assertEquals(4, pojo1.getSeatCapacity());
        assertEquals(Collections.singletonList("image1.jpg"), pojo1.getS3ImageUrl());
    }

    VehicleDto getVehiclePojo(){
        VehicleDto vehiclePojo =new VehicleDto();
        vehiclePojo.setSeatCapacity(4);
        vehiclePojo.setVehicleNumber("12");
        vehiclePojo.setVehicleAC("AC");
        vehiclePojo.setSleeper("FS");
        vehiclePojo.setS3ImageUrl(List.of("image1.jpg"));
        return vehiclePojo;
    }
    VehicleEntity getVehicleEntity(){
        VehicleEntity vehicleEntity =new VehicleEntity();
        vehicleEntity.setSeatCapacity(4);
        vehicleEntity.setVehicleNumber("12");
        vehicleEntity.setFilter("FS/AC");
        vehicleEntity.setS3ImageUrl(List.of("image1.jpg"));
        return vehicleEntity;
    }
}