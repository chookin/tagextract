package cmri.tagbase.b2c.mobile.yhd;

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
class CategoryPageProcessor2 implements PageProcessor {
    private static final CategoryPageProcessor2 processor = new CategoryPageProcessor2();
    public static Request getRequest(CategoryEntity category){
        return new Request(category.getUrl(), processor)
                .putExtra("parent", category);
    }

    @Override
    public void process(ResultItems page) {
        Document doc = (Document) page.getResource();
        Elements box = doc.select("div.modSubnav div.borderBotm");
        for (Element subBox : box) {
            String myName = subBox.select("a p").first().text();
            CategoryEntity me = new CategoryEntity()
                    .setName(myName)
                    .setSite(SiteName.Yihaodian)
                    .setParent(page.getRequest().getExtra("parent", CategoryEntity.class));
            getLogger().trace(me);
            page.addItem(me);

            Elements cate3rdBox = subBox.select("div.subCon a");
            for (Element cate3rd : cate3rdBox) {
                String name = cate3rd.text();
                if (name.equals("所有商品")) {
                    continue;
                }
                String url = cate3rd.absUrl("href");
                String strId = url.substring(url.indexOf("ch/") + 3, url.indexOf("/p1-s1"));
                CategoryEntity childCategory = new CategoryEntity()
                        .setName(name)
                        .setCode(strId)
                        .setUrl(url)
                        .setSite(SiteName.Yihaodian)
                        .setParent(me);

                getLogger().trace(childCategory);
                page.addItem(childCategory);
            }
        }
    }
}
