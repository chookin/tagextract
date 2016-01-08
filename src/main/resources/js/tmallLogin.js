/**
 * Created by zhuyin on 1/28/15.
 * Just for test, have not been used.
 */
var url = "http://login.tmall.com/";
var casper = require('casper').create();
casper.start(url, function () {
    this.capture("1.png");
    this.echo("启动程序....");
});
//为登录准备
casper.then(function () {
    this.click("#u");
    this.capture("2.png");
    this.echo("点击登录的范围");
});
//输入登录信息
casper.then(function () {
    this.fill('form[id="loginform"]', {
        "u": "938065079",
        "p": "123456"
    }, false);
    this.capture("3.png");
    this.echo("等待点击登录按钮");

});
//点击登录按钮
casper.then(function () {
    this.click('input[class="signin-btn"]');
    this.capture("4.png");
    this.echo("已经点击登录按钮, 跳转等待.....");
    this.wait(3000, function () {
        this.echo(this.getTitle());
        this.capture("5.png");
        this.echo("登录成功");
    });
});
casper.run();