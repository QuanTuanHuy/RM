package hust.project.restaurant_management.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service để tính phí giao hàng dựa trên khoảng cách
 */
@Service
@RequiredArgsConstructor
public class DeliveryFeeService {
    private final LocationService locationService;
    
    // Cài đặt phí giao hàng
    private static final double BASE_FEE = 15000.0; // Phí cơ bản
    private static final double PRICE_PER_KM = 5000.0; // Phí trên mỗi km
    private static final double FREE_DELIVERY_THRESHOLD = 300000.0; // Ngưỡng miễn phí giao hàng
    private static final double MAX_DISTANCE = 10.0; // Khoảng cách giao hàng tối đa (km)
    
    /**
     * Tính phí giao hàng dựa trên khoảng cách
     * 
     * @param restaurantLat vĩ độ nhà hàng
     * @param restaurantLon kinh độ nhà hàng
     * @param customerLat vĩ độ khách hàng
     * @param customerLon kinh độ khách hàng
     * @param orderAmount tổng giá trị đơn hàng
     * @return phí giao hàng (đơn vị tiền tệ)
     */
    public double calculateDeliveryFee(double restaurantLat, double restaurantLon, 
                                      double customerLat, double customerLon,
                                      double orderAmount) {
        // Nếu đơn hàng đạt ngưỡng miễn phí giao hàng
        if (orderAmount >= FREE_DELIVERY_THRESHOLD) {
            return 0.0;
        }
        
        // Tính khoảng cách giữa nhà hàng và khách hàng
        double distance = locationService.calculateDistance(restaurantLat, restaurantLon, 
                                                          customerLat, customerLon);
        
        // Kiểm tra nếu khoảng cách vượt quá giới hạn
        if (distance > MAX_DISTANCE) {
            throw new IllegalArgumentException("Khoảng cách giao hàng vượt quá giới hạn: " + distance + " km");
        }
        
        // Tính phí giao hàng: phí cơ bản + (khoảng cách * giá mỗi km)
        return BASE_FEE + (distance * PRICE_PER_KM);
    }
    
    /**
     * Tính phí giao hàng khi chưa có tọa độ chính xác
     * 
     * @param distance khoảng cách ước tính (km)
     * @param orderAmount tổng giá trị đơn hàng
     * @return phí giao hàng (đơn vị tiền tệ)
     */
    public double calculateDeliveryFeeByDistance(double distance, double orderAmount) {
        // Nếu đơn hàng đạt ngưỡng miễn phí giao hàng
        if (orderAmount >= FREE_DELIVERY_THRESHOLD) {
            return 0.0;
        }
        
        // Kiểm tra nếu khoảng cách vượt quá giới hạn
        if (distance > MAX_DISTANCE) {
            throw new IllegalArgumentException("Khoảng cách giao hàng vượt quá giới hạn: " + distance + " km");
        }
        
        // Tính phí giao hàng: phí cơ bản + (khoảng cách * giá mỗi km)
        return BASE_FEE + (distance * PRICE_PER_KM);
    }
    
    /**
     * Tính thời gian giao hàng ước tính
     * 
     * @param distance khoảng cách (km)
     * @param preparationTimeMinutes thời gian chuẩn bị đơn hàng (phút)
     * @return thời gian giao hàng ước tính (phút)
     */
    public int estimateDeliveryTime(double distance, int preparationTimeMinutes) {
        // Giả sử tốc độ trung bình của shipper là 25 km/h = 0.4167 km/phút
        double speedKmPerMinute = 25.0 / 60.0;
        
        // Thời gian di chuyển = khoảng cách / tốc độ
        int travelTimeMinutes = (int) Math.ceil(distance / speedKmPerMinute);
        
        // Thời gian giao hàng = thời gian chuẩn bị + thời gian di chuyển
        return preparationTimeMinutes + travelTimeMinutes;
    }
}