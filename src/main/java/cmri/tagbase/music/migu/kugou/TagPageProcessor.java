package cmri.tagbase.music.migu.kugou;

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
public class TagPageProcessor implements PageProcessor {
    private static final TagPageProcessor processor = new TagPageProcessor();

    public static Collection<Request> getSeedRequests(){
        return Collections.singletonList(new Request("http://www.kugou.com/yy/html/category.html", processor)
        );
    }

    @Override
    public void process(ResultItems page) {
        Document doc = (Document) page.getResource();
        Elements elements = doc.select("div.all_type > div > a");
        for (Element item : elements) {
            // <a href="http://www.kugou.com/yy/category/song/1-11.html" title="中国风">中国风</a>
            String name = item.attr("title");
            String url = item.absUrl("href");
            String code = StringHelper.parseRegex(url, "1-([\\d]+)", 1);
            CategoryEntity category = new CategoryEntity().setName(name)
                    .setCode(code)
                    .setUrl(url)
                    .setSite(SiteName.KugouMusic);
            getLogger().trace(category);
            page.addItem(category);
        }
    }
}
