package hust.project.restaurant_management.constants;

public enum DeliveryStatusEnum {
    PENDING,    // Đơn hàng đang chờ phân công shipper
    ASSIGNED,   // Đã phân công shipper
    PICKED_UP,  // Shipper đã nhận đơn hàng từ nhà hàng
    IN_TRANSIT, // Shipper đang giao hàng
    DELIVERED,  // Đã giao hàng thành công
    FAILED,     // Giao hàng thất bại
    CANCELLED   // Đơn hàng bị hủy
}