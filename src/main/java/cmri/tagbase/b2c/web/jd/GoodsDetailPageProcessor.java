package cmri.tagbase.b2c.web.jd;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.lang.TimeHelper;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;

/**
 * Created by zhuyin on 3/3/15.
 */
public class GoodsDetailPageProcessor implements PageProcessor {
    private static final GoodsDetailPageProcessor processor = new GoodsDetailPageProcessor();

    public static Request getRequest(KindEntity goods){
        String url = String.format("http://item.jd.com/%s.html", goods.getCode());
        return new Request(url, processor)
                .putExtra("goodsEntity", goods)
                ;
    }

    @Override
    public void process(ResultItems page) {
        extract(page);
    }

    private void extract(ResultItems page) {
        KindEntity goods = page.getRequest().getExtra("goodsEntity", KindEntity.class);
        goods.set("detailedTime", new Date());

        Document doc = (Document) page.getResource();
        Elements elements = doc.select("#parameter2 > li");
        for (Element e : elements) {
            String text = e.text();
            if (text.contains("商品名称") || text.contains("商品编号")) { // already has that properties as class property.
                continue;
            }
            String[] kv = text.split("：");
            if (kv.length != 2) {
                continue;
            }
            String key = StringUtils.strip(kv[0]);
            String value = StringUtils.strip(kv[1]);
            switch (key) {
                case "上架时间":
                    goods.set(key, TimeHelper.parseDate(value, "yyyy-MM-dd H:m:s"));
                    break;
                default:
                    goods.set(key, value);
            }
        }
        getLogger().trace(goods);
    }
}
