package cmri.tagbase.b2c.mobile.amazon;

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
        return Collections.singletonList(new Request("http://www.amazon.cn/l/aps", processor)
        );
    }

    @Override
    public void process(ResultItems page) {
        Document doc = (Document) page.getResource();
        Elements elements = doc.select("div#s-center-below-extra-content ul .a-list-item a");
        for (Element item : elements) {
            String name = item.text();
            if (name.contains("查看所有")) {
                continue;
            }
            String url = item.absUrl("href");
            if (url.isEmpty()) {
                continue;
            }
            CategoryEntity category = new CategoryEntity()
                    .setName(name)
                    .setUrl(url)
                    .setSite(SiteName.Amazon)
                    .setParent(page.getRequest().getExtra("parent", CategoryEntity.class));

            getLogger().trace(category);
            page.addItem(category);
            if (category.getLevel() < 5) { // max category level.
                page.addTargetRequest(new Request(url, processor)
                                .putExtra("parent", category)

                );
            }
        }
    }
}
