package com.xiaobai1226.aether.core.controller;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;

import static com.xiaobai1226.aether.core.constant.GateWayTagConsts.STATIC_RESOURCES;

@Component(tag = STATIC_RESOURCES)
public class StaticResourcesController {

    @Mapping
    public ModelAndView base() {
        ModelAndView model = new ModelAndView("index.html");
        return model;
    }
}
