package cmri.tagbase.music.baidu;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.SiteName;
import cmri.tagbase.orm.domain.CategoryEntity;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

/**
 * Created by zhuyin on 3/31/15.
 */
public class TagPageProcessor implements PageProcessor {
    private static final TagPageProcessor processor = new TagPageProcessor();

    public static Collection<Request> getSeedRequests(){
        return Collections.singletonList(new Request("http://music.baidu.com/tag", processor)
        );
    }
    @Override
    public void process(ResultItems page) {
        Document doc = (Document) page.getResource();
        Elements elements = doc.select("a.tag-item");
        for (Element item : elements) {
            String name = item.text();
            String url = item.absUrl("href");
            CategoryEntity category = new CategoryEntity().setName(name)
                    .setUrl(url)
                    .setSite(SiteName.BaiduMusic);
            getLogger().trace(category);
            page.addItem(category);
        }
    }
}
