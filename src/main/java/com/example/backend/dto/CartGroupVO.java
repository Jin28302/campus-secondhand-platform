package com.example.backend.dto;

import com.example.backend.entity.Cart;
import com.example.backend.entity.Product;
import lombok.Data;

import java.util.List;

@Data
public class CartGroupVO {

    private Long sellerId;

    private String shopName;

    private List<CartItemVO> items;

    @Data
    public static class CartItemVO {
        private Long cartId;
        private Integer quantity;
        private Product product;

        public static CartItemVO of(Cart cart, Product product) {
            CartItemVO vo = new CartItemVO();
            vo.setCartId(cart.getId());
            vo.setQuantity(cart.getQuantity());
            vo.setProduct(product);
            return vo;
        }
    }
}
