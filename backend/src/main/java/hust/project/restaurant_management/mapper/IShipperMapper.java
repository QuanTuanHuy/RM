package hust.project.restaurant_management.mapper;

import hust.project.restaurant_management.entity.ShipperEntity;
import hust.project.restaurant_management.entity.dto.request.CreateShipperRequest;
import hust.project.restaurant_management.model.ShipperModel;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IShipperMapper {
    ShipperModel toModelFromEntity(ShipperEntity entity);

    ShipperEntity toEntityFromModel(ShipperModel model);
    
    ShipperEntity toEntityFromCreateRequest(CreateShipperRequest request);

    List<ShipperEntity> toEntitiesFromModels(List<ShipperModel> models);
    
    List<ShipperModel> toModelsFromEntities(List<ShipperEntity> entities);
}
