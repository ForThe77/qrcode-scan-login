package org.zturn.exhibition.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zturn.exhibition.cache.PoolCache;
import org.zturn.exhibition.config.Constants;
import org.zturn.exhibition.model.dto.ResultDto;
import org.zturn.exhibition.model.vo.QRCodeInfo;
import org.zturn.exhibition.service.QrcodeWebsocket;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


/**
 * @Title: 二维码扫描控制类
 * @Description:
 * @author: Roy
 * @date: 2019/5/16 10:50
 */
@SuppressWarnings("Duplicates")
@Controller
public class QrcodeScanController {

    private static final Logger LOGGER = LoggerFactory.getLogger(QrcodeScanController.class);


    @RequestMapping("greet")
    @ResponseBody
    public String greet() {
        return "hello!";
    }

    @RequestMapping("index")
    public String index(Model model) {
        //model.addAttribute("uuid", initQRCodeInfo());
        return "index";
    }

    @RequestMapping("/")
    public String home(Model model) {
        //model.addAttribute("uuid", initQRCodeInfo());
        return "index";
    }

    /**
     * 生成uuid
     * @return
     */
    @RequestMapping("generate")
    @ResponseBody
    public ResultDto generate() {
        String uuid = null;
        try {
            uuid = initQRCodeInfo();
        } catch (Exception e) {
            LOGGER.error("Generate uuid of QR code error!", e);
        }
        if (null == uuid) {
            return new ResultDto<>(false, null, "Generate uuid of QR code error!");
        }
        return new ResultDto<>(true, uuid, "Generate uuid of QR code successfully!");
    }


    /**
     * 初始化二维码信息，并添加至缓存池
     * @return uuid
     */
    private String initQRCodeInfo() {
        String uuid = UUID.randomUUID().toString().replaceAll("-","");
        PoolCache.init(uuid);
        return uuid;
    }

    /**
     * 轮询检查
     * @param uuid
     * @return
     */
    @RequestMapping("checkByPool")
    @ResponseBody
    public ResultDto checkByPool(String uuid) {
        ResultDto<QRCodeInfo> resultDto = new ResultDto<>();
        if (null == uuid || 0 == uuid.length()) {
            resultDto.setFlagAndMsg(false, "The parameter is null while pooling!");
            LOGGER.warn("轮询时，输入参数uuid为空！");
            return resultDto;
        }
        LOGGER.info(MessageFormat.format("查询二维码状态（{0}），检测是否登录。", uuid));
        if (!PoolCache.contains(uuid)) { // uuid对应数据为空，二维码失效
            resultDto.setAll(true,
                    new QRCodeInfo(null, Constants.QRCodeStatus.INVALID, null),
                    "The QR code is invalid, please retry it.");
            LOGGER.info(MessageFormat.format("该二维码（{0}）已失效！", uuid));
        } else {
            QRCodeInfo qrCodeInfo = PoolCache.get(uuid);
            resultDto.setFlagAndData(true, qrCodeInfo);
        }
        return resultDto;
    }

    /**
     * 长轮询检查
     * @param uuid
     * @return
     */
    @RequestMapping("checkByLongPool")
    @ResponseBody
    public ResultDto checkByLongPool(String uuid) {
        ResultDto<QRCodeInfo> resultDto = new ResultDto<>();
        if (null == uuid || 0 == uuid.length()) {
            resultDto.setFlagAndMsg(false, "The parameter is null while pooling!");
            LOGGER.warn("轮询时，输入参数uuid为空！");
            return resultDto;
        }
        LOGGER.info(MessageFormat.format("查询二维码状态（{0}），检测是否登录。", uuid));
        if (!PoolCache.contains(uuid)) { // uuid对应数据为空，二维码失效
            resultDto.setAll(true,
                    new QRCodeInfo(null, Constants.QRCodeStatus.INVALID, null),
                    "The QR code is invalid, please retry it.");
            LOGGER.info(MessageFormat.format("该二维码（{0}）已失效！", uuid));
        } else {
            QRCodeInfo qrCodeInfo = PoolCache.get(uuid);
            if (Constants.QRCodeStatus.NOT_SCAN.equals(qrCodeInfo.getStatus())) {
                // 如果二维码状态处于未扫面状态，则睡一会
                try {
                    Thread.sleep(Constants.PoolCacheConfig.LONGPOOL_DELAY_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                resultDto.setFlagAndData(true, qrCodeInfo);
            }
        }
        return resultDto;
    }

    /**
     * 扫码操作
     * @param uuid
     * @param tabId
     * @return
     */
    @RequestMapping("/scan/{uuid}")
    @ResponseBody
    public ResultDto scan(@PathVariable("uuid") String uuid, String tabId) {
        ResultDto<QRCodeInfo> resultDto = new ResultDto<>();
        if (null == uuid || 0 == uuid.length()) {
            resultDto.setFlagAndMsg(false, "The parameter is null while scanning!");
            LOGGER.warn("扫码时，传入参数uuid为空！");
            return resultDto;
        }
        LOGGER.info(MessageFormat.format("扫描二维码（{0}）进行登录操作。", uuid));
        try {
            if (!PoolCache.contains(uuid)) { // uuid对应数据为空，二维码失效
                resultDto.setAll(true,
                        new QRCodeInfo(null, Constants.QRCodeStatus.INVALID, null),
                        "The QR code is invalid, please retry it.");
                LOGGER.info(MessageFormat.format("该二维码（{0}）已失效！", uuid));
            } else {
                QRCodeInfo qrCodeInfo = PoolCache.get(uuid);
                if (Constants.QRCodeStatus.NOT_SCAN.equals(qrCodeInfo.getStatus())) {
                    qrCodeInfo.setStatus(Constants.QRCodeStatus.SCANNED); // 更新二维码状态
                    qrCodeInfo.setSession(getSessionByUuid(uuid));
                    resultDto.setAll(true, qrCodeInfo, "Scan QR code successfully!");
                }
            }
            if ("3".equals(tabId)) { // 若为websocket方式，则发送信息给客户端
                QrcodeWebsocket.sendMessage(uuid, resultDto);
            }
        } catch (Exception e) {
            LOGGER.error(MessageFormat.format("Scan error! <uuid:${0}, tabId:{1}>", uuid, tabId), e);
        }
        return resultDto;
    }

    /**
     * 根据uuid，获取session数据（含用户信息）
     * @param uuid
     * @return
     */
    private String getSessionByUuid(String uuid) {
        return "The session data of " + uuid;
    }

    /**
     * 扫码登录成功跳转
     * @return
     */
    @RequestMapping("scanSuccess")
    @ResponseBody
    public String scanSuccess() {
        return "扫码登录成功！";
    }

    /**
     * 推送消息
     * @param uuid
     * @param message
     * @return
     */
    @RequestMapping("/pushWebsocket/{uuid}")
    @ResponseBody
    public ResultDto pushWebsocket(@PathVariable("uuid") String uuid, String message) {
        try {
            QrcodeWebsocket.sendMessage(uuid, message);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResultDto(false, "Push message error: " + uuid + "#" + message);
        }
        return new ResultDto(true, "Push message successfully: "+ uuid);
    }

}
