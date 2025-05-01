package hust.project.restaurant_management.service.impl;

import hust.project.restaurant_management.entity.ShipperEntity;
import hust.project.restaurant_management.entity.dto.request.CreateShipperRequest;
import hust.project.restaurant_management.entity.dto.request.UpdateShipperRequest;
import hust.project.restaurant_management.service.IShipperService;
import hust.project.restaurant_management.usecase.CreateShipperUseCase;
import hust.project.restaurant_management.usecase.GetShipperUseCase;
import hust.project.restaurant_management.usecase.UpdateShipperUseCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShipperService implements IShipperService {
    CreateShipperUseCase createShipperUseCase;
    UpdateShipperUseCase updateShipperUseCase;
    GetShipperUseCase getShipperUseCase;

    @Override
    public ShipperEntity createShipper(CreateShipperRequest request) {
        return createShipperUseCase.createShipper(request);
    }

    @Override
    public ShipperEntity updateShipper(Long shipperId, UpdateShipperRequest request) {
        return updateShipperUseCase.updateShipper(shipperId, request);
    }

    @Override
    public List<ShipperEntity> getAllShippers() {
        return getShipperUseCase.getAllShippers() ;
    }
}
