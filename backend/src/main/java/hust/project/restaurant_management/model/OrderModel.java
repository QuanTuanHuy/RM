package hust.project.restaurant_management.model;

import hust.project.restaurant_management.constants.DeliveryStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "order_status")
    private String orderStatus;

    @Column(name = "total_cost")
    private Double totalCost;

    @Column(name = "number_of_people")
    private Long numberOfPeople;

    @Column(name = "note")
    private String note;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Các trường liên quan đến giao hàng
    @Column(name = "is_delivery_order")
    private Boolean isDeliveryOrder;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Column(name = "delivery_instructions")
    private String deliveryInstructions;

    @Column(name = "delivery_fee")
    private Double deliveryFee;

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_phone")
    private String recipientPhone;

    @Column(name = "delivery_status")
    @Enumerated(EnumType.STRING)
    private DeliveryStatusEnum deliveryStatus;

    @Column(name = "shipper_id")
    private Long shipperId;

    @Column(name = "delivery_latitude")
    private Double deliveryLatitude;

    @Column(name = "delivery_longitude")
    private Double deliveryLongitude;

    // Thông tin tọa độ nhà hàng cho việc tính toán nhóm đơn hàng
    @Column(name = "restaurant_latitude")
    private Double restaurantLatitude;

    @Column(name = "restaurant_longitude")
    private Double restaurantLongitude;

    // Thông tin batch cho việc nhóm đơn hàng
    @Column(name = "batch_id")
    private String batchId;

    @Column(name = "batch_sequence")
    private Integer batchSequence;

    // Estimated pickup time for restaurant preparation
    @Column(name = "estimated_pickup_time")
    private LocalDateTime estimatedPickupTime;
}
