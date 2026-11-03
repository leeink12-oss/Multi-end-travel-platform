package com.example.travelplatform.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HotelRequest {

    private Long cityId;
    private String name;
    private String description;
    private String address;
    private BigDecimal price;
    private String coverImage;
}

