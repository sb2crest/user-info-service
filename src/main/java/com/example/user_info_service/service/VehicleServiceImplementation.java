package com.example.user_info_service.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.user_info_service.dto.VehicleDto;
import com.example.user_info_service.entity.VehicleEntity;
import com.example.user_info_service.repository.VehicleInfoRepo;
import com.example.user_info_service.util.ListUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;

@Service
public class VehicleServiceImplementation implements VehicleService {
    @Autowired
    VehicleInfoRepo vehicleInfoRepo;
    @Autowired
    AmazonS3 amazonS3;

    private ModelMapper modelMapper;
    @Value("${aws.bucketName}")
    String s3BucketName;


//    @Override
//    public VehicleDto addVehicle(VehicleDto vehicleDto, List<MultipartFile> images) throws IOException {
//        VehicleEntity getByVehicleNumber = vehicleInfoRepo.getByVehicleNumber(vehicleDto.getVehicleNumber());
//        VehicleEntity vehicleEntity = modelMapper.map(vehicleDto, VehicleEntity.class);
//        if (images != null) {
//            String s3ImageUrl = uploadImagesToS3Bucket(images, vehicleDto);
//            vehicleEntity.setS3ImageUrl(s3ImageUrl);
//        }
//
//    }

    @Override
    public VehicleDto getVehicle(String vehicleNumber) {
        VehicleEntity vehicleEntity = vehicleInfoRepo.getByVehicleNumber(vehicleNumber);
        if (isNull(vehicleEntity)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not available with this " + vehicleNumber);
        }
        return modelMapper.map(vehicleEntity, VehicleDto.class);
    }

//    @Override
//    public VehicleDto updateVehicle(VehicleDto vehicleDto, List<MultipartFile> images) throws IOException {
//        VehicleEntity vehicleEntity = vehicleInfoRepo.getByVehicleNumber(vehicleDto.getVehicleNumber());
//        if (vehicleEntity == null) {
//            return addVehicle(vehicleDto, images);
//        }
//        // Delete old images from S3
//        List<String> oldImageUrl = vehicleEntity.getS3ImageUrl();
//        if (oldImageUrl != null && !images.isEmpty()) {
//            List<String> oldKeys = extractS3KeyFromUrl(oldImageUrl);
//            for (String oldKey : oldKeys) {
//                amazonS3.deleteObject(s3BucketName, oldKey);
//            }
//        }
//        // Upload new images to S3
//        List<String> s3ImageUrl = uploadImagesToS3Bucket(images, vehiclePojo);
//        vehicleEntity.setS3ImageUrl(s3ImageUrl);
//        vehicleEntity.setSeatCapacity(vehiclePojo.getSeatCapacity());
//        vehicleEntity.setVehicleNumber(vehiclePojo.getVehicleNumber());
//        vehicleEntity.setIsVehicleAC(vehiclePojo.getIsVehicleAC());
//        vehicleEntity.setIsVehicleSleeper(vehiclePojo.getIsVehicleSleeper());
//        vehicleEntity.setDriverName(vehiclePojo.getDriverName());
//        vehicleEntity.setDriverNumber(vehiclePojo.getDriverNumber());
//        vehicleEntity.setAlternateNumber(vehiclePojo.getAlternateNumber());
//        vehicleEntity.setEmergencyNumber(vehiclePojo.getEmergencyNumber());
//        return vehicleInfoRepo.save(vehicleEntity);
//    }

    List<String> extractS3KeyFromUrl(List<String> s3ImageUrls) {
        List<String> updatedS3Keys = new ArrayList<>();
        for (String s3ImageUrl : s3ImageUrls) {
            String[] parts = s3ImageUrl.split("/");
            String oldKeyName = parts[parts.length - 1];
            updatedS3Keys.add(oldKeyName);
        }
        return updatedS3Keys;
    }

    List<String> uploadImagesToS3Bucket(List<MultipartFile> images, VehicleDto vehicleDto) {
        List<String> uploadedImageUrls = new ArrayList<>();
        for (MultipartFile file : images) {
            String fileName = generateFileName(file.getOriginalFilename(), vehicleDto);
            try {
                InputStream inputStream = file.getInputStream();
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType("jpeg");
                metadata.setContentLength(file.getSize());
                amazonS3.putObject(s3BucketName, fileName, inputStream, metadata);
                String fileUrl = amazonS3.getUrl(s3BucketName, fileName).toString();
                uploadedImageUrls.add(fileUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return uploadedImageUrls;

    }


    int nameCounter = 1;

    private String generateFileName(String originalFileName, VehicleDto vehicleDto) {
        String imageName = vehicleDto.getVehicleNumber() + "_image" + nameCounter;
        String extension = getFileExtension(originalFileName);
        nameCounter++;
        return imageName + extension;
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex >= 0) {
            return fileName.substring(lastDotIndex);
        } else {
            return "";
        }
    }

    @Override
    public String deleteVehicle(String vehicleNumber) {
        VehicleEntity vehicleEntity = vehicleInfoRepo.getByVehicleNumber(vehicleNumber);
        if (vehicleEntity != null) {
            vehicleInfoRepo.delete(vehicleEntity);
            return "deleted Successfully";
        }
        return "no vehicle with this number " + vehicleNumber;
    }

    @Override
    public List<VehicleDto> listAllVehicles() {
        List<VehicleDto> vehicleDtoList = new ArrayList<>();
        List<VehicleEntity> vehicleEntityList = vehicleInfoRepo.findAll();
        ListUtil.collectionAsStream(vehicleEntityList)
                .forEach(entity -> vehicleDtoList.add(modelMapper.map(entity, VehicleDto.class)));
        return vehicleDtoList;
    }
}
