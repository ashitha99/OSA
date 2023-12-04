package com.practice.cartms.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CartDto {
    private Long cartId;
    private double totalPrice;
    private List<CartItemDto> cartItems;
}
