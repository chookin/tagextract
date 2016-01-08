package cmri.tagbase.site.chinaz;

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
        return Collections.singletonList(new Request("http://top.chinaz.com/", processor)
        );
    }

    @Override
    public void process(ResultItems page) {
        Document doc = (Document) page.getResource();

        CategoryEntity ancestor = new CategoryEntity()
                .setName(SiteName.Chinaz)
                .setSite(SiteName.Chinaz)
                .setUrl(page.getRequest().getUrl());
        page.addItem(ancestor);

        Elements elements = doc.select("#divtop3 > div.group");
        for (Element item : elements) {
            Element parentElement = item.select("h3 > a").first();
            String parentName = parentElement.text();
            parentName = parentName.substring(1, parentName.length() - 1);
            String parentUrl = parentElement.absUrl("href");
            CategoryEntity parent = new CategoryEntity()
                    .setName(parentName)
                    .setCode(getId(parentUrl))
                    .setUrl(parentUrl)
                    .setSite(SiteName.Chinaz)
                    .setParent(ancestor);
            getLogger().trace(parent);
            page.addItem(parent);

            Elements subElements = item.select("li > a");
            for (Element subElement : subElements) {
                String url = subElement.absUrl("href");
                String name = subElement.text();
                CategoryEntity child = new CategoryEntity()
                        .setName(name)
                        .setCode(getId(url))
                        .setUrl(url)
                        .setSite(SiteName.Chinaz)
                        .setParent(parent);

                getLogger().trace(child);
                page.addItem(child);
            }
        }
    }

    String getId(String url) {
        return url.substring(url.indexOf("=") + 1);
    }
}
