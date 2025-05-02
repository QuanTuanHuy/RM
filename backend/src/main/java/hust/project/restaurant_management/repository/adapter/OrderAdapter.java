package hust.project.restaurant_management.repository.adapter;

import hust.project.restaurant_management.constants.DeliveryStatusEnum;
import hust.project.restaurant_management.constants.ErrorCode;
import hust.project.restaurant_management.entity.OrderEntity;
import hust.project.restaurant_management.entity.dto.request.GetOrderRequest;
import hust.project.restaurant_management.entity.dto.response.PageInfo;
import hust.project.restaurant_management.exception.AppException;
import hust.project.restaurant_management.mapper.IOrderMapper;
import hust.project.restaurant_management.model.OrderModel;
import hust.project.restaurant_management.port.IOrderPort;
import hust.project.restaurant_management.repository.IOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderAdapter implements IOrderPort {
    private final IOrderRepository orderRepository;
    private final IOrderMapper orderMapper;

    @Override
    public OrderEntity save(OrderEntity orderEntity) {
        try {
            OrderModel orderModel = orderMapper.toModelFromEntity(orderEntity);
            return orderMapper.toEntityFromModel(orderRepository.save(orderModel));
        } catch (Exception e) {
            log.error("[OrderAdapter] save: error: {}", e.getMessage());
            throw new AppException(ErrorCode.CREATE_ORDER_FAILED);
        }
    }

    @Override
    public Pair<PageInfo, List<OrderEntity>> getAllOrders(GetOrderRequest filter) {
        var result = orderRepository.getAllOrders(filter);
        return Pair.of(result.getFirst(), orderMapper.toEntitiesFromModels(result.getSecond()));
    }

    @Override
    public List<OrderEntity> getOrdersByIds(List<Long> ids) {
        return orderMapper.toEntitiesFromModels(orderRepository.findByIdIn(ids));
    }

    @Override
    public OrderEntity getOrderById(Long id) {
        return orderMapper.toEntityFromModel(orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND)));
    }

    @Override
    public List<OrderEntity> getOrdersInTimeRangeAndStatus(LocalDateTime startTime, LocalDateTime endTime, String status) {
        return orderMapper.toEntitiesFromModels(orderRepository
                .findByCheckInTimeBetweenAndOrderStatusIs(startTime, endTime, status));
    }

    @Override
    public List<OrderEntity> getOrdersByStatus(String status) {
        return orderMapper.toEntitiesFromModels(orderRepository.findByOrderStatus(status));
    }
    
    @Override
    public List<OrderEntity> findByDeliveryStatus(DeliveryStatusEnum status) {
        try {
            return orderMapper.toEntitiesFromModels(
                    orderRepository.findByIsDeliveryOrderTrueAndDeliveryStatus(status));
        } catch (Exception e) {
            log.error("[OrderAdapter] findByDeliveryStatus: error: {}", e.getMessage());
            throw new AppException(ErrorCode.GET_ORDER_FAILED);
        }
    }
    
    @Override
    public List<OrderEntity> findDeliveryOrdersByShipperId(Long shipperId) {
        try {
            return orderMapper.toEntitiesFromModels(
                    orderRepository.findByIsDeliveryOrderTrueAndShipperId(shipperId));
        } catch (Exception e) {
            log.error("[OrderAdapter] findDeliveryOrdersByShipperId: error: {}", e.getMessage());
            throw new AppException(ErrorCode.GET_ORDER_FAILED);
        }
    }
    
    @Override
    public List<OrderEntity> findPendingDeliveryOrders() {
        try {
            return orderMapper.toEntitiesFromModels(
                    orderRepository.findByIsDeliveryOrderTrueAndDeliveryStatus(DeliveryStatusEnum.PENDING));
        } catch (Exception e) {
            log.error("[OrderAdapter] findPendingDeliveryOrders: error: {}", e.getMessage());
            throw new AppException(ErrorCode.GET_ORDER_FAILED);
        }
    }

    @Override
    public void deleteOrderById(Long id) {
        try {
            orderRepository.deleteById(id);
        } catch (Exception e) {
            log.error("[OrderAdapter] delete order failed error: {}", e.getMessage());
            throw new AppException(ErrorCode.DELETE_ORDER_FAILED);
        }
    }
}
