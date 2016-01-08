/**
 * Created by zhuyin on 12/18/14.
 */
// casperjs --proxy=proxy.cmcc:8080 tbGoodsScroll.js --url="http://s.m.taobao.com/search.htm?q=%E5%AD%95%E5%A6%87%E5%A5%B6%E7%B2%89&spm=41.139785.167729.2" --out="tbGoods.json"
// casperjs --proxy=proxy.cmcc:8080 tbGoodsScroll.js
// casperjs tbGoodsScroll.js  --remote-debugger-port=9000

// http://casperjs.readthedocs.org/en/latest/cli.html
// http://casperjs.readthedocs.org/en/latest/debugging.html
// http://casperjs.readthedocs.org/en/latest/modules/casper.html

// An options object can be passed to the Casper constructor
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
var out = casper.cli.get("out");
var useragent = casper.cli.get("userAgent");
if (url == null) {
    url = "http://s.m.taobao.com/search.htm?q=%E5%AD%95%E5%A6%87%E5%A5%B6%E7%B2%89&spm=41.139785.167729.2";
}
if (out == null) {
    out = "tbGoods";
}else{
    var ext = ".htm";
    var indexExt = out.indexOf(ext);
    if(indexExt == out.length - ext.length){
        out = out.substring(0, indexExt);
    }
}
if (useragent == null) {
    useragent = "Mozilla/5.0 (Linux; Android 4.4.2; M812C Build/KVT49L) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.59 Mobile Safari/537.36";
}
casper.echo('userAgent: ' + useragent);
casper.userAgent(useragent);

var scrollCount = 0;
var totalPageNum = 0;

debugger;

casper.echo("Casper CLI passed options:");
require("utils").dump(casper.cli.options);

function tryAndScroll(casper) {
    casper.wait(500, function () {
        this.scrollToBottom();
        this.emit("page.loaded");
    });
}

function getCurPageNum(casper) {
    var strCurPageNum = casper.getElementInfo(".currentPage").text;
    return parseInt(strCurPageNum);
}
function getTotalPageNum(casper) {
    if (totalPageNum > 0) {
        return totalPageNum;
    }
    var txt = casper.getElementInfo(".pagenav-normal").text;
    var indexStart = txt.indexOf("/") + 1;
    var indexEnd = txt.indexOf("下");
    if (indexEnd > indexStart) {
        totalPageNum = txt.substr(indexStart, indexEnd - indexStart);
        totalPageNum = parseInt(totalPageNum);
    }
    return totalPageNum;
}

casper.start(url, function () {
    this.emit("page.loaded");
});

casper.run(function () {
    dumpPageContent(this);
    this.echo('Done of ' + url).exit(); // <--- don't forget me!
});

casper.on('page.loaded', function () {
    var filename = out + ".pages";
    console.log("dump pages num to file " + filename);
    require('fs').write(filename, getTotalPageNum(this), 'w');
    var curPageNum = getCurPageNum(casper);
    console.log("cur page " + curPageNum);
    if (curPageNum == 2) {
        return;
    }

    ++scrollCount;
    if(scrollCount > 5){
        return;
    }
    this.echo("scroll " + scrollCount);
    tryAndScroll(this);
});

//Casper (well, actually PhantomJS) supplies two callbacks, one when the resource is requested (where you can see headers being sent), and one when response is received (so you can see the headers the server replied with).
//http://phantomjs.org/api/webpage/handler/on-resource-requested.html
//http://phantomjs.org/api/webpage/handler/on-resource-received.html
casper.options.onResourceRequested = function (C, requestData, request) {
    var url = requestData.url;
    if (url.indexOf("&page=") >= 0) {
        console.log("get requested url is " + url);
        var filename = out + ".url";
        console.log("dump requested url to file " + filename);
        require('fs').write(filename, url, 'w');
    }
};

function dumpPageContent(casper) {
    var html = casper.getPageContent();
    var filename = out + ".htm";
    casper.echo("dump page content to file " + filename);
    require('fs').write(filename, html, 'w');
}