package cmri.tagbase.b2c.mobile.jd;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.SiteName;
import cmri.tagbase.orm.domain.CategoryEntity;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by zhuyin on 3/3/15.
 */
class CategoryPageProcessor3 implements PageProcessor {
    private static final CategoryPageProcessor3 processor = new CategoryPageProcessor3();
    public static Request getRequest(CategoryEntity parent){
        return new Request(parent.getUrl(), processor)
                .putExtra("parent", parent);
    }

    @Override
    public void process(ResultItems page) {
        Document doc = (Document) page.getResource();

        Elements box = doc.select("div.mc a");
        for (Element subBox : box) {
            String name = subBox.select("input").attr("value");

            String url = subBox.absUrl("href");
            int index = url.indexOf("?sid=");
            if (index == -1) {
                index = url.indexOf("?resourceType");
                if (index == -1) {
                    continue;
                }
            }
            url = url.substring(0, index);
            index = url.indexOf("ts/");
            String strId = url.substring(index + 3, url.indexOf(".htm"));
            CategoryEntity category = new CategoryEntity()
                    .setName(name)
                    .setCode(strId)
                    .setUrl(url)
                    .setSite(SiteName.Jd)
                    .setParent(page.getRequest().getExtra("parent", CategoryEntity.class));
            getLogger().trace(category);
            page.addItem(category);
        }
    }
}
