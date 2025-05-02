package hust.project.restaurant_management.entity.dto.request;

import lombok.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {
    @NotNull(message = "ID món ăn không được để trống")
    private Long menuItemId;
    
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;
    
    private String note;
}