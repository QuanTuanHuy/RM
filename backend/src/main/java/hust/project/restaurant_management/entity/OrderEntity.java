package hust.project.restaurant_management.entity;

import hust.project.restaurant_management.constants.DeliveryStatusEnum;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {
    private Long id;
    private Long customerId;
    private Long userId;
    private String orderCode;
    private Double totalAmount;
    private Double totalCost;
    private Long numberOfPeople;
    private String orderStatus;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Long paymentId;
    private String paymentMethod;
    private String note;
    private Boolean isDeliveryOrder;
    private String deliveryAddress;
    private String deliveryInstructions;
    private Double deliveryFee;
    private LocalDateTime estimatedDeliveryTime;
    private String recipientName;
    private String recipientPhone;
    private DeliveryStatusEnum deliveryStatus;
    private Long shipperId;

    // Delivery coordinates for easier calculations
    private Double deliveryLatitude;
    private Double deliveryLongitude;

    // Restaurant coordinates for batch delivery optimization
    private Double restaurantLatitude;
    private Double restaurantLongitude;

    private String batchId;
    private Integer batchSequence;
    private LocalDateTime estimatedPickupTime;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Relationships
    private CustomerEntity customer;

    private List<OrderItemEntity> orderItems;

    private List<OrderTableEntity> orderTables;

    private ShipperEntity shipper;
}
