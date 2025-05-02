package hust.project.restaurant_management.usecase;

import hust.project.restaurant_management.constants.DeliveryStatusEnum;
import hust.project.restaurant_management.constants.ErrorCode;
import hust.project.restaurant_management.constants.GeoConstant;
import hust.project.restaurant_management.constants.OrderStatusEnum;
import hust.project.restaurant_management.entity.OrderEntity;
import hust.project.restaurant_management.entity.OrderTableEntity;
import hust.project.restaurant_management.entity.TableEntity;
import hust.project.restaurant_management.entity.dto.request.AddMenuItemsToOrderRequest;
import hust.project.restaurant_management.entity.dto.request.CreateOrderRequest;
import hust.project.restaurant_management.entity.dto.request.GetTableAvailableRequest;
import hust.project.restaurant_management.entity.dto.request.MenuItemQuantityRequest;
import hust.project.restaurant_management.exception.AppException;
import hust.project.restaurant_management.mapper.IOrderMapper;
import hust.project.restaurant_management.port.*;
import hust.project.restaurant_management.service.DeliveryFeeService;
import hust.project.restaurant_management.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateOrderUseCase {
    private final IOrderPort orderPort;
    private final ICustomerPort customerPort;
    private final IOrderTablePort orderTablePort;
    private final ITablePort tablePort;
    private final IOrderMapper orderMapper;

    private final UpdateOrderUseCase updateOrderUseCase;
    private final DeliveryFeeService deliveryFeeService;
    private final LocationService locationService;
    
    // Thời gian chuẩn bị đơn hàng mặc định (phút)
    private static final int DEFAULT_PREPARATION_TIME = 15;

    @Transactional
    public OrderEntity createOrder(CreateOrderRequest request) {
        // Xác thực đầu vào
        customerPort.getCustomerById(request.getCustomerId());
        OrderEntity order = orderMapper.toEntityFromRequest(request);
        
        // Xử lý đơn hàng giao hàng
        if (Boolean.TRUE.equals(request.getIsDeliveryOrder())) {
            return processDeliveryOrder(request, order);
        } 
        // Xử lý đơn hàng ăn tại chỗ
        else {
            return processInStoreOrder(request, order);
        }
    }
    
    /**
     * Xử lý đơn hàng giao hàng
     */
    private OrderEntity processDeliveryOrder(CreateOrderRequest request, OrderEntity order) {
        // Kiểm tra thông tin giao hàng
        if (request.getDeliveryAddress() == null || request.getDeliveryAddress().isBlank()) {
            log.error("[CreateOrderUseCase] delivery order without address");
            throw new AppException(ErrorCode.CREATE_ORDER_FAILED);
        }
        
        if (request.getRecipientName() == null || request.getRecipientName().isBlank() ||
            request.getRecipientPhone() == null || request.getRecipientPhone().isBlank()) {
            log.error("[CreateOrderUseCase] delivery order without recipient info");
            throw new AppException(ErrorCode.CREATE_ORDER_FAILED);
        }
        
        // Tính phí giao hàng
        double deliveryFee;
        double estimatedOrderAmount = 10000.0; // Tạm thời để giá trị mặc định, sẽ cập nhật sau khi tính toán đơn hàng
        
        // Nếu có tọa độ
        if (request.getDeliveryLatitude() != null && request.getDeliveryLongitude() != null) {
            try {
                deliveryFee = deliveryFeeService.calculateDeliveryFee(
                        GeoConstant.RESTAURANT_LATITUDE, GeoConstant.RESTAURANT_LONGITUDE,
                        request.getDeliveryLatitude(), request.getDeliveryLongitude(),
                        estimatedOrderAmount);
                
                // Tính thời gian giao hàng
                double distance = locationService.calculateDistance(
                        GeoConstant.RESTAURANT_LATITUDE, GeoConstant.RESTAURANT_LONGITUDE,
                        request.getDeliveryLatitude(), request.getDeliveryLongitude());
                        
                int estimatedDeliveryMinutes = deliveryFeeService.estimateDeliveryTime(
                        distance, DEFAULT_PREPARATION_TIME);
                        
                LocalDateTime estimatedDeliveryTime = LocalDateTime.now().plusMinutes(estimatedDeliveryMinutes);
                order.setEstimatedDeliveryTime(estimatedDeliveryTime);
                
            } catch (IllegalArgumentException e) {
                log.error("[CreateOrderUseCase] delivery distance exceeds limit");
                throw new AppException(ErrorCode.CREATE_ORDER_FAILED);
            }
        } else {
            // Nếu không có tọa độ, sử dụng một khoảng cách mặc định
            deliveryFee = deliveryFeeService.calculateDeliveryFeeByDistance(5.0, estimatedOrderAmount);
            
            // Ước tính thời gian giao hàng
            int estimatedDeliveryMinutes = deliveryFeeService.estimateDeliveryTime(5.0, DEFAULT_PREPARATION_TIME);
            LocalDateTime estimatedDeliveryTime = LocalDateTime.now().plusMinutes(estimatedDeliveryMinutes);
            order.setEstimatedDeliveryTime(estimatedDeliveryTime);
        }
        
        // Cập nhật thông tin giao hàng
        order.setDeliveryFee(deliveryFee);
        order.setIsDeliveryOrder(true);
        order.setDeliveryStatus(DeliveryStatusEnum.PENDING);
        order.setOrderStatus(OrderStatusEnum.CONFIRMED.name());
        order.setRestaurantLatitude(GeoConstant.RESTAURANT_LATITUDE);
        order.setRestaurantLongitude(GeoConstant.RESTAURANT_LONGITUDE);

        OrderEntity savedOrder = orderPort.save(order);

        // Thông báo cho nhà bếp
        if (!CollectionUtils.isEmpty(request.getOrderItems())) {
            List<MenuItemQuantityRequest> menuItemsQuantity = request.getOrderItems().stream()
                    .map(oi -> MenuItemQuantityRequest.builder()
                            .quantity((long) oi.getQuantity())
                            .menuItemId(oi.getMenuItemId())
                            .note(oi.getNote())
                            .build())
                    .toList();
            updateOrderUseCase.addMenuItemsToOrder(savedOrder.getId(), AddMenuItemsToOrderRequest.builder()
                            .menuItemsQuantity(menuItemsQuantity)
                    .build());
        }
        
        // Lưu đơn hàng
        return  savedOrder;
    }
    
    /**
     * Xử lý đơn hàng ăn tại chỗ
     */
    private OrderEntity processInStoreOrder(CreateOrderRequest request, OrderEntity order) {
        if (request.getCheckInTime() == null || request.getCheckOutTime() == null ||
            request.getCheckInTime().isAfter(request.getCheckOutTime()) ||
            request.getCheckInTime().isBefore(LocalDateTime.now())) {
            log.error("[CreateOrderUseCase] invalid time range for order");
            throw new AppException(ErrorCode.CREATE_ORDER_FAILED);
        }
        
        if (CollectionUtils.isEmpty(request.getTableIds())) {
            log.error("[CreateOrderUseCase] in-store order without tables");
            throw new AppException(ErrorCode.CREATE_ORDER_FAILED);
        }

        HashSet<Long> availableTableIds = (HashSet<Long>)
                tablePort.getAllTablesAvailable(new GetTableAvailableRequest(
                        request.getCheckInTime(), request.getCheckOutTime()))
                .stream()
                .map(TableEntity::getId)
                .collect(Collectors.toSet());

        if (!availableTableIds.containsAll(request.getTableIds())) {
            log.error("[CreateOrderUseCase] createOrder: Not all tables are available");
            throw new AppException(ErrorCode.CREATE_ORDER_FAILED);
        }

        order.setOrderStatus(OrderStatusEnum.CONFIRMED.name());
        order.setIsDeliveryOrder(false);
        order = orderPort.save(order);
        final Long orderId = order.getId();

        List<OrderTableEntity> orderTableEntities = request.getTableIds().stream()
                .map(tableId -> OrderTableEntity.builder()
                        .orderId(orderId)
                        .tableId(tableId)
                        .build())
                .toList();

        List<OrderTableEntity> savedOrderTable = orderTablePort.saveAll(orderTableEntities);
        order.setOrderTables(savedOrderTable);

        return order;
    }
}
