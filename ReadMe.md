# 扫码登录实现Demo

**基本思路**：
1. 访问首页`http://ip:9999/qrcode-scan-login/index`（ip需使用同一网下可被其他设置访问的ip地址，而非localhost），生成用于登录二维码图片；
2. tab分为“轮询”、“长轮询”和“WebSocket”类型，选用不同，采取不同的实现方式；
3. 生成二维码后，超过设定时长`org.zturn.exhibition.config.Constants.PoolCacheConfig.QRCODE_TIMEOUT`，二维码会失效，需重新刷新获取；
4. 使用手机扫描二维码后，若二维码未失效，成功扫码后，页面会跳转新页面。

**基本框架**：
* jdk 1.8
* SpringBoot 2.1.2.RELEASE
* front end: thymeleaf

## 1. 轮询

## 2. 长轮询

## 3. Websocket