package org.zturn.exhibition.socket;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Title: 二维码 Websocket配置
 * @Description:
 * @author: Roy
 * @date: 2019/5/17 17:20
 */
@ServerEndpoint("/qrcodeWebsocket/{uuid}") // 将目前的类定义成一个websocket服务器端
@Component
public class QrcodeWebsocket {

    private static final Logger LOGGER = LoggerFactory.getLogger(QrcodeWebsocket.class);

    // 用来记录当前在线连接数
    private static int onlineCount = 0;

    // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    // 当前Websocket存储的连接数据：uuid -> websocket数据
    private static final ConcurrentMap<String, QrcodeWebsocket> WEBSOCKET_MAP = new ConcurrentHashMap<>();


    /**
     * 连接建立成功时调用
     * @param uuid
     * @param session
     */
    @OnOpen
    public void onOpen(@PathParam("uuid") String uuid, Session session) {
        this.session = session;
        WEBSOCKET_MAP.put(uuid, this);
        addOnlineCount();
        LOGGER.info(MessageFormat.format("onOpen(${0})... onlineCount: {1}", uuid, getOnlineCount()));
    }

    /**
     * 连接关闭时调用
     * @param uuid
     */
    @OnClose
    public void onClose(@PathParam("uuid") String uuid) {
        WEBSOCKET_MAP.remove(uuid);
        subOnlineCount();
        LOGGER.info(MessageFormat.format("onClose(${0})... onlineCount: {1}", uuid, getOnlineCount()));
    }

    /**
     * 接收客户端消息后调用
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        LOGGER.info("onMessage()... message: " + message);
    }

    /**
     * 发生错误时调用
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        LOGGER.error("onError()...");
        error.printStackTrace();
    }

    /**
     * 发送消息给客户端
     * @param message
     * @throws IOException
     */
    private void sendMessage(Object message) throws IOException {
        String msgJson = "";
        if (null != message) {
            msgJson = JSON.toJSONString(message);
        }
        this.session.getBasicRemote().sendText(msgJson);
    }

    /**
     * 根据uuid，发送消息给指定客户端
     * @param uuid
     * @param message
     * @throws IOException
     */
    public static void sendMessage(String uuid, Object message) throws IOException {
        LOGGER.info(MessageFormat.format("发送消息给{0}，消息为：{1}", uuid, message));
        QrcodeWebsocket qrcodeWebsocket = WEBSOCKET_MAP.get(uuid);
        if (null != qrcodeWebsocket) {
            qrcodeWebsocket.sendMessage(message);
        } else {
            LOGGER.warn(MessageFormat.format("发送消息给{0}，发送失败，无相关连接！", uuid));
        }
    }

    private static synchronized void addOnlineCount() {
        QrcodeWebsocket.onlineCount++;
    }

    private static synchronized void subOnlineCount() {
        QrcodeWebsocket.onlineCount--;
    }

    private static synchronized int getOnlineCount() {
        return QrcodeWebsocket.onlineCount;
    }
}
