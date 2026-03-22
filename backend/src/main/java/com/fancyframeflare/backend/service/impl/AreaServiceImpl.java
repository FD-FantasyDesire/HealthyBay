package com.fancyframeflare.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fancyframeflare.backend.entity.Area;
import com.fancyframeflare.backend.mapper.AreaMapper;
import com.fancyframeflare.backend.service.AreaService;
import org.springframework.stereotype.Service;

@Service
public class AreaServiceImpl extends ServiceImpl<AreaMapper, Area> implements AreaService {
}
