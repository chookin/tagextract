/**
 * Created by zhuyin on 5/6/15.
 */
m = function(){
    function splitOnSlash(brandArr) {
        var rst = [];
        for (var i in brandArr) {
            var myBrand = brandArr[i];
            if (myBrand.indexOf('/') != -1) {
                var indexSlash = myBrand.indexOf('/');
                var brand1 = myBrand.substring(0, indexSlash).trim();
                var brand2 = myBrand.substring(indexSlash + 1).trim();
                rst.push(brand1);
                rst.push(brand2);
            } else {
                rst.push(myBrand);
            }
        }
        return rst;
    }

    function splitOnParenthesis(brandArr) {
        var rst = [];
        for (var i in brandArr) {
            var myBrand = brandArr[i];
            if ((myBrand.indexOf("(") != -1 && myBrand.indexOf(")") == myBrand.length - 1) || (myBrand.indexOf("(") == 0 && myBrand.indexOf(")") != -1)) {
                // string in parentheses is the english name.
                var indexLParenthesis = myBrand.indexOf("(");
                var indexRParenthesis = myBrand.indexOf(")");
                var brand1 = myBrand.substring(0, indexLParenthesis).trim();
                var brand2 = myBrand.substring(indexLParenthesis + 1, indexRParenthesis).trim();
                rst.push(brand1);
                rst.push(brand2);
            } else if (myBrand.indexOf("(") != -1) {
                // only one '('
                indexLParenthesis = myBrand.indexOf("(");
                brand1 = myBrand.substring(0, indexLParenthesis).trim();
                brand2 = myBrand.substring(indexLParenthesis + 1).trim();
                rst.push(brand1);
                rst.push(brand2);
            } else if (myBrand.indexOf(")") != -1) {
                indexRParenthesis = myBrand.indexOf(")");
                brand1 = myBrand.substring(0, indexRParenthesis).trim();
                brand2 = myBrand.substring(indexRParenthesis + 1).trim();
                rst.push(brand1);
                rst.push(brand2);
            } else {
                rst.push(myBrand);
            }
        }
        return rst;
    }

    function splitOnPartChinese(brandArr) {
        var rst = [];
        for (var i in brandArr) {
            var myBrand = brandArr[i];
            // half is chinese character, and half is english character
            var startCh = myBrand.charCodeAt(0);
            var endCh = myBrand.charCodeAt(myBrand.length - 1);
            var iTurn = -1;
            if (startCh > 255 && endCh <= 255) {
                for (var i = 1; i < myBrand.length - 1; ++i) {
                    if (myBrand.charCodeAt(i) <= 255) {
                        if (iTurn == -1) {
                            iTurn = i; // changes happen
                        }
                    } else {
                        if (iTurn != -1) { // though happen changes, but back again.
                            iTurn = -1;
                            break;
                        }
                    }
                }
            } else if (startCh <= 255 && endCh > 255) {
                for (var i = 1; i < myBrand.length - 1; ++i) {
                    if (myBrand.charCodeAt(i) > 255) {
                        if (iTurn == -1) {
                            iTurn = i; // changes happen
                        }
                    } else {
                        if (iTurn != -1) { // though happen changes, but back again.
                            iTurn = -1;
                            break;
                        }
                    }
                }
            }
            if (iTurn != -1) {
                var brand1 = myBrand.substring(0, iTurn).trim();
                var brand2 = myBrand.substring(iTurn).trim();
                rst.push(brand1);
                rst.push(brand2);
            } else {
                rst.push(myBrand);
            }
        }
        return rst;
    }

    function filter(brandArr) {
        var reNum = /^\d*\.?\d*$/; // number
        var reG = /^\d*(g|G|(mg)|(ml)|L|°|%|&)\d*/;
        var rst = [];
        for (var i in brandArr) {
            var myBrand = brandArr[i];
            if (myBrand == null) {
                continue;
            }

            if (myBrand.indexOf('(') == 0 && myBrand.indexOf(')') == -1) {
                myBrand = myBrand.substring(1);
            }
            if (myBrand.indexOf(')') == myBrand.length - 1 && myBrand.indexOf('(') == -1) {
                myBrand = myBrand.substring(0, myBrand.length - 1);
            }
            if (myBrand.length == 0) {
                continue;
            }
            if (this.taboo.indexOf(myBrand) != -1 || reNum.test(myBrand) || reG.test(myBrand)) {
                continue;
            }
            if (myBrand.indexOf("其他") != -1) { // if the brand contains "其他", then ignore it.
                continue;
            }
            rst.push(myBrand);
        }
        return rst;
    }

    var brand = this.properties.品牌;
    brand = brand.replace('', ' ').trim(); // tb's white character
    brand = brand.replace('（', '(').replace('）', ')');

    var brandArr = [brand];
    brandArr = splitOnSlash(brandArr);
    brandArr = splitOnParenthesis(brandArr);
    brandArr = splitOnPartChinese(brandArr);
    brandArr = filter(brandArr);

    var brandsDict = {}; // key is brand, and value is it's goods count;
    for (var i in brandArr) {
        var brand = brandArr[i];
        brandsDict[brand] = 1;
    }
    emit(this.domain + "||" + this.tag,
        {
            brands: brandsDict,
            site: this.site
        });
};

r = function(key, values){
    var brandsDict = {};
    for(var i = 0; i< values.length; ++i){
        var value = values[i];
        for(var brand in value.brands){
            var myCount = value.brands[brand];
            if (brandsDict[brand] == undefined) {
                brandsDict[brand] = myCount;
            } else {
                brandsDict[brand] = brandsDict[brand] + myCount;
            }
        }
    }
    return {
        brands: brandsDict,
        site: values[0].site
    };
};

f = function(key, value){
    var countThreshhold = 3; // if count less than 3, then ignore this brand.
    if (value.brands.length < 10) { // if little, then output all.
        countThreshhold = 0;
    }

    // convert from dict to string, separated by '|'
    var brands = "";
    for (var brand in value.brands) {
        // only output that with more than or equal 3 goods.
        var count = value.brands[brand];
        if (count < countThreshhold) {
            continue;
        }
        if (brands.length == 0) {
            brands = brand;
        } else {
            brands = brands + "|" + brand;
        }
    }
    var keyArr = key.split("||");
    return {
        domain: keyArr[0],
        tag: keyArr[1],
        brands: brands,
        brandsDict: value.brands,
        site: value.site
    };
};

var inputCollection = "goods";

// some name exclude from brands
var nonsense = ["other", "其他", "2B", "al：ce", "5L*4", "国际","安道尔", "阿联酋", "阿富汗", "安提瓜和巴布达", "安格拉", "阿尔巴尼亚", "亚美尼亚", "荷兰属地", "安哥拉", "阿根廷", "东萨摩亚", "奥地利", "澳大利亚", "阿鲁巴", "阿塞拜疆", "波黑", "巴巴多斯", "孟加拉国", "比利时", "布基纳法索", "保加利亚", "巴林", "布隆迪", "贝宁", "文莱达鲁萨兰国", "玻利维亚", "巴西", "巴哈马", "不丹", "伯兹瓦纳", "白俄罗斯", "伯利兹", "加拿大", "科科斯群岛", "中非共和国", "刚果", "瑞士", "象牙海岸", "库克群岛", "智利", "喀麦隆", "中国", "哥伦比亚", "赤道几内亚", "哥斯达黎加", "古巴", "圣诞岛（英属）", "塞浦路斯", "捷克共和国", "德国", "吉布提", "丹麦", "多米尼加联邦", "多米尼加共和国", "阿尔及利亚", "厄瓜多尔", "爱沙尼亚", "埃及", "西萨摩亚", "西班牙", "埃塞俄比亚", "萨尔瓦多", "芬兰", "斐济", "福克兰群岛", "密克罗尼西亚", "法罗群岛", "法国", "加蓬", "大不列颠联合王国", "格林纳达", "格鲁吉亚", "法属圭亚那", "加纳", "直布罗陀", "格陵兰群岛", "冈比亚", "几内亚", "瓜德罗普岛（法属）", "希腊", "危地马拉", "关岛", "几内亚比绍", "圭亚那", "洪都拉斯", "克罗蒂亚", "海地", "匈牙利", "印度尼西亚", "爱尔兰共和国", "以色列", "印度", "英属印度洋领地", "伊拉克", "伊朗", "冰岛", "意大利", "牙买加", "约旦", "日本", "韩国", "肯尼亚", "吉尔吉斯斯坦", "柬埔塞", "基里巴斯", "科摩罗", "北朝鲜", "南朝鲜", "科威特", "开曼群岛（英属）", "哈萨克斯坦", "老挝人民共和国", "黎巴嫩", "圣露西亚岛", "列支敦士登", "斯里兰卡", "利比里亚", "莱索托", "立陶宛", "卢森堡", "拉脱维亚", "利比亚", "摩洛哥", "摩纳哥", "摩尔多瓦", "马达加斯加", "马绍尔群岛", "马里", "缅甸", "蒙古", "北马里亚纳群岛", "马提尼克岛（法属）", "毛里塔尼亚", "蒙塞拉特岛", "马尔他", "马尔代夫", "马拉维", "墨西哥", "马来西亚", "莫桑比克", "纳米比亚", "新喀里多尼亚", "尼日尔", "诺福克岛", "尼日利亚", "尼加拉瓜", "荷兰", "挪威", "尼泊尔", "瑙鲁", "纽埃", "新西兰", "阿曼", "巴拿马", "秘鲁", "法属玻利尼西亚", "巴布亚新几内亚", "菲律宾", "巴基斯坦", "波兰", "皮特克恩岛", "波多黎各", "葡萄牙", "帕劳", "巴拉圭", "卡塔尔", "留尼汪岛（法属）", "罗马尼亚", "俄罗斯联邦", "卢旺达", "沙特阿拉伯", "所罗门群岛", "塞舌尔", "苏旦", "瑞典", "新加坡", "海伦娜", "斯洛伐克", "塞拉利昂", "圣马力诺", "塞内加尔", "索马里", "苏里南", "叙利亚", "斯威士兰", "乍得", "法属南半球领地", "多哥", "泰国", "塔吉克斯坦", "托克劳群岛", "土库曼斯坦", "突尼斯", "汤加", "东帝汶", "土耳其", "图瓦鲁", "坦桑尼亚", "乌克兰", "乌干达", "英国", "美国", "乌拉圭", "梵地冈", "委内瑞拉", "维京群岛", "越南", "瓦努阿图", "东萨摩亚", "也门", "南斯拉夫", "南非", "赞比亚", "扎伊尔", "津巴布韦"];
result = db.runCommand({
    mapreduce: this.inputCollection,
    map: m,
    reduce: r,
    query: {
        "properties.品牌": {
            "$ne": null
        },
        "tag": {
            "$ne": null
        }
    },
    out: "goods_tag_brands",
    finalize:f,
    scope:{taboo: this.nonsense},
    verbose: true
});
printjson(result);