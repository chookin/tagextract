package cmri.tagbase.b2c.mobile.tb;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.common.ValidateHelper;
import cmri.etl.downloader.JsoupDownloader;
import cmri.etl.pipeline.FilePipeline;
import cmri.etl.processor.PageProcessor;
import cmri.etl.spider.SpiderAdapter;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.Validate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by zhuyin on 3/3/15.
 */
public class JsonGoodsPageProcessor implements PageProcessor {
    private static final JsonGoodsPageProcessor pageProcessor = new JsonGoodsPageProcessor();

    public static JsonGoodsPageProcessor getInstance(){
        return pageProcessor;
    }
    public static Request getRequest(CategoryEntity category, String url){
        return new Request(url, pageProcessor)
                .setTarget(Request.TargetResource.Json)
                .setDownloader(JsoupDownloader.getInstance())
                .setPriority(8)
                .putExtra("categoryEntity", category);
    }

    @Override
    public void process(ResultItems page) {
        String url = page.getRequest().getUrl();
        getLogger().trace("extract page data: " + url);
        CategoryEntity categoryEntity = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);
        Validate.notNull(categoryEntity);

        try {
            String strJson = (String) page.getResource();
            strJson = strJson.substring(strJson.indexOf("(") + 1, strJson.lastIndexOf(")"));
            JsonParser parser = new JsonParser();
            JsonObject ojson = parser.parse(strJson).getAsJsonObject();
            JsonArray ajson = ojson.get("listItem").getAsJsonArray();
            for (JsonElement e : ajson) {
                JsonObject jGoods = e.getAsJsonObject();
                String name = jGoods.get("name").getAsString();
                if (name.contains("<")) {
                    name = Jsoup.parse(name).text();// name may be <font color='red'>[年货节]</font>费列罗巧克力礼盒装18粒进口高档生日情人节礼物送女神零食品包邮
                }
                String strId = jGoods.get("itemNumId").getAsString();
                String wUrl = jGoods.get("url").getAsString();
                String strPrice = jGoods.get("price").getAsString();
                KindEntity goodsEntity = new KindEntity("goods")
                        .setName(name)
                        .setUrl(wUrl)
                        .setCode(strId)
                        .setCategory(categoryEntity)
                        .set("price", Double.parseDouble(strPrice));
                getLogger().trace(goodsEntity);
                page.addItem(goodsEntity);
            }
        } catch (UnsupportedOperationException ex) {
            throw new RuntimeException("url: " + url, ex);
        }
    }

    String getRedirectUrl(String url, String userAgent) throws IOException {
        if (url.contains("mclick.simba")) {
            // mclick.simba.taobao.com, need redirection
            // for example: http://mclick.simba.taobao.com/cc_im?p=%BB%C6%BE%C6&s=109896524&k=313&e=cBBUlJqpstKMMD7UTXSag1tM1uYvknqK37uodw9C1WsXnBSGeu6%2FXLrF1ohw%2BUTcXy5eqpEkp2XVErP3XE1dyDkAmLLF0TqyaZwrUfcAOVdRzRhoHFzjF%2BlnJT%2B%2FGQ7Prj7Ovx%2B51JORhJSj6HpFDHg3cLvKJNcJYBP04B2a7t%2FNPHhMevcCPxiOY3Cp9ehAEpC3h7MoSU4zeef248d%2BHpPixaeuPR84%2Baqm%2Bps7H5F97XwL0jp6KZ1zeeDOsG1hXcG55ga%2BoBEwfzkETLBtbt5yHCG%2BaShspoTaReAh9CnVKdNI%2FXQxYw%3D%3D
            ResultItems resultItems = JsoupDownloader.getInstance().download(new Request().setUrl(url).ignoreCache(true),
                    new SpiderAdapter()
                            .userAgent(userAgent));
            Document doc = (Document) resultItems.getResource();
            if(!ValidateHelper.isValidationPage(doc.baseUri(), doc.html())) {
                new FilePipeline().process(resultItems);
                return doc.baseUri();
            }
        }
        return url;
    }
}
