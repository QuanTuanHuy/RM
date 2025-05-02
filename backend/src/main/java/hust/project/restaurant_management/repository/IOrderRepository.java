package hust.project.restaurant_management.repository;

import hust.project.restaurant_management.constants.DeliveryStatusEnum;
import hust.project.restaurant_management.model.OrderModel;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IOrderRepository extends IBaseRepository<OrderModel>, CustomOrderRepository {
    List<OrderModel> findByIdIn(List<Long> ids);

    List<OrderModel> findByCheckInTimeBetweenAndOrderStatusIs(LocalDateTime startTime,LocalDateTime endTime,
                                                              String status);

    List<OrderModel> findByOrderStatus(String status);

    List<OrderModel> findByIsDeliveryOrderTrueAndDeliveryStatus(DeliveryStatusEnum status);

    List<OrderModel> findByIsDeliveryOrderTrueAndShipperId(Long shipperId);

    List<OrderModel> findByIsDeliveryOrderTrueAndShipperIdAndDeliveryStatus(Long shipperId, DeliveryStatusEnum status);
}
