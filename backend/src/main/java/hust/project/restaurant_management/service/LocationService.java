package hust.project.restaurant_management.service;

import org.springframework.stereotype.Service;

/**
 * Dịch vụ tính toán vị trí và khoảng cách sử dụng cho phân công shipper
 */
@Service
public class LocationService {

    // Bán kính trái đất (km)
    private static final double EARTH_RADIUS_KM = 6371;

    /**
     * Tính khoảng cách giữa hai điểm dựa trên tọa độ vĩ độ và kinh độ
     * Sử dụng công thức Haversine để tính khoảng cách trên bề mặt cầu
     * 
     * @param lat1 Vĩ độ điểm thứ nhất
     * @param lon1 Kinh độ điểm thứ nhất
     * @param lat2 Vĩ độ điểm thứ hai
     * @param lon2 Kinh độ điểm thứ hai
     * @return Khoảng cách giữa hai điểm (km)
     */
    public double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        // Công thức Haversine
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    /**
     * Ước tính thời gian đi từ điểm A đến điểm B (phút)
     * 
     * @param distanceKm Khoảng cách giữa hai điểm (km)
     * @param speedKmPerHour Tốc độ di chuyển trung bình (km/h)
     * @return Thời gian ước tính (phút)
     */
    public int estimateTravelTimeMinutes(double distanceKm, double speedKmPerHour) {
        // Mặc định tốc độ di chuyển trong thành phố là 20km/h nếu không được chỉ định
        if (speedKmPerHour <= 0) {
            speedKmPerHour = 20.0;
        }
        
        // Tính thời gian di chuyển (giờ) = khoảng cách / tốc độ
        double timeHours = distanceKm / speedKmPerHour;
        
        // Chuyển đổi từ giờ sang phút
        return (int) Math.ceil(timeHours * 60);
    }
    
    /**
     * Tính tổng khoảng cách cho một chuỗi điểm (ví dụ: tuyến đường giao hàng)
     * 
     * @param points Mảng các điểm dưới dạng [lat1, lon1, lat2, lon2, ...]
     * @return Tổng khoảng cách (km)
     */
    public double calculateRouteDistance(double[][] points) {
        if (points == null || points.length < 2) {
            return 0.0;
        }
        
        double totalDistance = 0.0;
        
        for (int i = 0; i < points.length - 1; i++) {
            totalDistance += calculateDistance(
                    points[i][0], points[i][1],
                    points[i+1][0], points[i+1][1]
            );
        }
        
        return totalDistance;
    }
    
    /**
     * Kiểm tra xem một điểm có nằm trong phạm vi một bán kính cụ thể từ điểm trung tâm không
     * 
     * @param centerLat Vĩ độ điểm trung tâm
     * @param centerLon Kinh độ điểm trung tâm
     * @param pointLat Vĩ độ điểm cần kiểm tra
     * @param pointLon Kinh độ điểm cần kiểm tra
     * @param radiusKm Bán kính (km)
     * @return true nếu điểm nằm trong phạm vi, false nếu không
     */
    public boolean isPointWithinRadius(double centerLat, double centerLon, double pointLat, double pointLon, double radiusKm) {
        double distance = calculateDistance(centerLat, centerLon, pointLat, pointLon);
        return distance <= radiusKm;
    }
}