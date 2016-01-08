package cmri.tagbase.b2c.web.tb;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.KindEntity;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;

/**
 * Created by zhuyin on 3/2/15.
 */
public class GoodsDetailPageProcessor implements PageProcessor {
    private static final GoodsDetailPageProcessor processor = new GoodsDetailPageProcessor();

    public static Request getRequest(KindEntity goods){
        String url = goods.getUrl();
        if (url.contains("m.tmall")) {
            url = url.replace("m.tmall", "tmall");
        } else if (url.contains("h5.m.taobao")) {
            url = String.format("http://item.taobao.com/item.htm?id=%s", goods.getCode());
        }
        return new Request(url, processor)
                .putExtra("goodsEntity", goods)
                ;
    }

    @Override
    public void process(ResultItems page) {
        Document doc = (Document) page.getResource();
        if (doc.baseUri().contains("tmall")) {
            extractTmall(page);
        } else {
            extractTb(page);
        }
    }

    /**
     * http://detail.m.tmall.com/item.htm?id=39235127777&sid=ca572790f1b506f6&abtest=4&rn=1708149bddf15f866517643ea8f42333
     * convert to http://detail.tmall.com/item.htm?id=39235127777&sid=ca572790f1b506f6&abtest=4&rn=1708149bddf15f866517643ea8f42333
     */
    private void extractTmall(ResultItems page) {
        KindEntity goods = page.getRequest().getExtra("goodsEntity", KindEntity.class);
        goods.set("detailedTime", new Date());

        Document doc = (Document) page.getResource();
        Elements elements = doc.select("#J_AttrUL > li");
        for (Element e : elements) {
            String text = e.text();
            String[] kv = text.split(":|：");
            if (kv.length != 2) {
                continue;
            }
            String key = StringUtils.strip(kv[0]);
            String value = StringUtils.strip(kv[1]);
            goods.set(key, value);
        }
        getLogger().trace(goods);
    }

    /**
     * http://a.m.taobao.com/i37909638513.htm?sid=dc69bfadfa6b7be9&abtest=15&rn=7039f02bb3700aa31ce40246d36f1806
     * default redirect to
     * http://item.ny.taobao.com/item.htm?id=37909638513&sid=dc69bfadfa6b7be9&abtest=15&rn=7039f02bb3700aa31ce40246d36f1806
     * <p/>
     * http://h5.m.taobao.com/awp/core/detail.htm?id=43553021083&ali_refid=a3_430019_1006:1102254945:N:%CD%AF%D7%B0%A3%F4%D0%F4:8afb9e7960c93dd423a842f0a2d9012e&ali_trackid=1_8afb9e7960c93dd423a842f0a2d9012e
     */
    private void extractTb(ResultItems page) {
        KindEntity goods = page.getRequest().getExtra("goodsEntity", KindEntity.class);
        goods.set("detailedTime", new Date());

        Document doc = (Document) page.getResource();
        Elements elements = doc.select("#attributes > ul > li");
        for (Element e : elements) {
            String text = e.text();
            String[] kv = text.split(":|：");
            if (kv.length != 2) {
                continue;
            }
            String key = StringUtils.strip(kv[0]);
            String value = StringUtils.strip(kv[1]);
            goods.set(key, value);
        }
        getLogger().trace(goods);
    }
}
