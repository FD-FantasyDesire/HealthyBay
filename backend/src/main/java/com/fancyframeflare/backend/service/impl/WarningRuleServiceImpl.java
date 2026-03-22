package com.fancyframeflare.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fancyframeflare.backend.entity.WarningRule;
import com.fancyframeflare.backend.mapper.WarningRuleMapper;
import com.fancyframeflare.backend.service.WarningRuleService;
import org.springframework.stereotype.Service;

@Service
public class WarningRuleServiceImpl extends ServiceImpl<WarningRuleMapper, WarningRule> implements WarningRuleService {
}
