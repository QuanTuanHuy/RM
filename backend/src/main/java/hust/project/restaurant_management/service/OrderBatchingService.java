package hust.project.restaurant_management.service;

import hust.project.restaurant_management.algo.TSPSolver;
import hust.project.restaurant_management.constants.DeliveryStatusEnum;
import hust.project.restaurant_management.constants.GeoConstant;
import hust.project.restaurant_management.entity.OrderEntity;
import hust.project.restaurant_management.entity.ShipperEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service để nhóm các đơn hàng thông minh để tối ưu hóa quá trình giao hàng
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderBatchingService {
    private final LocationService locationService;
    
    /**
     * Tạo các batch đơn hàng tối ưu từ danh sách đơn hàng chờ
     * 
     * @param pendingOrders Danh sách đơn hàng đang chờ giao
     * @return Danh sách các batch đơn hàng
     */
    public List<OrderBatch> createOptimalBatches(List<OrderEntity> pendingOrders) {
        if (pendingOrders == null || pendingOrders.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<OrderEntity> validOrders = pendingOrders.stream()
                .filter(this::isValidForBatching)
                .collect(Collectors.toList());
        
        if (validOrders.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Bước 1: Nhóm các đơn hàng từ cùng nhà hàng hoặc nhà hàng gần nhau
        List<RestaurantCluster> restaurantClusters = clusterOrdersByRestaurant(validOrders);
        
        // Bước 2: Đối với mỗi cụm nhà hàng, nhóm các điểm giao hàng gần nhau
        List<OrderBatch> allBatches = new ArrayList<>();
        for (RestaurantCluster restaurantCluster : restaurantClusters) {
            allBatches.addAll(createBatchesFromRestaurantCluster(restaurantCluster));
        }
        
        // Bước 3: Sắp xếp các batch theo thứ tự ưu tiên (thời gian tạo đơn cũ nhất)
        allBatches.sort(Comparator.comparing(OrderBatch::getOldestOrderTime));
        
        return allBatches;
    }
    
    /**
     * Nhóm các đơn hàng từ cùng nhà hàng hoặc nhà hàng gần nhau
     */
    private List<RestaurantCluster> clusterOrdersByRestaurant(List<OrderEntity> orders) {
        List<RestaurantCluster> clusters = new ArrayList<>();
        Set<OrderEntity> assignedOrders = new HashSet<>();
        
        for (OrderEntity order : orders) {
            if (assignedOrders.contains(order)) {
                continue;
            }
            
            // Chỉ xét các đơn hàng chưa được gán vào cụm nào
            List<OrderEntity> clusterOrders = new ArrayList<>();
            clusterOrders.add(order);
            assignedOrders.add(order);
            
            // Tọa độ nhà hàng (với đơn giao hàng, nhà hàng luôn là điểm xuất phát)
            double restaurantLat = order.getRestaurantLatitude();
            double restaurantLng = order.getRestaurantLongitude();
            
            // Tìm các đơn khác từ nhà hàng này hoặc nhà hàng gần đó
            for (OrderEntity otherOrder : orders) {
                if (assignedOrders.contains(otherOrder)) {
                    continue;
                }

                // Nếu nhà hàng cách nhau trong bán kính cho phép, gộp vào cùng cụm
                boolean isSameCluster = locationService.isPointWithinRadius(
                        restaurantLat, restaurantLng,
                        otherOrder.getRestaurantLatitude(), otherOrder.getRestaurantLongitude(),
                        GeoConstant.RESTAURANT_CLUSTER_RADIUS_KM);
                if (isSameCluster) {
                    clusterOrders.add(otherOrder);
                    assignedOrders.add(otherOrder);

                    // Giới hạn số lượng nhà hàng trong một cụm
                    if (clusterOrders.size() >= GeoConstant.MAX_BATCH_SIZE) {
                        break;
                    }
                }
            }
            
            clusters.add(new RestaurantCluster(restaurantLat, restaurantLng, clusterOrders));
        }
        
        return clusters;
    }
    
    /**
     * Tạo các batch đơn hàng từ một cụm nhà hàng
     */
    private List<OrderBatch> createBatchesFromRestaurantCluster(RestaurantCluster cluster) {
        List<OrderBatch> batches = new ArrayList<>();
        List<OrderEntity> orders = new ArrayList<>(cluster.getOrders());
        
        // Sắp xếp đơn hàng theo thời gian tạo
        orders.sort(Comparator.comparing(OrderEntity::getCreatedAt));
        
        while (!orders.isEmpty()) {
            OrderBatch batch = new OrderBatch();
            OrderEntity firstOrder = orders.remove(0);
            batch.addOrder(firstOrder);
            
            // Tìm các đơn hàng giao cùng khu vực với đơn đầu tiên
            List<OrderEntity> candidateOrders = new ArrayList<>(orders);
            
            for (OrderEntity candidate : candidateOrders) {
                if (batch.getOrders().size() >= GeoConstant.MAX_BATCH_SIZE) {
                    break;
                }
                
                // Kiểm tra xem có thể thêm đơn hàng này vào batch không
                if (canAddOrderToBatch(batch, candidate)) {
                    batch.addOrder(candidate);
                    orders.remove(candidate);
                }
            }
            
            // Tối ưu thứ tự giao hàng trong batch
            optimizeDeliverySequence(batch);
            
            batches.add(batch);
        }
        
        return batches;
    }
    
    /**
     * Kiểm tra xem có thể thêm đơn hàng vào batch không
     */
    private boolean canAddOrderToBatch(OrderBatch batch, OrderEntity candidateOrder) {
        // Kiểm tra khoảng cách từ điểm giao của đơn cuối cùng hiện tại đến điểm giao của đơn mới
        OrderEntity lastOrder = batch.getOrders().get(batch.getOrders().size() - 1);

        boolean isSameBatch = locationService.isPointWithinRadius(
                lastOrder.getDeliveryLatitude(), lastOrder.getDeliveryLongitude(),
                candidateOrder.getDeliveryLatitude(), candidateOrder.getDeliveryLongitude(),
                GeoConstant.DELIVERY_CLUSTER_RADIUS_KM
        );

        if (!isSameBatch) {
            return false;
        }
        
        // Tính tổng khoảng cách của batch nếu thêm đơn hàng này
        double totalDistance = calculateTotalBatchDistance(batch, candidateOrder);
        
        // Nếu tổng khoảng cách vượt quá MAX_TOTAL_DISTANCE_KM thì không thêm vào batch
        return totalDistance <= GeoConstant.MAX_TOTAL_DISTANCE_KM;
    }
    
    /**
     * Tính tổng khoảng cách của một batch nếu thêm đơn hàng mới
     */
    private double calculateTotalBatchDistance(OrderBatch batch, OrderEntity newOrder) {
        List<OrderEntity> allOrders = new ArrayList<>(batch.getOrders());
        allOrders.add(newOrder);
        
        double totalDistance = 0;
        
        // Điểm bắt đầu là nhà hàng của đơn đầu tiên
        OrderEntity firstOrder = allOrders.get(0);
        double currentLat = firstOrder.getRestaurantLatitude();
        double currentLng = firstOrder.getRestaurantLongitude();
        
        for (OrderEntity order : allOrders) {
            // Tính khoảng cách từ điểm hiện tại đến điểm giao hàng
            double distance = locationService.calculateDistance(
                    currentLat, currentLng,
                    order.getDeliveryLatitude(), order.getDeliveryLongitude()
            );
            
            totalDistance += distance;
            
            // Cập nhật vị trí hiện tại
            currentLat = order.getDeliveryLatitude();
            currentLng = order.getDeliveryLongitude();
        }
        
        return totalDistance;
    }
    
    /**
     * Tối ưu hóa thứ tự giao hàng trong một batch sử dụng thuật toán TSP
     * Thuật toán này sử dụng phương pháp dynamic programming để giải quyết bài toán người đi du lịch (TSP)
     */
    private void optimizeDeliverySequence(OrderBatch batch) {
        List<OrderEntity> orders = batch.getOrders();
        
        if (orders.size() <= 1) {
            return; // Không cần tối ưu nếu chỉ có 1 đơn
        }
        
        // Thêm nhà hàng làm điểm xuất phát (index 0)
        List<Point> points = new ArrayList<>();
        double restaurantLat = orders.get(0).getRestaurantLatitude();
        double restaurantLng = orders.get(0).getRestaurantLongitude();
        points.add(new Point(restaurantLat, restaurantLng, -1)); // -1 đánh dấu đây là nhà hàng
        
        // Thêm các điểm giao hàng
        for (int i = 0; i < orders.size(); i++) {
            OrderEntity order = orders.get(i);
            points.add(new Point(
                    order.getDeliveryLatitude(),
                    order.getDeliveryLongitude(),
                    i // index của đơn hàng trong danh sách orders
            ));
        }
        
        int n = points.size() + 1;
        
        // Tính ma trận khoảng cách giữa tất cả các điểm
        double[][] distanceMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    distanceMatrix[i][j] = 0;
                } else {
                    distanceMatrix[i][j] = locationService.calculateDistance(
                            points.get(i).getLat(), points.get(i).getLng(),
                            points.get(j).getLat(), points.get(j).getLng()
                    );
                }
            }
        }
        
        // Giải quyết bài toán TSP sử dụng Held-Karp algorithm (Dynamic Programming)
        TSPSolver tspSolver = new TSPSolver(distanceMatrix);
        List<Integer> optimalRoute = tspSolver.solve();
        
        // Tạo danh sách đơn hàng mới theo thứ tự tối ưu (bỏ qua điểm xuất phát là nhà hàng)
        List<OrderEntity> optimizedSequence = new ArrayList<>();
        for (int i = 1; i < optimalRoute.size(); i++) { // bắt đầu từ 1 để bỏ qua nhà hàng
            int pointIndex = optimalRoute.get(i);
            int orderIndex = points.get(pointIndex).getOrderIndex();
            if (orderIndex >= 0) { // Chỉ thêm các điểm là đơn hàng (không phải nhà hàng)
                optimizedSequence.add(orders.get(orderIndex));
            }
        }

        batch.setOrders(optimizedSequence);
    }
    
    /**
     * Lớp hỗ trợ lưu trữ thông tin về một điểm (nhà hàng hoặc điểm giao hàng)
     */
    @Getter
    private static class Point {
        private final double lat;
        private final double lng;
        private final int orderIndex; // Index của đơn hàng trong danh sách gốc, -1 nếu là nhà hàng
        
        public Point(double lat, double lng, int orderIndex) {
            this.lat = lat;
            this.lng = lng;
            this.orderIndex = orderIndex;
        }

    }
    
    /**
     * Kiểm tra xem đơn hàng có hợp lệ để nhóm không
     */
    private boolean isValidForBatching(OrderEntity order) {
        // Chỉ xem xét các đơn giao hàng có đầy đủ thông tin tọa độ
        return Boolean.TRUE.equals(order.getIsDeliveryOrder()) 
                && order.getDeliveryStatus() == DeliveryStatusEnum.PENDING
                && order.getDeliveryLatitude() != null 
                && order.getDeliveryLongitude() != null
                && order.getRestaurantLatitude() != null
                && order.getRestaurantLongitude() != null;
    }
    
    /**
     * Phân công các batch đơn hàng cho các shipper
     */
    public void assignBatchesToShippers(List<OrderBatch> batches, List<ShipperEntity> availableShippers,
                                       ShipperAssignmentService assignmentService) {
        if (batches.isEmpty() || availableShippers.isEmpty()) {
            return;
        }
        
        // Sắp xếp các shipper theo số lượng đơn đang giao (ít trước)
        availableShippers.sort(Comparator.comparing(ShipperEntity::getCurrentOrderCount));
        
        for (OrderBatch batch : batches) {
            // Tìm shipper phù hợp nhất cho batch này
            ShipperEntity bestShipper = findBestShipperForBatch(batch, availableShippers);
            
            if (bestShipper != null) {
                // Phân công từng đơn trong batch cho shipper
                for (OrderEntity order : batch.getOrders()) {
                    try {
                        assignmentService.assignOrderToSpecificShipper(order.getId(), bestShipper.getId());
                        
                        // Cập nhật số lượng đơn của shipper
                        bestShipper.setCurrentOrderCount(bestShipper.getCurrentOrderCount() + 1);
                        
                        // Nếu shipper đạt giới hạn, loại khỏi danh sách shipper khả dụng
                        if (bestShipper.getCurrentOrderCount() >= bestShipper.getMaxConcurrentOrders()) {
                            availableShippers.remove(bestShipper);
                            break;
                        }
                    } catch (Exception e) {
                        log.error("Failed to assign order {} to shipper {}: {}", 
                                order.getId(), bestShipper.getId(), e.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Tìm shipper tốt nhất cho một batch đơn hàng
     */
    private ShipperEntity findBestShipperForBatch(OrderBatch batch, List<ShipperEntity> availableShippers) {
        if (availableShippers.isEmpty() || batch.getOrders().isEmpty()) {
            return null;
        }
        
        // Lấy tọa độ nhà hàng từ đơn đầu tiên
        OrderEntity firstOrder = batch.getOrders().get(0);
        double restaurantLat = firstOrder.getRestaurantLatitude();
        double restaurantLng = firstOrder.getRestaurantLongitude();
        
        ShipperEntity bestShipper = null;
        double minDistance = Double.MAX_VALUE;
        
        // Tìm shipper gần nhà hàng nhất
        for (ShipperEntity shipper : availableShippers) {
            // Bỏ qua các shipper không có thông tin vị trí
            if (shipper.getLatitude() == null || shipper.getLongitude() == null) {
                continue;
            }
            
            double distance = locationService.calculateDistance(
                    restaurantLat, restaurantLng,
                    shipper.getLatitude(), shipper.getLongitude()
            );
            
            // Kiểm tra xem shipper có đủ khả năng nhận toàn bộ đơn trong batch không
            if (distance < minDistance && 
                    shipper.getCurrentOrderCount() + batch.getOrders().size() <= shipper.getMaxConcurrentOrders()) {
                minDistance = distance;
                bestShipper = shipper;
            }
        }
        
        return bestShipper;
    }
    
    /**
     * Lớp đại diện cho một nhóm các đơn hàng từ nhà hàng gần nhau
     */
    @Getter
    public static class RestaurantCluster {
        private final double centerLat;
        private final double centerLng;
        private final List<OrderEntity> orders;
        
        public RestaurantCluster(double centerLat, double centerLng, List<OrderEntity> orders) {
            this.centerLat = centerLat;
            this.centerLng = centerLng;
            this.orders = orders;
        }

    }
    
    /**
     * Lớp đại diện cho một batch đơn hàng cần giao
     */
    @Setter
    @Getter
    public static class OrderBatch {
        private List<OrderEntity> orders;
        
        public OrderBatch() {
            this.orders = new ArrayList<>();
        }

        public void addOrder(OrderEntity order) {
            this.orders.add(order);
        }
        
        public Date getOldestOrderTime() {
            return orders.stream()
                    .map(order -> java.util.Date.from(order.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant()))
                    .min(Date::compareTo)
                    .orElse(new Date());
        }
        
        public int getBatchSize() {
            return orders.size();
        }
    }
}