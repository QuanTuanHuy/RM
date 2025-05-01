package hust.project.restaurant_management.repository;

import hust.project.restaurant_management.constants.ShipperStatusEnum;
import hust.project.restaurant_management.model.ShipperModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IShipperRepository extends IBaseRepository<ShipperModel> {
    Optional<ShipperModel> findByUserId(Long userId);
    
    List<ShipperModel> findByStatus(ShipperStatusEnum status);
    
    @Query("SELECT s FROM ShipperModel s WHERE s.status = 'AVAILABLE' AND s.isAvailable = true")
    List<ShipperModel> findAvailableShippers();
    
    @Query("SELECT s FROM ShipperModel s WHERE s.currentOrderCount < s.maxConcurrentOrders AND s.status = 'AVAILABLE'")
    List<ShipperModel> findShippersWithCapacity();
}