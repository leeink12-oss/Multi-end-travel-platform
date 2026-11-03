package com.example.travelplatform.location;

import com.example.travelplatform.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@CrossOrigin
public class CityController {

    private final CityRepository cityRepository;

    public CityController(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @GetMapping
    public ApiResponse<List<City>> listEnabledCities() {
        return ApiResponse.success(cityRepository.findByEnabledTrueOrderByNameAsc());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ApiResponse<List<City>> listAll() {
        return ApiResponse.success(cityRepository.findAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<City> create(@RequestBody City city) {
        city.setId(null);
        return ApiResponse.success(cityRepository.save(city));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<City> update(@PathVariable Long id, @RequestBody City updated) {
        City city = cityRepository.findById(id).orElseThrow();
        city.setName(updated.getName());
        city.setProvince(updated.getProvince());
        city.setEnabled(updated.getEnabled());
        return ApiResponse.success(cityRepository.save(city));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        cityRepository.deleteById(id);
        return ApiResponse.success(null);
    }
}

