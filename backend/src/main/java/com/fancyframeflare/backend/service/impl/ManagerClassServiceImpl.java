package com.fancyframeflare.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fancyframeflare.backend.entity.ManagerClass;
import com.fancyframeflare.backend.mapper.ManagerClassMapper;
import com.fancyframeflare.backend.service.ManagerClassService;
import org.springframework.stereotype.Service;

@Service
public class ManagerClassServiceImpl extends ServiceImpl<ManagerClassMapper, ManagerClass> implements ManagerClassService {
}
