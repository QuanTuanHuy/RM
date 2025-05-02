package hust.project.restaurant_management.repository.adapter;

import hust.project.restaurant_management.constants.ErrorCode;
import hust.project.restaurant_management.constants.ShipperStatusEnum;
import hust.project.restaurant_management.entity.ShipperEntity;
import hust.project.restaurant_management.exception.AppException;
import hust.project.restaurant_management.mapper.IShipperMapper;
import hust.project.restaurant_management.model.ShipperModel;
import hust.project.restaurant_management.port.IShipperPort;
import hust.project.restaurant_management.repository.IShipperRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipperAdapter implements IShipperPort {
    private final IShipperRepository shipperRepository;
    private final IShipperMapper shipperMapper;

    @Override
    public ShipperEntity save(ShipperEntity shipperEntity) {
        try {
            ShipperModel shipperModel = shipperMapper.toModelFromEntity(shipperEntity);
            return shipperMapper.toEntityFromModel(shipperRepository.save(shipperModel));
        } catch (Exception e) {
            log.error("[ShipperAdapter] save: error: {}", e.getMessage());
            throw new AppException(ErrorCode.CREATE_SHIPPER_FAILED);
        }
    }

    @Override
    public Optional<ShipperEntity> findById(Long id) {
        try {
            Optional<ShipperModel> shipperModelOptional = shipperRepository.findById(id);
            return shipperModelOptional.map(shipperMapper::toEntityFromModel);
        } catch (Exception e) {
            log.error("[ShipperAdapter] findById: error: {}", e.getMessage());
            throw new AppException(ErrorCode.GET_SHIPPER_FAILED);
        }
    }

    @Override
    public List<ShipperEntity> findAll() {
        return shipperMapper.toEntitiesFromModels(shipperRepository.findAll());
    }

    @Override
    public Optional<ShipperEntity> findByUserId(Long userId) {
        try {
            Optional<ShipperModel> shipperModelOptional = shipperRepository.findByUserId(userId);
            return shipperModelOptional.map(shipperMapper::toEntityFromModel);
        } catch (Exception e) {
            log.error("[ShipperAdapter] findByUserId: error: {}", e.getMessage());
            throw new AppException(ErrorCode.GET_SHIPPER_FAILED);
        }
    }

    @Override
    public ShipperEntity updateLocation(Long id, Double latitude, Double longitude, String address) {
        try {
            Optional<ShipperModel> shipperModelOptional = shipperRepository.findById(id);
            if (shipperModelOptional.isEmpty()) {
                throw new AppException(ErrorCode.SHIPPER_NOT_FOUND);
            }
            
            ShipperModel shipperModel = shipperModelOptional.get();
            shipperModel.setLatitude(latitude);
            shipperModel.setLongitude(longitude);
            shipperModel.setCurrentAddress(address);
            shipperModel.setLastLocationUpdateTime(LocalDateTime.now());
            
            return shipperMapper.toEntityFromModel(shipperRepository.save(shipperModel));
        } catch (Exception e) {
            log.error("[ShipperAdapter] updateLocation: error: {}", e.getMessage());
            throw new AppException(ErrorCode.UPDATE_SHIPPER_FAILED);
        }
    }

    @Override
    public ShipperEntity updateStatus(Long id, ShipperStatusEnum status) {
        try {
            Optional<ShipperModel> shipperModelOptional = shipperRepository.findById(id);
            if (shipperModelOptional.isEmpty()) {
                throw new AppException(ErrorCode.SHIPPER_NOT_FOUND);
            }
            
            ShipperModel shipperModel = shipperModelOptional.get();
            shipperModel.setStatus(status);
            
            // Cập nhật trạng thái sẵn sàng dựa trên status
            if (status == ShipperStatusEnum.AVAILABLE) {
                shipperModel.setIsAvailable(true);
            } else if (status == ShipperStatusEnum.OFFLINE || status == ShipperStatusEnum.BUSY) {
                shipperModel.setIsAvailable(false);
            }
            
            return shipperMapper.toEntityFromModel(shipperRepository.save(shipperModel));
        } catch (Exception e) {
            log.error("[ShipperAdapter] updateStatus: error: {}", e.getMessage());
            throw new AppException(ErrorCode.UPDATE_SHIPPER_FAILED);
        }
    }

    @Override
    public List<ShipperEntity> findAvailableShippers() {
        try {
            List<ShipperModel> availableShippers = shipperRepository.findAllAvailableForNewOrders();
            return shipperMapper.toEntitiesFromModels(availableShippers);
        } catch (Exception e) {
            log.error("[ShipperAdapter] findAvailableShippers: error: {}", e.getMessage());
            throw new AppException(ErrorCode.GET_SHIPPER_FAILED);
        }
    }

    @Override
    public Integer countCurrentOrders(Long shipperId) {
        try {
            Optional<ShipperModel> shipperModelOptional = shipperRepository.findById(shipperId);
            if (shipperModelOptional.isEmpty()) {
                throw new AppException(ErrorCode.SHIPPER_NOT_FOUND);
            }
            
            return shipperModelOptional.get().getCurrentOrderCount();
        } catch (Exception e) {
            log.error("[ShipperAdapter] countCurrentOrders: error: {}", e.getMessage());
            throw new AppException(ErrorCode.GET_SHIPPER_FAILED);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            shipperRepository.deleteById(id);
        } catch (Exception e) {
            log.error("[ShipperAdapter] deleteById: error: {}", e.getMessage());
            throw new AppException(ErrorCode.DELETE_SHIPPER_FAILED);
        }
    }
}