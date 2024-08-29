package com.xiaobai1226.aether.core.gateway;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Gateway;

import static com.xiaobai1226.aether.core.constant.GateWayTagConsts.STATIC_RESOURCES;

/**
 * 静态资源Gateway
 */
@Component
@Mapping
public class StaticResourcesGateway extends Gateway {
    @Override
    protected void register() {
        addBeans(bw -> STATIC_RESOURCES.equals(bw.tag()));
    }
}