package hust.project.restaurant_management.port;

import hust.project.restaurant_management.constants.ShipperStatusEnum;
import hust.project.restaurant_management.entity.ShipperEntity;

import java.util.List;

public interface IShipperPort {
    ShipperEntity save(ShipperEntity shipperEntity);
    
    List<ShipperEntity> findAll();
    
    ShipperEntity findById(Long id);
    
    ShipperEntity findByUserId(Long userId);
    
    List<ShipperEntity> findByStatus(ShipperStatusEnum status);
    
    List<ShipperEntity> findAvailableShippers();
    
    List<ShipperEntity> findShippersWithCapacity();
    
    void deleteById(Long id);
}