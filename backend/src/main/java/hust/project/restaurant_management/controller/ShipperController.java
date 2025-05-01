package hust.project.restaurant_management.controller;

import hust.project.restaurant_management.entity.dto.request.CreateShipperRequest;
import hust.project.restaurant_management.entity.dto.request.UpdateShipperRequest;
import hust.project.restaurant_management.entity.dto.response.Resource;
import hust.project.restaurant_management.service.IShipperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/shippers")
public class ShipperController {
    private final IShipperService shipperService;

    @PostMapping
    public ResponseEntity<Resource> createShipper(@RequestBody CreateShipperRequest request) {
        return ResponseEntity.ok(new Resource(shipperService.createShipper(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Resource> updateShipper(
            @PathVariable(name = "id") Long id,
            @RequestBody UpdateShipperRequest request
    ) {
        return ResponseEntity.ok(new Resource(shipperService.updateShipper(id, request)));
    }

    @GetMapping()
    public ResponseEntity<Resource> getAll() {
        return ResponseEntity.ok(new Resource(shipperService.getAllShippers()));
    }
}
