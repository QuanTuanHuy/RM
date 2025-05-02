package hust.project.restaurant_management.entity.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    private Long customerId;
    private Long userId;
    
    private List<OrderItemRequest> orderItems;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkInTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkOutTime;
    
    private Long numberOfPeople;
    
    private List<Long> tableIds;
    
    private String note;
    
    // Delivery related fields
    private Boolean isDeliveryOrder;
    
    private String deliveryAddress;
    
    private String deliveryInstructions;
    
    private String recipientName;
    
    private String recipientPhone;
    
    // Coordinates for delivery location (optional, can be geocoded from address)
    private Double deliveryLatitude;
    
    private Double deliveryLongitude;
}
