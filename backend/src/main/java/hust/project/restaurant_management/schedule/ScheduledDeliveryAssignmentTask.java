package hust.project.restaurant_management.schedule;

import hust.project.restaurant_management.service.ShipperAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tác vụ định kỳ để tự động phân công shipper cho các đơn hàng đang chờ
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledDeliveryAssignmentTask {

    private final ShipperAssignmentService shipperAssignmentService;
    
    /**
     * Chạy định kỳ mỗi 30 giây để phân công shipper cho các đơn hàng đang chờ
     * Cron expression: second minute hour day-of-month month day-of-week
     */
    @Scheduled(cron = "0/30 * * * * *")
    public void assignShippersToQueuedOrders() {
        log.info("[ScheduledDeliveryAssignmentTask] Starting automatic shipper assignment task");
        try {
            shipperAssignmentService.assignShippersToQueuedOrders();
        } catch (Exception e) {
            log.error("[ScheduledDeliveryAssignmentTask] Error during automatic shipper assignment: {}", e.getMessage(), e);
        }
    }
}