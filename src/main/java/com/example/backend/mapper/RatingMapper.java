package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Rating;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RatingMapper extends BaseMapper<Rating> {
}
