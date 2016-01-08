/**
 * 移动端淘宝网页在自动下拉滚动时可能会获取不到数据，此时，执行casperjs enable.js，可使得下次滚动时能获得到数据。
 * Created by zhuyin on 12/18/14.
 */
var casper = require('casper').create({
    clientScripts: [ // These  scripts will be injected in remote DOM on every request
        // 'include/jquery.js'
    ],
    pageSettings: {
        loadImages: false,        // The WebPage instance used by Casper will
        loadPlugins: false         // use these settings
    },
    verbose: true,
    logLevel: "debug"
});

var url = casper.cli.get("url");
var useragent = casper.cli.get("userAgent");
if (url == null) {
    url = "http://s.m.taobao.com/search.htm?q=%E5%AD%95%E5%A6%87%E5%A5%B6%E7%B2%89&spm=41.139785.167729.2";
}

if (useragent == null) {
    useragent = "Mozilla/5.0 (Linux; Android 4.4.2; M812C Build/KVT49L) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.59 Mobile Safari/537.36";
}
casper.echo('userAgent: ' + useragent);
casper.userAgent(useragent);

var scrollCount = 0;

debugger;

casper.echo("Casper CLI passed options:");
require("utils").dump(casper.cli.options);

function tryAndScroll(casper) {
    casper.wait(500, function () {
        this.scrollToBottom();
        this.emit("page.loaded");
    });
}

casper.start(url, function () {
    this.emit("page.loaded");
});

casper.run(function () {
    this.echo('Done of ' + url).exit(); // <--- don't forget me!
});

casper.on('page.loaded', function () {
    if(scrollCount > 2){
        return;
    }
    ++scrollCount;
    this.echo("scroll " + scrollCount);
    tryAndScroll(this);
});
