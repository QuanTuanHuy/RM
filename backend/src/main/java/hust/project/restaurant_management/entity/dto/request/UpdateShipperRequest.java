package hust.project.restaurant_management.entity.dto.request;

import hust.project.restaurant_management.constants.ShipperStatusEnum;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateShipperRequest {
    @Size(max = 50, message = "Loại phương tiện không được vượt quá 50 ký tự")
    private String vehicleType;

    @Size(max = 20, message = "Biển số xe không được vượt quá 20 ký tự")
    private String vehiclePlateNumber;
    
    private Boolean isAvailable;
    
    private Double latitude;
    
    private Double longitude;
    
    private String currentAddress;
    
    private ShipperStatusEnum status;
    
    private Integer maxConcurrentOrders;
}