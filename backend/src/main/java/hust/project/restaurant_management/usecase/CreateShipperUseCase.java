package hust.project.restaurant_management.usecase;

import hust.project.restaurant_management.constants.ErrorCode;
import hust.project.restaurant_management.entity.ShipperEntity;
import hust.project.restaurant_management.entity.UserEntity;
import hust.project.restaurant_management.entity.dto.request.CreateShipperRequest;
import hust.project.restaurant_management.exception.AppException;
import hust.project.restaurant_management.mapper.ShipperMapper;
import hust.project.restaurant_management.port.IShipperPort;
import hust.project.restaurant_management.port.IUserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateShipperUseCase {
    private final IShipperPort shipperPort;
    private final IUserPort userPort;
    private final ShipperMapper shipperMapper;

    @Transactional
    public ShipperEntity createShipper(CreateShipperRequest request) {
        try {
            // Validate user exists
            UserEntity user = userPort.getUserById(request.getUserId());
            if (user == null) {
                log.error("[CreateShipperUseCase] createShipper: User not found");
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }

            // Check if user is already a shipper
            if (shipperPort.findByUserId(request.getUserId()) != null) {
                log.error("[CreateShipperUseCase] createShipper: User is already a shipper");
                throw new AppException(ErrorCode.CREATE_SHIPPER_FAILED);
            }

            // Create shipper entity
            ShipperEntity shipper = shipperMapper.toEntityFromCreateRequest(request);
            
            // Save shipper
            return shipperPort.save(shipper);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("[CreateShipperUseCase] createShipper error: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.CREATE_SHIPPER_FAILED);
        }
    }
}