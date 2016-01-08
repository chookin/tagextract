package cmri.tagbase.b2c.mobile.yhd;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.downloader.JsoupDownloader;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.lang.Pair;
import org.apache.commons.lang3.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by zhuyin on 3/3/15.
 */
class GoodsPageProcessor2 implements PageProcessor {
    static final GoodsPageProcessor2 processor = new GoodsPageProcessor2();
    public static Request getRequest(CategoryEntity category, String url){
        return new Request(url, processor)
                .putExtra("categoryEntity", category);
    }
    @Override
    public void process(ResultItems page) {
        Document doc = (Document) page.getResource();
        String url = page.getRequest().getUrl();
        getLogger().trace("extract page data: " + url);
        CategoryEntity categoryEntity = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);
        Validate.notNull(categoryEntity);

        getLogger().trace("extract page data: " + url);
        String userAgent = page.getSpider().getUserAgent(page.getRequest());
        try {
            Elements elements = doc.select("body li a");
            for (Element element : elements) {
                String name = element.select("div.title_box").text();
                String initUrl = element.absUrl("href");
                Pair<String, String> urlCode = getGoodsUrlCode(initUrl, userAgent);
                KindEntity goodsEntity = new KindEntity("goods")
                        .setName(name)
                        .setUrl(urlCode.getKey())
                        .setCategory(categoryEntity)
                        .setCode(urlCode.getValue());
                getLogger().trace(goodsEntity);
                page.addItem(goodsEntity);
            }
        } catch (UnsupportedOperationException | IOException ex) {
            throw new RuntimeException("url: " + url, ex);
        }
    }

    /**
     * @param url the origin url, which may be very complicated.
     * @return {redirectedUrl: code}
     * @throws IOException
     */
    Pair<String, String> getGoodsUrlCode(String url, String userAgent) throws IOException {
        String code = parseGoodsCode(url);
        if (code == null) {
            String redirectedUrl = JsoupDownloader.getInstance().getDocument(url, userAgent).baseUri();
            code = parseGoodsCode(redirectedUrl);
            return new Pair<>(redirectedUrl, code);
        } else {
            return new Pair<>(url, code);
        }
    }

    String parseGoodsCode(String url) {
        String startMark = "item/";
        int indexStart = url.indexOf(startMark);
        if (indexStart != -1) {
            indexStart += startMark.length();
            String endMark = "?";
            int indexEnd = url.indexOf(endMark, indexStart);
            if (indexEnd == -1) {
                return url.substring(indexStart);
            } else {
                return url.substring(indexStart, indexEnd);
            }
        } else {
            return null;
        }
    }
}
