package com.practice.cartms.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProductDto {
    private Long productId;
    private String productName;
    private double price;
}
