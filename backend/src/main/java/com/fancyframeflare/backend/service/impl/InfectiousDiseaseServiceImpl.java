package com.fancyframeflare.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fancyframeflare.backend.entity.InfectiousDisease;
import com.fancyframeflare.backend.mapper.InfectiousDiseaseMapper;
import com.fancyframeflare.backend.service.InfectiousDiseaseService;
import org.springframework.stereotype.Service;

@Service
public class InfectiousDiseaseServiceImpl extends ServiceImpl<InfectiousDiseaseMapper, InfectiousDisease>
        implements InfectiousDiseaseService {
}
