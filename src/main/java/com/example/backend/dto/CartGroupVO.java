package com.example.backend.dto;

import com.example.backend.entity.Cart;
import com.example.backend.entity.Product;
import lombok.Data;

import java.util.List;

/**
 * 购物车分组视图对象 - 按商家分组展示购物车商品
 * 前端购物车页使用，每个 CartGroupVO 代表一个商家的所有购物车商品
 * 包含内部类 CartItemVO 表示单个购物车项及其关联的商品信息
 */
@Data
public class CartGroupVO {

    /** 商家（卖家）用户ID */
    private Long sellerId;

    /** 店铺名称 */
    private String shopName;

    /** 该商家下的购物车商品列表 */
    private List<CartItemVO> items;

    /**
     * 购物车项视图对象（内部类）
     * 包含购物车记录和对应的商品详情
     */
    @Data
    public static class CartItemVO {
        /** 购物车记录ID */
        private Long cartId;
        /** 购买数量 */
        private Integer quantity;
        /** 关联的商品详情（Product 实体） */
        private Product product;

        /**
         * 静态工厂方法：从 Cart 和 Product 创建 CartItemVO
         * @param cart 购物车记录
         * @param product 关联的商品
         * @return CartItemVO 实例
         */
        public static CartItemVO of(Cart cart, Product product) {
            CartItemVO vo = new CartItemVO();
            vo.setCartId(cart.getId());
            vo.setQuantity(cart.getQuantity());
            vo.setProduct(product);
            return vo;
        }
    }
}
