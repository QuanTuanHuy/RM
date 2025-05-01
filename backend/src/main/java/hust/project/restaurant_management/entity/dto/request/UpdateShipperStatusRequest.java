package hust.project.restaurant_management.entity.dto.request;

import hust.project.restaurant_management.constants.ShipperStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateShipperStatusRequest {
    @NotNull(message = "Trạng thái không được để trống")
    private ShipperStatusEnum status;
}