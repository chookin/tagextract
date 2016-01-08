package cmri.tagbase.b2c.mobile.yhd;

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
        return Collections.singletonList(new Request("http://m.yhd.com/mw/sort?tp=5006.0.173.0.1.cwB7Cq",processor)
        );
    }

    @Override
    public void process(ResultItems page) {
        Document doc = (Document) page.getResource();

        Elements elements = doc.select("div.whiteBodrBox a");
        for (Element ancestorElement : elements) {
            String name = ancestorElement.select("p").first().text();
            String url = ancestorElement.absUrl("href");
            if (url.isEmpty()) {
                continue;
            }
            int index = url.indexOf("?bound");
            if (index == -1) {
                continue;
            }
            String code = url.substring(url.indexOf("rt/") + 3, index);
            CategoryEntity category = new CategoryEntity()
                    .setName(name)
                    .setSite(SiteName.Yihaodian)
                    .setCode(code)
                    .setUrl(url);
            getLogger().trace(category);
            page.addItem(category);

            page.addTargetRequest(CategoryPageProcessor2.getRequest(category));
        }
    }
}
