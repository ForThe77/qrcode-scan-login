package org.zturn.exhibition.model.dto;

import java.io.Serializable;

/**
 * @Title: 返回结果类
 * @Description:
 * @author: Roy
 * @date: 2019/5/16 19:07
 */
public class ResultDto<T> implements Serializable {

    private static final long serialVersionUID = -6195361174219795706L;

    /**
     * 返回标志
     */
    private boolean flag;
    /**
     * 返回数据
     */
    private T data;
    /**
     * 返回信息
     */
    private String msg;

    public ResultDto() {

    }

    public ResultDto(boolean flag, String msg) {
        this.flag = flag;
        this.msg = msg;
    }

    public ResultDto(boolean flag, T data, String msg) {
        this.flag = flag;
        this.data = data;
        this.msg = msg;
    }

    public void setFlagAndMsg(boolean flag, String msg) {
        setFlag(flag);
        setMsg(msg);
    }

    public void setFlagAndData(boolean flag, T data) {
        setFlag(flag);
        setData(data);
    }

    public void setAll(boolean flag, T data, String msg) {
        setFlag(flag);
        setData(data);
        setMsg(msg);
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
