package hust.project.restaurant_management.port;

import hust.project.restaurant_management.constants.ShipperStatusEnum;
import hust.project.restaurant_management.entity.ShipperEntity;

import java.util.List;
import java.util.Optional;

public interface IShipperPort {
    ShipperEntity save(ShipperEntity shipperEntity);

    Optional<ShipperEntity> findById(Long id);

    List<ShipperEntity> findAll();

    Optional<ShipperEntity> findByUserId(Long userId);

    ShipperEntity updateLocation(Long id, Double latitude, Double longitude, String address);

    ShipperEntity updateStatus(Long id, ShipperStatusEnum status);

    List<ShipperEntity> findAvailableShippers();

//    List<ShipperEntity> findNearbyAvailableShippers(Double latitude, Double longitude, Double distanceKm);

    Integer countCurrentOrders(Long shipperId);

    void deleteById(Long id);
}