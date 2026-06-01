package com.example.backend.controller;

import com.example.backend.common.R;
import com.example.backend.common.RequireRole;
import com.example.backend.dto.RatingDTO;
import com.example.backend.entity.Review;
import com.example.backend.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 评价控制器 - 处理买家评价卖家、卖家评价买家、商品评价查询
@RestController
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    // 买家评价卖家 - 买家对订单中的卖家进行评分和评价
    @PostMapping("/rating/seller")
    public R<Void> rateSeller(@RequestBody @Valid RatingDTO dto) {
        ratingService.rateSeller(dto);
        return R.ok();
    }

    // 卖家评价买家 - 商家对买家进行评分和评价
    @PostMapping("/rating/buyer")
    @RequireRole({"商家", "管理员"})
    public R<Void> rateBuyer(@RequestBody @Valid RatingDTO dto) {
        ratingService.rateBuyer(dto);
        return R.ok();
    }

    // 商品评价列表 - 查看指定商品的所有用户评价
    @GetMapping("/product/{id}/ratings")
    public R<List<Review>> productRatings(@PathVariable Long id) {
        return R.ok(ratingService.productRatings(id));
    }
}
