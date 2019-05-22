package org.zturn.exhibition.model.vo;


import org.zturn.exhibition.config.Constants;

import java.io.Serializable;

/**
 * @Title: 二维码信息描述类
 * @Description:
 * @author: Roy
 * @date: 2019/5/16 17:19
 */
public class QRCodeInfo implements Serializable {

    private static final long serialVersionUID = -8154326828292956569L;

    /**
     * 创建时间
     */
    private Long createTime = System.currentTimeMillis();

    /**
     * 状态，初始为“未扫描”状态
     */
    private String status = Constants.QRCodeStatus.NOT_SCAN;

    /**
     * session数据
     */
    private Object session;

    public QRCodeInfo() {
    }

    public QRCodeInfo(Long createTime, String status, Object session) {
        this.createTime = createTime;
        this.status = status;
        this.session = session;
    }

    /**
     * 根据状态判断是否需要hold（同步方法）
     * @return
     */
    public synchronized void hold() {
        try {
            if (Constants.QRCodeStatus.NOT_SCAN.equals(status)) {
                new Thread(() -> {
                    try {
                        Thread.sleep(Constants.PoolCacheConfig.LONGPOOL_DELAY_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    this.notifyQRCodeInfo();
                }).start();

                // 若处于“未扫描”状态，则进入等待
                this.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 唤醒当前对象对应锁住的所有线程
     */
    public synchronized void notifyQRCodeInfo() {
        try {
            this.notifyAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public QRCodeInfo(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getSession() {
        return session;
    }

    public void setSession(Object session) {
        this.session = session;
    }

    public Long getCreateTime() {
        return createTime;
    }
}
