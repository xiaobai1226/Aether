package com.xiaobai1226.aether.core.gateway;

import com.xiaobai1226.aether.core.filter.SaTokenFilter;
import com.xiaobai1226.aether.core.filter.SaTokenPermissionFilter;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Gateway;

import static com.xiaobai1226.aether.common.constant.GateWayTagConsts.API_ADMIN;

/**
 * Admin Gateway
 */
@Component
@Mapping("/api/admin/**")
public class AdminGateway extends Gateway {
    @Override
    protected void register() {
        filter(new SaTokenFilter());
        filter(new SaTokenPermissionFilter());
        addBeans(bw -> API_ADMIN.equals(bw.tag()));
    }
}