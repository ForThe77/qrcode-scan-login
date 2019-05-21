package org.zturn.exhibition.config;

/**
 * @Title: 常量配置数据类
 * @Description:
 * @author: Roy
 * @date: 2019/5/16 17:30
 */
public class Constants {

    /**
     * 二维码状态
     */
    public interface QRCodeStatus {
        // 未扫描（初始状态）
        String NOT_SCAN = "0";
        // 已扫描（成功）
        String SCANNED = "1";
        // 失效
        String INVALID = "2";
    }

    /**
     * 缓存池数据配置
     */
    public interface PoolCacheConfig {
        // 二维码uuid在缓存池的超时时间
        Long QRCODE_TIMEOUT = 30 * 1000L;
        // 缓存池的清理时间间隔
        Long CLEAN_INTERVAL_TIME = 25 * 1000L;
        // 缓存池的延迟清理时间
        Long CLEAN_DELAY_TIME = 10 * 1000L;
        // 长轮询后台执行睡眠时间
        Long LONGPOOL_DELAY_TIME = 30 * 1000L;
    }





}
