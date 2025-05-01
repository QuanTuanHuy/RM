package hust.project.restaurant_management.usecase;

import hust.project.restaurant_management.entity.ShipperEntity;
import hust.project.restaurant_management.port.IShipperPort;
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
public class GetShipperUseCase {
    IShipperPort shipperPort;

    public List<ShipperEntity> getAllShippers() {
        return shipperPort.findAll();
    }
}
