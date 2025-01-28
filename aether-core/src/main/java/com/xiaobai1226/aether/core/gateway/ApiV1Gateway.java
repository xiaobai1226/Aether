package com.xiaobai1226.aether.core.gateway;

import com.xiaobai1226.aether.core.filter.SaTokenFilter;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Gateway;

import static com.xiaobai1226.aether.common.constant.GateWayTagConsts.API_V1;

/**
 * Api V1版本 Gateway
 */
@Component
@Mapping("/api/v1/**")
public class ApiV1Gateway extends Gateway {
    @Override
    protected void register() {
        filter(new SaTokenFilter());
        addBeans(bw -> API_V1.equals(bw.tag()));
    }
}