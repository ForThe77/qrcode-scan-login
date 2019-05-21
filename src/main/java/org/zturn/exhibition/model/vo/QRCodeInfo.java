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

    /**
     * 创建时间
     */
    private Long createTime = System.currentTimeMillis();

    /**
     * 状态
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
