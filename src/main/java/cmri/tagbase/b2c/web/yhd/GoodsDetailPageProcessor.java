package cmri.tagbase.b2c.web.yhd;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.KindEntity;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;

import java.util.Date;

/**
 * Created by zhuyin on 3/3/15.
 */
public class GoodsDetailPageProcessor implements PageProcessor {
    private static final GoodsDetailPageProcessor processor = new GoodsDetailPageProcessor();

    public static Request getRequest(KindEntity goods){
        String url = String.format("http://item.yhd.com/item/%s", goods.getCode());
        return new Request(url, processor)
                .putExtra("goodsEntity", goods)
                ;
    }

    @Override
    public void process(ResultItems page) {
        KindEntity goods = page.getRequest().getExtra("goodsEntity", KindEntity.class);
        goods.set("detailedTime", new Date());

        Document doc = (Document) page.getResource();
        String brand = StringUtils.strip(doc.select("#brand_relevance").text());
        goods.set("品牌", brand);

        getLogger().trace(goods);
    }
}
