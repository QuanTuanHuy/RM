package hust.project.restaurant_management.entity.dto.response;

import hust.project.restaurant_management.constants.ShipperStatusEnum;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipperResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String phone;
    private String vehicleType;
    private String vehiclePlateNumber;
    private Boolean isAvailable;
    private Double latitude;
    private Double longitude;
    private String currentAddress;
    private LocalDateTime lastLocationUpdateTime;
    private ShipperStatusEnum status;
    private Integer maxConcurrentOrders;
    private Integer currentOrderCount;
    private Double rating;
    private Integer completedDeliveries;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}