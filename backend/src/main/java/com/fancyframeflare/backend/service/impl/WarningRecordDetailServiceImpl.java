package com.fancyframeflare.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fancyframeflare.backend.entity.WarningRecordDetail;
import com.fancyframeflare.backend.mapper.WarningRecordDetailMapper;
import com.fancyframeflare.backend.service.WarningRecordDetailService;
import org.springframework.stereotype.Service;

@Service
public class WarningRecordDetailServiceImpl extends ServiceImpl<WarningRecordDetailMapper, WarningRecordDetail>
                implements WarningRecordDetailService {
}
