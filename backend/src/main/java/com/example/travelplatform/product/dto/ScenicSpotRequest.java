package com.example.travelplatform.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScenicSpotRequest {

    private Long cityId;
    private String name;
    private String description;
    private String address;

    // 是否免费景点：true=免费，false/null=收费
    private Boolean free;

    // 不同人群票价
    private BigDecimal childPrice;
    private BigDecimal adultPrice;
    private BigDecimal studentPrice;

    // 兼容旧字段，可留空，后台会根据上面的票价自动计算
    private BigDecimal price;

    private String coverImage;
}

