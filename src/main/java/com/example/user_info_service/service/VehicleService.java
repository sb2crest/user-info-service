package com.example.user_info_service.service;

import com.example.user_info_service.dto.VehicleDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface VehicleService {
//    VehicleDto addVehicle(VehicleDto vehiclePojo, List<MultipartFile> images) throws IOException;
    VehicleDto getVehicle(String vehicleNumber);
 //   VehicleDto updateVehicle(VehicleDto vehiclePojo, List<MultipartFile> images) throws IOException;
    String deleteVehicle(String vehicleNumber);

    List<VehicleDto> listAllVehicles();
}
