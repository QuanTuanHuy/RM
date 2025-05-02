package hust.project.restaurant_management.entity;

import hust.project.restaurant_management.constants.ShipperStatusEnum;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipperEntity {
    private Long id;
    
    private Long userId;
    
    private String vehicleType;
    
    private String vehiclePlateNumber;
    
    private Boolean isAvailable;
    
    private Double latitude;
    
    private Double longitude;
    
    private String currentAddress;
    
    private LocalDateTime lastLocationUpdateTime;
    
    private ShipperStatusEnum status;
    
    // Các trường bổ sung cho thuật toán phân công
    @Builder.Default
    private Integer currentOrderCount = 0;
    
    @Builder.Default
    private Integer maxConcurrentOrders = 3;
    
    @Builder.Default
    private Integer completedDeliveries = 0;
    
    @Builder.Default
    private Double rating = 5.0; // Mặc định đánh giá 5 sao

    private UserEntity user;
}