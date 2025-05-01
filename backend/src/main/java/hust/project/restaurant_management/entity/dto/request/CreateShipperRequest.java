package hust.project.restaurant_management.entity.dto.request;

import hust.project.restaurant_management.constants.ShipperStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateShipperRequest {
    @NotNull(message = "Người dùng không được để trống")
    private Long userId;

    @NotBlank(message = "Loại phương tiện không được để trống")
    @Size(max = 50, message = "Loại phương tiện không được vượt quá 50 ký tự")
    private String vehicleType;

    @NotBlank(message = "Biển số xe không được để trống")
    @Size(max = 20, message = "Biển số xe không được vượt quá 20 ký tự")
    private String vehiclePlateNumber;

    private Double latitude;
    
    private Double longitude;
    
    private String currentAddress;

    @NotNull(message = "Trạng thái không được để trống")
    private ShipperStatusEnum status;

    @NotNull(message = "Số đơn tối đa không được để trống")
    private Integer maxConcurrentOrders;
}