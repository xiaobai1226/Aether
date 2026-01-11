package com.xiaobai1226.aether.core.controller;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;

import static com.xiaobai1226.aether.common.constant.GateWayTagConsts.STATIC_RESOURCES;

@Component(tag = STATIC_RESOURCES)
public class StaticResourcesController {

    @Mapping
    public void base(Context ctx) {
        // ModelAndView model = new ModelAndView("index.html");
        // return model;
        ctx.forward("/index.html");
    }
}