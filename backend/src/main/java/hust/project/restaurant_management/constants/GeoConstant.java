package hust.project.restaurant_management.constants;

public class GeoConstant {
    public static final double RESTAURANT_CLUSTER_RADIUS_KM = 1.0; // Bán kính cụm nhà hàng (km)
    public static final double DELIVERY_CLUSTER_RADIUS_KM = 2.0; // Bán kính cụm giao hàng (km)
    public static final int MAX_BATCH_SIZE = 3; // Số lượng đơn hàng tối đa trong một batch
    public static final double MAX_TOTAL_DISTANCE_KM = 8.0; // Tổng khoảng cách tối đa cho một batch (km)

    // Tọa độ mặc định của nhà hàng - cần cập nhật theo vị trí thực tế
    public static final double RESTAURANT_LATITUDE = 21.007835;
    public static final double RESTAURANT_LONGITUDE = 105.843030;
}
