package hust.project.restaurant_management.repository;

import hust.project.restaurant_management.model.ShipperModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IShipperRepository extends IBaseRepository<ShipperModel> {
    Optional<ShipperModel> findByUserId(Long userId);

    @Query("SELECT s FROM ShipperModel s WHERE s.isAvailable = true " +
            "AND s.status = 'AVAILABLE' " +
            "AND s.currentOrderCount < s.maxConcurrentOrders")
    List<ShipperModel> findAllAvailableForNewOrders();
}