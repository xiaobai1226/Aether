package com.xiaobai1226.aether.core.task;

import com.xiaobai1226.aether.core.service.intf.RecycleBinService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.scheduling.annotation.Scheduled;

/**
 * 定时任务管理器
 *
 * @author bai
 */
@Component
@Slf4j
public class ScheduledTaskManager {

    @Inject
    private RecycleBinService recycleBinService;

    /**
     * 回收站自动清理任务
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredRecycleBinFiles() {
        recycleBinService.cleanExpiredRecycleBinFiles();
    }
}