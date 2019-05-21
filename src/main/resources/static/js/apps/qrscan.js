!function () {
}();

$(function () {
    // 页面加载，选择默认点击tab
    $('.qrscan_total_place .tab_list_area .default').click();
});

/**
 * 记录tab id
 */
var tabId;

/**
 * tab点击事件绑定
 */
$('.qrscan_total_place .tab_list_area ul li').on('click', function () {
    tabId = $(this).attr('data-id');
    start();
});

/**
 * 生成uuid和二维码
 */
function start() {
    // 生成uuid
    $.get(ctxPath + '/generate', function (res) {
        var uuid;
        if (res && res.flag && (uuid = res.data)) {
            $('#uuid').val(uuid);
            init();
        } else {
            alert('Generate uuid of QR code error!\nPlease refresh your page.');
            clearQrInfo();
            updateTip('Please refresh your page.')
        }
    });
}

/**
 * checkByPool()的setInterval记录，用于清除
 */
var intervalOfCheckByPool;

/**
 * 初始化
 */
function init() {
    generateQRCode();
    switch (tabId) {
        case '1':
            console.log('select poll.');
            intervalOfCheckByPool = setInterval(checkByPool, 3000);
            break;
        case '2':
            console.log('select long poll.');
            checkByLongPool();
            break;
        case '3':
            console.log('select web socket.');
            checkByWebsocket();
            break;
        default:
            break;
    }
}

/**
 * 生成二维码
 */
function generateQRCode() {
    var uuid = $('#uuid').val();
    var url = ctx + '/scan/' + uuid + '?tabId=' + tabId;
    console.log('The QR code is:' + url);
    var $qrimg = clearQrInfo();
    $qrimg.qrcode({
        render: "canvas",
        width: 256,
        height: 256,
        correctLevel: 0, // 纠错等级
        text: url,
        background: '#ffffff',
        foreground: '#000000'
    });
    updateTip('Please scan QR code...');
}

/**
 * 清空原有轮询请求和二维码图片
 * @returns
 */
function clearQrInfo() {
    clearInterval(intervalOfCheckByPool); // 清除原有轮询请求
    if (websocket) { // 清除websocket
        websocket.close();
    }
    $('.qrcode_area .qrcode .qrmask').hide();
    var $qrimg = $('#qrimg');
    $qrimg.html(''); // 清空原有二维码图片
    return $qrimg;
}

/**
 * 轮询
 * @param uuid
 */
function checkByPool() {
    $.get(ctxPath + '/checkByPool', {uuid: $('#uuid').val()}, function (res) {
        if (!res || !res.flag) {
            console.log(res.msg || 'The result of poolCheck() is error!');
        }
        if (res.data) {
            var status = res.data.status;
            switch (status) {
                case '2':
                    console.log('The QR code is invalid, please retry it!');
                    clearInterval(intervalOfCheckByPool);
                    $('.qrcode_area .qrcode .qrmask').show();
                    updateTip('The QR code is invalid, please retry it!')
                    break;
                case '1':
                    console.log('Scan QR code successfully!');
                    clearInterval(intervalOfCheckByPool);
                    updateTip('Scan QR code successfully!');
                    window.location.href = ctxPath + '/scanSuccess';
                    break;
                case '0':
                    console.log('The QR code has not been scanned.');
                    break;
                default:
                    break;
            }
        }
    });
}

/**
 * 长轮询
 */
function checkByLongPool() {
    var poolCheck = $.get(ctxPath + '/checkByLongPool', {uuid: $('#uuid').val()}, function (res) {
        var isContinuePolling = true;
        if (!res || !res.flag) {
            console.log(res.msg || 'The result of poolCheck() is error!');
        }
        if (res.data) {
            var status = res.data.status;
            switch (status) {
                case '2':
                    console.log('The QR code is invalid, please retry it!');
                    isContinuePolling = false;
                    clearInterval(intervalOfCheckByPool);
                    $('.qrcode_area .qrcode .qrmask').show();
                    updateTip('The QR code is invalid, please retry it!');
                    break;
                case '1':
                    console.log('Scan QR code successfully!');
                    isContinuePolling = false;
                    clearInterval(intervalOfCheckByPool);
                    updateTip('Scan QR code successfully!');
                    window.location.href = ctxPath + '/scanSuccess';
                    break;
                case '0':
                    console.log('The QR code has not been scanned.');
                    break;
                default:
                    break;
            }
        }
        if (isContinuePolling) {
            poolCheck();
        }
    });
}

var websocket;

/**
 * websocket方式
 */
function checkByWebsocket() {
    //判断当前浏览器是否支持WebSocket
    if ('WebSocket' in window) {
        var url = "ws://localhost:9999" + ctxPath + "/qrcodeWebsocket/" + $('#uuid').val();
        websocket = new WebSocket(url);
    } else {
        alert('Websocket is not supported in current browser.');
    }
    //连接发生错误的回调方法
    websocket.onerror = function () {
        console.log("WebSocket连接发生错误");
    };
    //连接成功建立的回调方法
    websocket.onopen = function (event) {
        console.log("WebSocket连接成功 " + event.currentTarget.url);
    };
    //接收到消息的回调方法
    websocket.onmessage = function (event) {
        var res = JSON.parse(event.data);
        console.log("WebSocket接收到消息：" + res);
        if (!res || !res.flag) {
            console.log(res.msg || 'The result of checkByWebsocket() is error!');
        }
        if (res.data) {
            var status = res.data.status;
            switch (status) {
                case '2':
                    console.log('The QR code is invalid, please retry it!');
                    $('.qrcode_area .qrcode .qrmask').show();
                    updateTip('The QR code is invalid, please retry it!');
                    break;
                case '1':
                    console.log('Scan QR code successfully!');
                    updateTip('Scan QR code successfully!');
                    window.location.href = ctxPath + '/scanSuccess';
                    break;
                case '0':
                    console.log('The QR code has not been scanned.');
                    break;
                default:
                    break;
            }
        }
    };
    //连接关闭的回调方法
    websocket.onclose = function (event) {
        console.log("WebSocket连接关闭 " + event.currentTarget.url);
    };
    //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
    window.onbeforeunload = function () {
        websocket.close();
    };
}

/**
 * 更新二维码图片下tip信息
 * @param msg
 */
function updateTip(msg) {
    $('#qrtip').text(msg);
}

/**
 * 刷新二维码图片
 */
function refresh() {
    start();
}