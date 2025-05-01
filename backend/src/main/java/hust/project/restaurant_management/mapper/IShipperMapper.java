package hust.project.restaurant_management.mapper;

import hust.project.restaurant_management.entity.ShipperEntity;
import hust.project.restaurant_management.model.ShipperModel;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IShipperMapper {
    ShipperEntity toEntity(ShipperModel model);

    ShipperModel toModel(ShipperEntity entity);

    List<ShipperEntity> toListEntity(List<ShipperModel> models);
}
