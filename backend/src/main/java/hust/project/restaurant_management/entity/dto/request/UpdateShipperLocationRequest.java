package hust.project.restaurant_management.entity.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateShipperLocationRequest {
    @NotNull(message = "Vĩ độ không được để trống")
    private Double latitude;
    
    @NotNull(message = "Kinh độ không được để trống")
    private Double longitude;
    
    private String currentAddress;
}