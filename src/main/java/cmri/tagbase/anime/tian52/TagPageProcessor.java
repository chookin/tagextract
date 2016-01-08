package cmri.tagbase.anime.tian52;

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
 * Created by zhuyin on 6/8/15.
 *
 * 天上人间动漫网
 */
public class TagPageProcessor implements PageProcessor {
    private static final TagPageProcessor processor = new TagPageProcessor();

    public static Collection<Request> getSeedRequests(){
        return Collections.singletonList(new Request("http://www.52tian.net/", processor)
        );
    }
    @Override
    public void process(ResultItems page) {
        Document doc = (Document) page.getResource();
        Elements elements = doc.select("div.ssnav a");

        for (Element item : elements) {
            String name = item.text();
            String url = item.absUrl("href");
            CategoryEntity category = new CategoryEntity().setName(name)
                    .setUrl(url)
                    .setSite(SiteName.Tian52);
            getLogger().trace(category);
            page.addItem(category);
        }
    }
}
