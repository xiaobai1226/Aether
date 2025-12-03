package com.xiaobai1226.aether.core.util;

import com.xiaobai1226.aether.core.service.intf.RecycleBinService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Init;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
     * 定时任务执行器
     */
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    /**
     * 初始化定时任务
     */
    @Init
    public void init() {
        // 每天凌晨2点执行回收站清理任务
        // 计算到明天凌晨2点的延迟时间
        long initialDelay = calculateInitialDelay();
        long period = TimeUnit.DAYS.toMillis(1); // 每24小时执行一次

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                log.info("开始执行回收站自动清理任务");
                recycleBinService.cleanExpiredRecycleBinFiles();
                log.info("回收站自动清理任务执行完成");
            } catch (Exception e) {
                log.error("回收站自动清理任务执行失败: {}", e.getMessage(), e);
            }
        }, initialDelay, period, TimeUnit.MILLISECONDS);

        log.info("定时任务管理器初始化完成，回收站清理任务将在{}毫秒后首次执行，之后每24小时执行一次", initialDelay);
    }

    /**
     * 计算到明天凌晨2点的延迟时间
     *
     * @return 延迟时间（毫秒）
     */
    private long calculateInitialDelay() {
        java.util.Calendar now = java.util.Calendar.getInstance();
        java.util.Calendar nextRun = java.util.Calendar.getInstance();
        
        // 设置为明天凌晨2点
        nextRun.set(java.util.Calendar.HOUR_OF_DAY, 2);
        nextRun.set(java.util.Calendar.MINUTE, 0);
        nextRun.set(java.util.Calendar.SECOND, 0);
        nextRun.set(java.util.Calendar.MILLISECOND, 0);
        
        // 如果当前时间已经超过今天凌晨2点，则设置为明天凌晨2点
        if (now.after(nextRun)) {
            nextRun.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
        
        return nextRun.getTimeInMillis() - now.getTimeInMillis();
    }
}