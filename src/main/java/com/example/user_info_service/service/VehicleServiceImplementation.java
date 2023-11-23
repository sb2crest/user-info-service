package com.example.user_info_service.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.user_info_service.dto.VehicleDto;
import com.example.user_info_service.entity.VehicleEntity;
import com.example.user_info_service.exception.ACType;
import com.example.user_info_service.exception.ResStatus;
import com.example.user_info_service.exception.SleeperType;
import com.example.user_info_service.exception.VehicleNumberException;
import com.example.user_info_service.repository.VehicleInfoRepo;
import com.example.user_info_service.util.CommonFunction;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.example.user_info_service.exception.ResStatus.VEHICLE_NOT_FOUND;
import static com.example.user_info_service.util.ListUtil.collectionAsStream;
import static java.util.Objects.isNull;

@Service
@Slf4j
public class VehicleServiceImplementation implements VehicleService {
    @Autowired
    VehicleInfoRepo vehicleInfoRepo;
    @Autowired
    AmazonS3 amazonS3;

    @Autowired
    private ModelMapper modelMapper;

    @Value("${aws.bucketName}")
    String s3BucketName;


    @Override
    public VehicleDto addVehicle(VehicleDto vehicleDto, List<MultipartFile> images) {
        VehicleEntity vehicleEntity;
        vehicleEntity = vehicleInfoRepo.getByVehicleNumber(vehicleDto.getVehicleNumber());
        if (vehicleEntity == null) {
            vehicleEntity = new VehicleEntity();
            vehicleEntity.setSeatCapacity(vehicleDto.getSeatCapacity());
            vehicleEntity.setVehicleNumber(vehicleDto.getVehicleNumber());
            appendFilter(vehicleEntity, vehicleDto);
            vehicleEntity.setDriverName(vehicleDto.getDriverName());
            vehicleEntity.setDriverNumber(vehicleDto.getDriverNumber());
            vehicleEntity.setAlternateNumber(vehicleDto.getAlternateNumber());
            vehicleEntity.setEmergencyNumber(vehicleDto.getEmergencyNumber());
            if (images != null) {
                List<String> s3ImageUrl = uploadImagesToS3Bucket(images, vehicleDto);
                vehicleEntity.setS3ImageUrl(s3ImageUrl);
            }
            vehicleEntity.setS3ImageUrl(vehicleEntity.getS3ImageUrl());
            VehicleEntity entity = vehicleInfoRepo.save(vehicleEntity);
            return modelMapper.map(entity, VehicleDto.class);
        } else {
            throw new VehicleNumberException(ResStatus.DUPLICATE_NUMBER);
        }

    }

    @Override
    public VehicleDto getVehicle(String vehicleNumber) {
        VehicleEntity vehicleEntity = vehicleInfoRepo.getByVehicleNumber(vehicleNumber);
        if (isNull(vehicleEntity)) {
            throw new VehicleNumberException(VEHICLE_NOT_FOUND);
        }
        VehicleDto vehicleDto = modelMapper.map(vehicleEntity, VehicleDto.class);
        getFilterDetails(vehicleDto, vehicleEntity);
        return vehicleDto;
    }

    @Override
    public VehicleDto updateVehicle(VehicleDto vehicleDto, List<MultipartFile> images) {
        VehicleEntity vehicleEntity = vehicleInfoRepo.getByVehicleNumber(vehicleDto.getVehicleNumber());
        if (vehicleEntity == null) {
            return addVehicle(vehicleDto, images);
        }
        List<String> oldImageUrl = vehicleEntity.getS3ImageUrl();
        if (oldImageUrl != null && !images.isEmpty()) {
            List<String> oldKeys = extractS3KeyFromUrl(oldImageUrl);
            for (String oldKey : oldKeys) {
                amazonS3.deleteObject(s3BucketName, oldKey);
            }
        }
        List<String> s3ImageUrl = uploadImagesToS3Bucket(images, vehicleDto);
        vehicleEntity.setS3ImageUrl(s3ImageUrl);
        vehicleEntity.setSeatCapacity(vehicleDto.getSeatCapacity());
        vehicleEntity.setVehicleNumber(vehicleDto.getVehicleNumber());
        appendFilter(vehicleEntity, vehicleDto);
        vehicleEntity.setDriverName(vehicleDto.getDriverName());
        vehicleEntity.setDriverNumber(vehicleDto.getDriverNumber());
        vehicleEntity.setAlternateNumber(vehicleDto.getAlternateNumber());
        vehicleEntity.setEmergencyNumber(vehicleDto.getEmergencyNumber());
        VehicleEntity saveResponse = vehicleInfoRepo.save(vehicleEntity);
        return modelMapper.map(saveResponse, VehicleDto.class);

    }

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
                log.error("error while uploading " + e.getMessage());
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
        collectionAsStream(vehicleEntityList)
                .forEach(entity -> {
                    VehicleDto vehicleDto = modelMapper.map(entity, VehicleDto.class);
                    getFilterDetails(vehicleDto, entity);
                    vehicleDtoList.add(vehicleDto);
                });
        return vehicleDtoList;
    }

    void appendFilter(VehicleEntity vehicleEntity, VehicleDto vehicleDto) {
        String sleeper = (vehicleDto.getSleeper() != null) ? vehicleDto.getSleeper() : SleeperType.NON_SLEEPER.getCode();
        String ac = (vehicleDto.getVehicleAC() != null) ? vehicleDto.getVehicleAC() : ACType.NON_AC.getCode();

        String filter = String.format("%s/%s", ac, sleeper);

        vehicleEntity.setFilter(filter);
    }

    private void getFilterDetails(VehicleDto vehicleDto, VehicleEntity vehicleEntity) {
        String[] filterDetails = CommonFunction.splitUsingSlash(vehicleEntity.getFilter());
        vehicleDto.setSleeper(SleeperType.getDescByCode(filterDetails[1]));
        vehicleDto.setVehicleAC(ACType.getDescByCode(filterDetails[0]));
    }
}
