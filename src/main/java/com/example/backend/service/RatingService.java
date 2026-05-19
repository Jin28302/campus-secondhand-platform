package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.dto.RatingDTO;
import com.example.backend.entity.Review;

import java.util.List;

public interface RatingService extends IService<Review> {

    void rateSeller(RatingDTO dto);

    void rateBuyer(RatingDTO dto);

    List<Review> productRatings(Long productId);
}
