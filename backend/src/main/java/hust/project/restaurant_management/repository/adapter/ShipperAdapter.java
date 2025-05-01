package hust.project.restaurant_management.repository.adapter;

import hust.project.restaurant_management.constants.ErrorCode;
import hust.project.restaurant_management.constants.ShipperStatusEnum;
import hust.project.restaurant_management.entity.ShipperEntity;
import hust.project.restaurant_management.exception.AppException;
import hust.project.restaurant_management.mapper.IShipperMapper;
import hust.project.restaurant_management.model.ShipperModel;
import hust.project.restaurant_management.port.IShipperPort;
import hust.project.restaurant_management.repository.IShipperRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShipperAdapter implements IShipperPort {
    IShipperRepository shipperRepository;
    IShipperMapper shipperMapper;

    @Override
    public ShipperEntity save(ShipperEntity shipperEntity) {
        try {
            ShipperModel model = shipperMapper.toModel(shipperEntity);
            return shipperMapper.toEntity(shipperRepository.save(model));
        } catch (Exception e) {
            throw new AppException(ErrorCode.CREATE_SHIPPER_FAILED);
        }
    }

    @Override
    public List<ShipperEntity> findAll() {
        return shipperMapper.toListEntity(shipperRepository.findAll());
    }

    @Override
    public ShipperEntity findById(Long id) {
        return shipperMapper.toEntity(shipperRepository.findById(id).orElse(null));
    }

    @Override
    public ShipperEntity findByUserId(Long userId) {
        return shipperMapper.toEntity(shipperRepository.findByUserId(userId).orElse(null));
    }

    @Override
    public List<ShipperEntity> findByStatus(ShipperStatusEnum status) {
        return shipperMapper.toListEntity(shipperRepository.findByStatus(status));
    }

    @Override
    public List<ShipperEntity> findAvailableShippers() {
        return shipperMapper.toListEntity(shipperRepository.findAvailableShippers());
    }

    @Override
    public List<ShipperEntity> findShippersWithCapacity() {
        return shipperMapper.toListEntity(shipperRepository.findShippersWithCapacity());
    }

    @Override
    public void deleteById(Long id) {
        shipperRepository.deleteById(id);
    }
}