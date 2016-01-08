package cmri.tagbase.video.youku;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.SiteName;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.utils.lang.StringHelper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

/**
 * Created by zhuyin on 4/8/15.
 */
public class CategoryPageProcessor implements PageProcessor {
    private static final CategoryPageProcessor processor = new CategoryPageProcessor();

    public static Collection<Request> getSeedRequests(){
        return Collections.singletonList(new Request("http://www.youku.com/v_showlist/c0.html", processor)
        );
    }

    @Override
    public void process(ResultItems page) {
        Document doc = (Document) page.getResource();
        Elements elements = doc.select(".item.item-moreshow li > a");
        for (Element item : elements) {
            // <a href="/v_olist/c_97.html" charset="703-0-0-0">电视剧</a>
            // <a href="/v_showlist/c103.html" charset="734-0-0-0">生活</a>
            String name = item.text();
            String url = item.absUrl("href");
            String code = StringHelper.parseRegex(url, "([a-zA-Z\\d_]+).html", 1);
            CategoryEntity category = new CategoryEntity().setName(name)
                    .setCode(code)
                    .setUrl(url)
                    .setSite(SiteName.Youku);
            getLogger().trace(category);
            page.addItem(category);

            page.addTargetRequest(CategoryPageProcessor2.getRequest(category));
        }
    }
}
