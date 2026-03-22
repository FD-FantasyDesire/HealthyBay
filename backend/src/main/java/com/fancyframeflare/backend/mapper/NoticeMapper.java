package com.fancyframeflare.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fancyframeflare.backend.entity.Notice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
}
