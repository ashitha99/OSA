package com.practice.cartms.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CartItemDto {
    private Long productId;
    private double price;
    private int quantity;
}
