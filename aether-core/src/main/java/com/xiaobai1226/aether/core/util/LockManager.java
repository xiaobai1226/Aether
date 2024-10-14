package com.xiaobai1226.aether.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 锁管理器
 */
public class LockManager {

    /**
     * 锁Map
     */
    private static final Map<String, Lock> lockMap = new ConcurrentHashMap<>();

    private static final Map<String, Long> lockTimeMap = new ConcurrentHashMap<>();

    /**
     * 锁过期时间，比如1小时
     */
    private static final int LOCK_EXPIRY_TIME_MS = 60 * 60 * 1000;

    /**
     * 清理任务运行间隔，比如30分钟
     */
    private static final int CLEAN_INTERVAL_MS = 30 * 60 * 1000;

    /**
     * 锁计数器Map
     */
//    private static final Map<String, AtomicInteger> referenceCount = new ConcurrentHashMap<>();

    static {
        startCleaningTask();
    }

    /**
     * 获取锁
     *
     * @param key 锁id
     * @return 锁对象
     */
    public static Lock getLock(String key) {
        return lockMap.computeIfAbsent(key, k -> new ReentrantLock());
    }

    /**
     * 上锁
     *
     * @param key 锁id
     */
    public static void lock(String key) {
        var lock = getLock(key);
        lock.lock();

        // 更新使用时间
        lockTimeMap.put(key, System.currentTimeMillis());

//        referenceCount.putIfAbsent(key, new AtomicInteger(0));
//        referenceCount.get(key).incrementAndGet();  // 增加引用计数
    }

    /**
     * 释放锁
     *
     * @param key 锁id
     */
    public static void unlock(String key) {
        var lock = getLock(key);
        lock.unlock();

        // 如果没有其他线程再使用此锁，则可以从map移除
        // 注意这里存在一个小的竞态条件：在判断引用计数并尝试删除的过程中，可能新的线程又开始获取这个锁了。所以这只是一个尽力删除不再使用的锁的尝试，不能保证一定能删除。
//        if (referenceCount.get(key).decrementAndGet() <= 0) {
//            if (lock.tryLock()) {
//                try {
//                    lockMap.remove(key);
//                    referenceCount.remove(key);
//                } finally {
//                    lock.unlock();
//                }
//            }
//        }
    }

    /**
     * 创建并启动清理任务
     */
    private static void startCleaningTask() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            for (Map.Entry<String, Long> entry : lockTimeMap.entrySet()) {
                if (now - entry.getValue() > LOCK_EXPIRY_TIME_MS) {
                    var lock = lockMap.get(entry.getKey());
                    if (lock.tryLock()) {
                        try {
                            lockMap.remove(entry.getKey());
                            lockTimeMap.remove(entry.getKey());
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            }
        }, CLEAN_INTERVAL_MS, CLEAN_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }
}
