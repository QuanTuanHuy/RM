package hust.project.restaurant_management.service;

import hust.project.restaurant_management.constants.*;
import hust.project.restaurant_management.entity.OrderEntity;
import hust.project.restaurant_management.entity.ShipperEntity;
import hust.project.restaurant_management.exception.AppException;
import hust.project.restaurant_management.port.IOrderPort;
import hust.project.restaurant_management.port.IShipperPort;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Dịch vụ tự động phân công shipper cho đơn hàng
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShipperAssignmentService {
    
    private final IShipperPort shipperPort;
    private final IOrderPort orderPort;
    private final LocationService locationService;
    private final OrderBatchingService orderBatchingService;
    
    // Các tham số cho thuật toán phân công
    private static final double DISTANCE_WEIGHT = 0.5; // Trọng số cho khoảng cách
    private static final double WORKLOAD_WEIGHT = 0.3; // Trọng số cho tải công việc hiện tại
    private static final double RATING_WEIGHT = 0.2;   // Trọng số cho đánh giá shipper
    
    private static final int MAX_SEARCH_RADIUS_KM = 10; // Bán kính tìm kiếm tối đa (km)
    
    /**
     * Phân công shipper tự động cho một đơn hàng dựa trên nhiều tiêu chí
     *
     * @param orderId ID của đơn hàng cần phân công
     * @return Entity của shipper được phân công
     * @throws AppException nếu không tìm thấy shipper phù hợp hoặc đơn hàng không hợp lệ
     */
    @Transactional
    public ShipperEntity assignShipperToOrder(Long orderId) {
        // Lấy thông tin đơn hàng
        OrderEntity order = orderPort.getOrderById(orderId);
        
        if (order == null) {
            log.error("[ShipperAssignmentService] Order not found with ID: {}", orderId);
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        
        // Kiểm tra xem đơn hàng có phải là đơn giao hàng chưa được phân công không
        if (Boolean.FALSE.equals(order.getIsDeliveryOrder())) {
            log.error("[ShipperAssignmentService] Order is not a delivery order: {}", orderId);
            throw new AppException(ErrorCode.SHIPPER_ASSIGNMENT_FAILED);
        }
        
        if (order.getShipperId() != null || order.getDeliveryStatus() != DeliveryStatusEnum.PENDING) {
            log.error("[ShipperAssignmentService] Order already has a shipper assigned: {}", orderId);
            throw new AppException(ErrorCode.SHIPPER_ASSIGNMENT_FAILED);
        }
        
        // Lấy danh sách shipper khả dụng
        List<ShipperEntity> availableShippers = shipperPort.findAvailableShippers();
        
        if (availableShippers.isEmpty()) {
            log.error("[ShipperAssignmentService] No available shippers found");
            throw new AppException(ErrorCode.NO_SHIPPERS_AVAILABLE);
        }
        
        // Lấy tọa độ giao hàng từ đơn hàng
        Double deliveryLatitude = order.getDeliveryLatitude();
        Double deliveryLongitude = order.getDeliveryLongitude();
        
        // Tính toán điểm số cho mỗi shipper và tìm shipper phù hợp nhất
        ShipperEntity bestShipper = findBestShipper(availableShippers, deliveryLatitude, deliveryLongitude);
        
        if (bestShipper == null) {
            log.error("[ShipperAssignmentService] Could not find suitable shipper for order: {}", orderId);
            throw new AppException(ErrorCode.NO_SHIPPERS_AVAILABLE);
        }
        
        // Cập nhật đơn hàng với thông tin shipper
        order.setShipperId(bestShipper.getId());
        order.setDeliveryStatus(DeliveryStatusEnum.ASSIGNED);
        orderPort.save(order);
        
        // Cập nhật thông tin shipper
        bestShipper.setCurrentOrderCount(bestShipper.getCurrentOrderCount() + 1);
        if (bestShipper.getCurrentOrderCount() >= bestShipper.getMaxConcurrentOrders()) {
            bestShipper.setIsAvailable(false);
        }
        bestShipper.setStatus(ShipperStatusEnum.BUSY);
        return shipperPort.save(bestShipper);
    }
    
    /**
     * Phân công một shipper cụ thể cho đơn hàng
     * 
     * @param orderId ID của đơn hàng cần phân công
     * @param shipperId ID của shipper được chỉ định
     */
    @Transactional
    public void assignOrderToSpecificShipper(Long orderId, Long shipperId) {
        OrderEntity order = orderPort.getOrderById(orderId);
        
        if (order == null) {
            log.error("[ShipperAssignmentService] Order not found with ID: {}", orderId);
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        
        if (Boolean.FALSE.equals(order.getIsDeliveryOrder())) {
            log.error("[ShipperAssignmentService] Order is not a delivery order: {}", orderId);
            throw new AppException(ErrorCode.SHIPPER_ASSIGNMENT_FAILED);
        }
        
        if (order.getShipperId() != null || order.getDeliveryStatus() != DeliveryStatusEnum.PENDING) {
            log.error("[ShipperAssignmentService] Order already has a shipper assigned: {}", orderId);
            throw new AppException(ErrorCode.SHIPPER_ASSIGNMENT_FAILED);
        }
        
        Optional<ShipperEntity> shipperOpt = shipperPort.findById(shipperId);
        if (shipperOpt.isEmpty()) {
            log.error("[ShipperAssignmentService] Shipper not found with ID: {}", shipperId);
            throw new AppException(ErrorCode.SHIPPER_NOT_FOUND);
        }
        
        ShipperEntity shipper = shipperOpt.get();
        
        // Cập nhật đơn hàng với thông tin shipper
        order.setShipperId(shipper.getId());
        order.setDeliveryStatus(DeliveryStatusEnum.ASSIGNED);
        
        // Cập nhật thông tin shipper
        shipper.setCurrentOrderCount(shipper.getCurrentOrderCount() + 1);
        if (shipper.getCurrentOrderCount() >= shipper.getMaxConcurrentOrders()) {
            shipper.setIsAvailable(false);
        }
        shipper.setStatus(ShipperStatusEnum.BUSY);
        shipperPort.save(shipper);
        
        orderPort.save(order);
    }
    
//    /**
//     * Phân công nhiều đơn hàng cho một shipper
//     *
//     * @param orderIds Danh sách ID đơn hàng cần phân công
//     * @param shipperId ID của shipper
//     * @return Số lượng đơn hàng được phân công thành công
//     */
//    @Transactional
//    public int assignMultipleOrdersToShipper(List<Long> orderIds, Long shipperId) {
//        if (orderIds == null || orderIds.isEmpty() || shipperId == null) {
//            return 0;
//        }
//
//        Optional<ShipperEntity> shipperOpt = shipperPort.findById(shipperId);
//        if (shipperOpt.isEmpty()) {
//            log.error("[ShipperAssignmentService] Shipper not found with ID: {}", shipperId);
//            throw new AppException(ErrorCode.SHIPPER_NOT_FOUND);
//        }
//
//        ShipperEntity shipper = shipperOpt.get();
//        int assignedCount = 0;
//
//        // Tạo ID nhóm đơn hàng chung
//        String batchId = UUID.randomUUID().toString();
//        int sequence = 0;
//
//        for (Long orderId : orderIds) {
//            try {
//                OrderEntity order = orderPort.getOrderById(orderId);
//
//                if (order != null &&
//                        Boolean.TRUE.equals(order.getIsDeliveryOrder()) &&
//                        order.getDeliveryStatus() == DeliveryStatusEnum.PENDING) {
//
//                    // Cập nhật đơn hàng
//                    order.setShipperId(shipperId);
//                    order.setDeliveryStatus(DeliveryStatusEnum.ASSIGNED);
//
//                    // Cập nhật thông tin batch
//                    order.setBatchId(batchId);
//                    order.setBatchSequence(sequence++);
//
//                    orderPort.save(order);
//                    assignedCount++;
//                }
//            } catch (Exception e) {
//                log.error("[ShipperAssignmentService] Error assigning order {} to shipper {}: {}",
//                        orderId, shipperId, e.getMessage());
//            }
//        }
//
//        if (assignedCount > 0) {
//            // Cập nhật thông tin shipper
//            shipper.setCurrentOrderCount(shipper.getCurrentOrderCount() + assignedCount);
//            if (shipper.getCurrentOrderCount() >= shipper.getMaxConcurrentOrders()) {
//                shipper.setIsAvailable(false);
//            }
//            shipper.setStatus(ShipperStatusEnum.BUSY);
//            shipperPort.save(shipper);
//        }
//
//        return assignedCount;
//    }
    
    /**
     * Tìm shipper tốt nhất cho một đơn hàng dựa trên các tiêu chí
     * 
     * @param shippers Danh sách shipper sẵn có
     * @param deliveryLatitude Vĩ độ địa điểm giao hàng
     * @param deliveryLongitude Kinh độ địa điểm giao hàng
     * @return Shipper phù hợp nhất hoặc null nếu không tìm thấy
     */
    private ShipperEntity findBestShipper(List<ShipperEntity> shippers, Double deliveryLatitude, Double deliveryLongitude) {
        if (deliveryLatitude == null || deliveryLongitude == null) {
            // Nếu không có tọa độ giao hàng, chỉ xét tải công việc và đánh giá
            return shippers.stream()
                    .filter(shipper -> shipper.getCurrentOrderCount() < shipper.getMaxConcurrentOrders())
                    .max(Comparator.comparing(ShipperEntity::getRating))
                    .orElse(null);
        }
        
        // Tính điểm cho mỗi shipper và sắp xếp theo điểm giảm dần
        List<RankedShipper> rankedShippers = shippers.stream()
                .filter(shipper -> {
                    // Lọc các shipper còn sức chứa đơn hàng
                    return shipper.getCurrentOrderCount() < shipper.getMaxConcurrentOrders() && 
                           shipper.getLatitude() != null && shipper.getLongitude() != null;
                })
                .map(shipper -> {
                    double distanceToRestaurant = locationService.calculateDistance(
                            shipper.getLatitude(), shipper.getLongitude(),
                            GeoConstant.RESTAURANT_LATITUDE, GeoConstant.RESTAURANT_LONGITUDE);
                    
                    // Chỉ xét các shipper trong bán kính tìm kiếm
                    if (distanceToRestaurant > MAX_SEARCH_RADIUS_KM) {
                        return null;
                    }
                    
                    double distanceToDelivery = locationService.calculateDistance(
                            shipper.getLatitude(), shipper.getLongitude(),
                            deliveryLatitude, deliveryLongitude);
                    
                    // Chuẩn hóa các thông số để tính điểm
                    double normalizedDistance = 1.0 - (distanceToRestaurant / MAX_SEARCH_RADIUS_KM); // 1 là gần nhất, 0 là xa nhất
                    double normalizedWorkload = 1.0 - ((double) shipper.getCurrentOrderCount() / shipper.getMaxConcurrentOrders()); // 1 là rảnh nhất, 0 là bận nhất
                    double normalizedRating = shipper.getRating() / 5.0; // Chuẩn hóa đánh giá từ 0-5 thành 0-1
                    
                    // Tính điểm tổng hợp
                    double score = (DISTANCE_WEIGHT * normalizedDistance) + 
                                 (WORKLOAD_WEIGHT * normalizedWorkload) + 
                                 (RATING_WEIGHT * normalizedRating);
                    
                    return new RankedShipper(shipper, score, distanceToRestaurant, distanceToDelivery);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(RankedShipper::getScore).reversed())
                .toList();
        
        // Trả về shipper có điểm cao nhất hoặc null nếu không tìm được
        return rankedShippers.isEmpty() ? null : rankedShippers.get(0).getShipper();
    }
    
    /**
     * Lớp nội bộ để lưu thông tin shipper đã được xếp hạng
     */
    @Getter
    private static class RankedShipper {
        private final ShipperEntity shipper;
        private final double score;
        private final double distanceToRestaurant;
        private final double distanceToDelivery;
        
        RankedShipper(ShipperEntity shipper, double score, double distanceToRestaurant, double distanceToDelivery) {
            this.shipper = shipper;
            this.score = score;
            this.distanceToRestaurant = distanceToRestaurant;
            this.distanceToDelivery = distanceToDelivery;
        }

    }
    
    /**
     * Cập nhật trạng thái đơn hàng giao hàng
     *
     * @param orderId ID của đơn hàng
     * @param status Trạng thái giao hàng mới
     * @return Đơn hàng đã được cập nhật
     */
    @Transactional
    public OrderEntity updateDeliveryStatus(Long orderId, DeliveryStatusEnum status) {
        OrderEntity order = orderPort.getOrderById(orderId);
        
        if (order == null) {
            log.error("[ShipperAssignmentService] Order not found with ID: {}", orderId);
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        
        if (Boolean.FALSE.equals(order.getIsDeliveryOrder())) {
            log.error("[ShipperAssignmentService] Order is not a delivery order: {}", orderId);
            throw new AppException(ErrorCode.UPDATE_DELIVERY_STATUS_FAILED);
        }
        
        if (order.getShipperId() == null && status != DeliveryStatusEnum.PENDING) {
            log.error("[ShipperAssignmentService] Cannot update status of unassigned order: {}", orderId);
            throw new AppException(ErrorCode.UPDATE_DELIVERY_STATUS_FAILED);
        }
        
        // Cập nhật trạng thái giao hàng
        order.setDeliveryStatus(status);
        
        // Cập nhật trạng thái đơn hàng tương ứng
        if (status == DeliveryStatusEnum.DELIVERED) {
            order.setOrderStatus(OrderStatusEnum.COMPLETED.name());
            
            // Cập nhật thông tin shipper nếu giao hàng thành công
            Optional<ShipperEntity> shipperOpt = shipperPort.findById(order.getShipperId());
            if (shipperOpt.isPresent()) {
                ShipperEntity shipper = shipperOpt.get();
                shipper.setCurrentOrderCount(shipper.getCurrentOrderCount() - 1);
                shipper.setCompletedDeliveries(shipper.getCompletedDeliveries() + 1);
                
                // Đặt shipper thành sẵn sàng nếu họ chưa đạt tối đa số đơn
                if (shipper.getCurrentOrderCount() < shipper.getMaxConcurrentOrders()) {
                    shipper.setIsAvailable(true);
                    shipper.setStatus(ShipperStatusEnum.AVAILABLE);
                }
                
                shipperPort.save(shipper);
            }
        } else if (status == DeliveryStatusEnum.FAILED || status == DeliveryStatusEnum.CANCELLED) {
            // Cập nhật trạng thái shipper nếu giao hàng thất bại hoặc bị hủy
            Optional<ShipperEntity> shipperOpt = shipperPort.findById(order.getShipperId());
            if (shipperOpt.isPresent()) {
                ShipperEntity shipper = shipperOpt.get();
                shipper.setCurrentOrderCount(shipper.getCurrentOrderCount() - 1);
                
                // Đặt shipper thành sẵn sàng
                shipper.setIsAvailable(true);
                shipper.setStatus(ShipperStatusEnum.AVAILABLE);
                
                shipperPort.save(shipper);
            }
            
            if (status == DeliveryStatusEnum.CANCELLED) {
                order.setOrderStatus(OrderStatusEnum.CANCELLED.name());
            }
        }
        
        return orderPort.save(order);
    }
    
    /**
     * Phân công shipper tự động cho nhiều đơn hàng đang chờ sử dụng batching
     */
    @Transactional
    public void assignShippersToQueuedOrders() {
        // Lấy danh sách các đơn hàng đang chờ phân công shipper
        List<OrderEntity> pendingOrders = orderPort.findByDeliveryStatus(DeliveryStatusEnum.PENDING);
        
        if (pendingOrders.isEmpty()) {
            log.info("[ShipperAssignmentService] No pending delivery orders to assign");
            return;
        }
        
        log.info("[ShipperAssignmentService] Found {} pending delivery orders", pendingOrders.size());
        
        // Lấy danh sách shipper sẵn có
        List<ShipperEntity> availableShippers = shipperPort.findAvailableShippers();
        
        if (availableShippers.isEmpty()) {
            log.warn("[ShipperAssignmentService] No available shippers found");
            return;
        }
        
        // Tạo các batch đơn hàng tối ưu
        List<OrderBatchingService.OrderBatch> batches = orderBatchingService.createOptimalBatches(pendingOrders);
        
        if (batches.isEmpty()) {
            log.info("[ShipperAssignmentService] No batches could be created, falling back to individual assignment");
            // Sắp xếp các đơn hàng theo độ ưu tiên (đơn hàng cũ nhất được ưu tiên trước)
            pendingOrders.sort(Comparator.comparing(OrderEntity::getCreatedAt));
            
            for (OrderEntity order : pendingOrders) {
                try {
                    assignShipperToOrder(order.getId());
                    log.info("[ShipperAssignmentService] Successfully assigned shipper to order: {}", order.getId());
                } catch (AppException e) {
                    if (e.getErrorCode() == ErrorCode.NO_SHIPPERS_AVAILABLE) {
                        log.warn("[ShipperAssignmentService] No available shippers to assign to order: {}", order.getId());
                        break; // Nếu hết shipper thì dừng lại
                    } else {
                        log.error("[ShipperAssignmentService] Error assigning shipper to order {}: {}", order.getId(), e.getMessage());
                    }
                }
            }
        } else {
            // Phân công các batch cho shipper
            log.info("[ShipperAssignmentService] Created {} batches for assignment", batches.size());
            orderBatchingService.assignBatchesToShippers(batches, availableShippers, this);
        }
    }
}