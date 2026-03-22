package com.fancyframeflare.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fancyframeflare.backend.entity.ClassInfo;
import com.fancyframeflare.backend.mapper.ClassInfoMapper;
import com.fancyframeflare.backend.service.ClassInfoService;
import org.springframework.stereotype.Service;

@Service
public class ClassInfoServiceImpl extends ServiceImpl<ClassInfoMapper, ClassInfo> implements ClassInfoService {
}
