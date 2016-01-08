package cmri.tagbase.b2c.mobile.amazon;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.lang.StringHelper;
import org.apache.commons.lang3.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by zhuyin on 3/3/15.
 */
public class GoodsPageProcessor implements PageProcessor {
    private static final GoodsPageProcessor processor = new GoodsPageProcessor();

    public static Request getRequest(CategoryEntity category){
        return new Request(category.getUrl(),processor)
                .putExtra("categoryEntity", category)
                ;
    }

    @Override
    public void process(ResultItems page) {
        CategoryEntity categoryEntity = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);
        Validate.notNull(categoryEntity);
        Document doc = (Document) page.getResource();

        Elements boxes = doc.select("div.sx-result-table li.sx-table-item");
        for (Element item : boxes) {
            String name = item.select("h5").text();
            String url = item.select("a").first().absUrl("href");
            KindEntity goodsEntity = new KindEntity("goods")
                    .setName(name)
                    .setUrl(url)
                    .setCode(getCode(url))
                    .setCategory(categoryEntity)
                    .set("price", getPrice(item));
            getLogger().trace(goodsEntity);
            page.addItem(goodsEntity);
        }

        String next = getNextPageUrl(doc);
        if (next != null) {
            Request request = new Request(next,processor)
                    .putExtra("categoryEntity", categoryEntity);
            page.addTargetRequest(request);
        }
    }

    String getCode(String url) {
        String startKey = "aw/d/";
        int indexStart = url.indexOf(startKey);
        if (indexStart == -1) {
            return "";
        }
        indexStart += startKey.length();
        int indexEnd = url.indexOf("/ref");
        return url.substring(indexStart, indexEnd);
    }

    Double getPrice(Element element) {
//        <span class="a-size-base a-color-price s-price a-text-bold">￥168.00 - ￥298.00</span>
//        <span class="a-size-base a-color-price s-price a-text-bold">￥1,049.30</span>
        Elements priceElements = element.select(".a-color-price");
        if (priceElements.isEmpty()) {
            return null;
        }
        String strPrice = priceElements.first().text();
        strPrice = strPrice.replace(",","");
        strPrice = StringHelper.parseRegex(strPrice, "(\\d+\\.?\\d*)", 1);
        return Double.parseDouble(strPrice);
    }

    String getNextPageUrl(Document doc) {
        Elements elements = doc.select(".a-pagination li"); // mobile side
        if (!elements.isEmpty()) {
            elements = elements.last().select("a");
        } else {
            elements = doc.select("#pagnNextLink");// web side
        }
        if (elements.size() > 0) {
            String url = elements.last().absUrl("href");
            if (!url.trim().isEmpty()) {
                return url;
            }
        }
        return null;
    }
}
