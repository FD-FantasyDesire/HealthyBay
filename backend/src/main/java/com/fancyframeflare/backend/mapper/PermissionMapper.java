package com.fancyframeflare.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fancyframeflare.backend.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}
