package hust.project.restaurant_management.mapper;

import hust.project.restaurant_management.entity.ShipperEntity;
import hust.project.restaurant_management.entity.UserEntity;
import hust.project.restaurant_management.entity.dto.request.CreateShipperRequest;
import hust.project.restaurant_management.entity.dto.request.UpdateShipperRequest;
import hust.project.restaurant_management.entity.dto.response.ShipperResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ShipperMapper {

    public ShipperEntity toEntityFromCreateRequest(CreateShipperRequest request) {
        return ShipperEntity.builder()
                .userId(request.getUserId())
                .vehicleType(request.getVehicleType())
                .vehiclePlateNumber(request.getVehiclePlateNumber())
                .isAvailable(true)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .currentAddress(request.getCurrentAddress())
                .lastLocationUpdateTime(LocalDateTime.now())
                .status(request.getStatus())
                .maxConcurrentOrders(request.getMaxConcurrentOrders())
                .currentOrderCount(0)
                .rating(0.0)
                .completedDeliveries(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void updateEntityFromRequest(ShipperEntity entity, UpdateShipperRequest request) {
        if (request.getVehicleType() != null) {
            entity.setVehicleType(request.getVehicleType());
        }
        
        if (request.getVehiclePlateNumber() != null) {
            entity.setVehiclePlateNumber(request.getVehiclePlateNumber());
        }
        
        if (request.getIsAvailable() != null) {
            entity.setIsAvailable(request.getIsAvailable());
        }
        
        if (request.getLatitude() != null) {
            entity.setLatitude(request.getLatitude());
        }
        
        if (request.getLongitude() != null) {
            entity.setLongitude(request.getLongitude());
        }
        
        if (request.getCurrentAddress() != null) {
            entity.setCurrentAddress(request.getCurrentAddress());
        }
        
        // Nếu đã cập nhật vị trí (lat/long), cập nhật thời gian
        if (request.getLatitude() != null || request.getLongitude() != null) {
            entity.setLastLocationUpdateTime(LocalDateTime.now());
        }
        
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        
        if (request.getMaxConcurrentOrders() != null) {
            entity.setMaxConcurrentOrders(request.getMaxConcurrentOrders());
        }
        
        entity.setUpdatedAt(LocalDateTime.now());
    }

    public ShipperResponse toResponse(ShipperEntity entity) {
        String fullName = null;
        String phone = null;
        
        if (entity.getUser() != null) {
            UserEntity user = entity.getUser();
            fullName = user.getName();
            phone = user.getPhoneNumber();
        }
        
        return ShipperResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .fullName(fullName)
                .phone(phone)
                .vehicleType(entity.getVehicleType())
                .vehiclePlateNumber(entity.getVehiclePlateNumber())
                .isAvailable(entity.getIsAvailable())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .currentAddress(entity.getCurrentAddress())
                .lastLocationUpdateTime(entity.getLastLocationUpdateTime())
                .status(entity.getStatus())
                .maxConcurrentOrders(entity.getMaxConcurrentOrders())
                .currentOrderCount(entity.getCurrentOrderCount())
                .rating(entity.getRating())
                .completedDeliveries(entity.getCompletedDeliveries())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<ShipperResponse> toResponseList(List<ShipperEntity> entities) {
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}