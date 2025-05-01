package hust.project.restaurant_management.service;

import hust.project.restaurant_management.entity.ShipperEntity;
import hust.project.restaurant_management.entity.dto.request.CreateShipperRequest;
import hust.project.restaurant_management.entity.dto.request.UpdateShipperRequest;

import java.util.List;

public interface IShipperService {
    ShipperEntity createShipper(CreateShipperRequest request);
    ShipperEntity updateShipper(Long shipperId, UpdateShipperRequest request);
    List<ShipperEntity> getAllShippers();
}
