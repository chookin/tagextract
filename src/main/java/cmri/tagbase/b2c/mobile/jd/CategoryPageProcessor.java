package cmri.tagbase.b2c.mobile.jd;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.SiteName;
import cmri.tagbase.orm.domain.CategoryEntity;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by zhuyin on 3/3/15.
 */
public class CategoryPageProcessor implements PageProcessor {
    private static CategoryPageProcessor processor = new CategoryPageProcessor();

    public static Collection<Request> getSeedRequests(){
        return Collections.singletonList(new Request("http://m.jd.com/category/all.html",processor)
        );
    }

    @Override
    public void process(ResultItems page) {
        Document doc = (Document) page.getResource();

        Elements elements = doc.select(".new-category-li");
        for (Element item : elements) {
            Element ancestorElement = item.select(".new-category-a").first();
            String ancestorName = ancestorElement.text();
            String ancestorId = ancestorElement.attr("id");
            CategoryEntity ancestor = new CategoryEntity()
                    .setName(ancestorName)
                    .setSite(SiteName.Jd)
                    .setCode(ancestorId);
            page.addItem(ancestor);
            getLogger().trace(ancestor);

            Elements subElements = item.select(".new-category2-a");
            for (Element subElement : subElements) {
                String url = subElement.select("a").first().absUrl("href");
                if (url.isEmpty()) {
                    continue;
                }
                int index = url.indexOf("?resourceType=");
                if (index == -1) {
                    continue;
                }
                url = url.substring(0, index);
                index = url.indexOf("ts/");
                String strId = url.substring(index + 3, url.indexOf(".htm"));
                String name = subElement.text();
                CategoryEntity parent = new CategoryEntity()
                        .setName(name)
                        .setCode(strId)
                        .setUrl(url)
                        .setSite(SiteName.Jd)
                        .setParent(ancestor);
                getLogger().trace(parent);
                page.addItem(parent);

                page.addTargetRequest(CategoryPageProcessor3.getRequest(parent));
            }
        }
    }
}
