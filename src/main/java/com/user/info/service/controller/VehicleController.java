package com.user.info.service.controller;

import com.user.info.service.dto.VehicleDto;
import com.user.info.service.exception.ResStatus;
import com.user.info.service.exception.VehicleNumberException;
import com.user.info.service.model.VRNValidation;
import com.user.info.service.service.VehicleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
@EnableSwagger2
@CrossOrigin(origins = {"http://localhost:8100","http://nandubus.in"})
public class VehicleController {
    @Autowired
    private VehicleService vehicleService;


    @PostMapping("/addVehicle")
    ResponseEntity<VehicleDto> addVehicle(@ModelAttribute VehicleDto vehicleDto, @RequestParam("files") List<MultipartFile> images ) throws IOException {
        try {
            checkNumber(vehicleDto);
            return new ResponseEntity<>(vehicleService.addVehicle(vehicleDto, images), HttpStatus.OK);
        } catch (Exception e) {
            log.info("exception");
            throw e;
        }
    }

    private void checkNumber(VehicleDto vehicleDto) {
        if (vehicleDto.getVehicleNumber() == null) {
            throw new VehicleNumberException(ResStatus.VEHICLE_NUMBER);
        }
        if (!VRNValidation.isValid(vehicleDto.getVehicleNumber())) {
            throw new VehicleNumberException(ResStatus.INVALID_NUMBER);
        }
    }

    @PutMapping("/updateVehicle")
    ResponseEntity<VehicleDto> updateVehicle(@RequestParam("files") List<MultipartFile> images, @ModelAttribute VehicleDto vehicleDto) throws IOException {
        try {
            checkNumber(vehicleDto);
            return new ResponseEntity<>(vehicleService.updateVehicle(vehicleDto,images), HttpStatus.OK);
        } catch (Exception e) {
            log.info("exception");
            throw e;
        }
    }

    @GetMapping("/getVehicle")
    ResponseEntity<VehicleDto> getVehicle(@RequestParam("vehicleNumber") @Valid String vehicleNumber) {
        return new ResponseEntity<>(vehicleService.getVehicle(vehicleNumber), HttpStatus.OK);
    }

    @DeleteMapping("/deleteVehicle")
    String deleteBooking(@RequestParam("vehicleNumber") @Valid String vehicleNumber) {
        return vehicleService.deleteVehicle(vehicleNumber);
    }
    @GetMapping("/listVehicles")
    ResponseEntity<List<VehicleDto>> listVehicles() {
        return new ResponseEntity<>(vehicleService.listAllVehicles(), HttpStatus.OK);
    }

}
