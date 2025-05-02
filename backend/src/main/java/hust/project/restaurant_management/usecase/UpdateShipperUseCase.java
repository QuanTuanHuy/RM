package hust.project.restaurant_management.usecase;

import hust.project.restaurant_management.constants.ErrorCode;
import hust.project.restaurant_management.entity.ShipperEntity;
import hust.project.restaurant_management.entity.dto.request.UpdateShipperRequest;
import hust.project.restaurant_management.exception.AppException;
import hust.project.restaurant_management.mapper.ShipperMapper;
import hust.project.restaurant_management.port.IShipperPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateShipperUseCase {
    private final IShipperPort shipperPort;
    private final ShipperMapper shipperMapper;

    @Transactional
    public ShipperEntity updateShipper(Long shipperId, UpdateShipperRequest request) {
        try {
            ShipperEntity shipper = shipperPort.findById(shipperId)
                    .orElseThrow(() -> new AppException(ErrorCode.SHIPPER_NOT_FOUND));
            
            // Update shipper
            shipperMapper.updateEntityFromRequest(shipper, request);
            
            // Save shipper
            return shipperPort.save(shipper);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("[UpdateShipperUseCase] updateShipper error: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UPDATE_SHIPPER_FAILED);
        }
    }
}