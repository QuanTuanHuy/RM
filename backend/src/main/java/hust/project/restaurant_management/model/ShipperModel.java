package hust.project.restaurant_management.model;

import hust.project.restaurant_management.constants.ShipperStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shippers")
public class ShipperModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "vehicle_type")
    private String vehicleType; // MOTORCYCLE, BICYCLE, CAR

    @Column(name = "vehicle_plate_number")
    private String vehiclePlateNumber;

    @Column(name = "is_available")
    private Boolean isAvailable;

    // Thay thế currentLocation bằng tọa độ chính xác
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "current_address")
    private String currentAddress;

    @Column(name = "last_location_update_time")
    private LocalDateTime lastLocationUpdateTime;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ShipperStatusEnum status;

    @Column(name = "max_concurrent_orders")
    private Integer maxConcurrentOrders;

    @Column(name = "current_order_count")
    private Integer currentOrderCount;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "completed_deliveries")
    private Integer completedDeliveries;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}