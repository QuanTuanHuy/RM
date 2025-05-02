package hust.project.restaurant_management.mapper;

import hust.project.restaurant_management.entity.OrderEntity;
import hust.project.restaurant_management.entity.dto.request.CreateOrderRequest;
import hust.project.restaurant_management.model.OrderModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IOrderMapper {
    OrderModel toModelFromEntity(OrderEntity entity);

    OrderEntity toEntityFromModel(OrderModel model);

    @Mapping(target = "orderItems", ignore = true)
    OrderEntity toEntityFromRequest(CreateOrderRequest request);

    List<OrderEntity> toEntitiesFromModels(List<OrderModel> models);
}
