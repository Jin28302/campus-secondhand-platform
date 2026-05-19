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

@RestController
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/rating/seller")
    public R<Void> rateSeller(@RequestBody @Valid RatingDTO dto) {
        ratingService.rateSeller(dto);
        return R.ok();
    }

    @PostMapping("/rating/buyer")
    @RequireRole({"商家"})
    public R<Void> rateBuyer(@RequestBody @Valid RatingDTO dto) {
        ratingService.rateBuyer(dto);
        return R.ok();
    }

    @GetMapping("/product/{id}/ratings")
    public R<List<Review>> productRatings(@PathVariable Long id) {
        return R.ok(ratingService.productRatings(id));
    }
}
