package com.fancyframeflare.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fancyframeflare.backend.entity.WarningRecord;
import com.fancyframeflare.backend.mapper.WarningRecordMapper;
import com.fancyframeflare.backend.service.WarningRecordService;
import org.springframework.stereotype.Service;

@Service
public class WarningRecordServiceImpl extends ServiceImpl<WarningRecordMapper, WarningRecord> implements WarningRecordService {
}
