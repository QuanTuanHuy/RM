package hust.project.restaurant_management.constants;

public enum ShipperStatusEnum {
    OFFLINE,    // Shipper không hoạt động
    AVAILABLE,  // Có thể nhận đơn hàng
    BUSY,       // Đang giao đơn hàng
    ON_BREAK    // Đang tạm nghỉ
}