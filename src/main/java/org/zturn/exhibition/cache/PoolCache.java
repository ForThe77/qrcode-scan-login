package org.zturn.exhibition.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zturn.exhibition.config.Constants;
import org.zturn.exhibition.model.dto.ResultDto;
import org.zturn.exhibition.model.vo.QRCodeInfo;
import org.zturn.exhibition.socket.QrcodeWebsocket;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Title: 缓存池
 * @Description:
 * @author: Roy
 * @date: 2019/5/16 16:49
 */
@Component
public class PoolCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoolCache.class);

    /**
     * 缓存池
     * key -> value = uuid -> QRCodeInfo
     */
    private static final Map<String, QRCodeInfo> CACHE_MAP = new ConcurrentHashMap<>();

    /**
     * 初始化：开启一个线程专门用于清理缓存池中的过时数据
     */
    @PostConstruct
    public void init() {
        LOGGER.info("PoolCache::init(): Start a new Thread to clean pool cache.");
        Timer timer = new Timer("Scheduler-CleanPoolCache");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                LOGGER.info("Start to clean the pool cache.");
                try {
                    if (!CACHE_MAP.isEmpty()) {
                        Long currentTime = System.currentTimeMillis();
                        for (Map.Entry<String, QRCodeInfo> entry : CACHE_MAP.entrySet()) {
                            if (Constants.PoolCacheConfig.QRCODE_TIMEOUT < currentTime - entry.getValue().getCreateTime()){
                                // 去除缓存池中的过期数据
                                CACHE_MAP.remove(entry.getKey());
                                // 若为websocket方式，则发送二维码失效消息给客户端
                                QrcodeWebsocket.sendMessage(entry.getKey(),
                                        new ResultDto<>(false, new QRCodeInfo(null, Constants.QRCodeStatus.INVALID, null),
                                                "The QR code is invalid, please retry it."));
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Clean the pool cache error!", e);
                }
            }
        }, Constants.PoolCacheConfig.CLEAN_DELAY_TIME, Constants.PoolCacheConfig.CLEAN_INTERVAL_TIME);
    }

    /**
     * 根据uuid，获取二维码信息
     * @param uuid
     * @return
     */
    public static QRCodeInfo get(String uuid) {
        return CACHE_MAP.get(uuid);
    }

    /**
     * 初始化某个二维码数据，设置uuid为key，新建QRCodeInfo为value。
     * @param uuid
     */
    public static void init(String uuid) {
        CACHE_MAP.put(uuid, new QRCodeInfo(Constants.QRCodeStatus.NOT_SCAN));
    }

    /**
     * 判断uuid是否存在
     * @param uuid
     * @return
     */
    public static boolean contains(String uuid) {
        return CACHE_MAP.containsKey(uuid);
    }

}
