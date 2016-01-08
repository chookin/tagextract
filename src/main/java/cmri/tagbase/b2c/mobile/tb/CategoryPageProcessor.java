package cmri.tagbase.b2c.mobile.tb;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.SiteName;
import cmri.tagbase.orm.domain.CategoryEntity;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by zhuyin on 3/3/15.
 */
public class CategoryPageProcessor implements PageProcessor {
    private static CategoryPageProcessor processor = new CategoryPageProcessor();

    public static Collection<Request> getSeedRequests(){
        return Collections.singletonList(new Request("http://m.taobao.com/channel/act/sale/quanbuleimu.html", processor)
        );
    }

    @Override
    public void process(ResultItems page) {
        Document doc = (Document) page.getResource();

        Elements elements = doc.select("div.txt-list-category-v2");
        for (Element item : elements) {
            String ancestorName = item.select("h3").text();
            String ancestorId = item.attr("id");
            CategoryEntity ancestor = new CategoryEntity()
                    .setName(ancestorName)
                    .setSite(SiteName.Taobao)
                    .setCode(ancestorId);
            getLogger().trace(ancestor);
            page.addItem(ancestor);

            Elements subElements = item.select("a");
            CategoryEntity parent = null;
            for (Element item3rd : subElements) {
                if (item3rd.attr("href").isEmpty()) {
                    String name = item3rd.text().trim();
                    if (name.isEmpty()) {
                        continue;
                    }
                    if (name.toCharArray()[0] == 160) {
                        continue;
                    }
                    parent = new CategoryEntity()
                            .setName(name)
                            .setSite(SiteName.Taobao)
                            .setParent(ancestor);
                    getLogger().trace(parent);
                    page.addItem(parent);
                } else {
                    String url = item3rd.absUrl("href");
                    try {
                        url = java.net.URLDecoder.decode(url, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(url, e);
                    }
                    String name = item3rd.text().trim();
                    if (name.isEmpty()) {
                        continue;
                    }
                    CategoryEntity grand = new CategoryEntity()
                            .setName(name)
                            .setUrl(url)
                            .setSite(SiteName.Taobao)
                            .setParent(parent);
                    if (parent == null) {
                        throw new RuntimeException("no parent of " + grand);
                    }
                    getLogger().trace(grand);
                    page.addItem(grand);
                }
            }
        }
    }
}
