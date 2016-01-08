package cmri.tagbase.anime.hedongman;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.SiteName;
import cmri.tagbase.orm.domain.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by zhuyin on 6/9/15.
 */
public class TagPageProcessor implements PageProcessor {
    private static final TagPageProcessor processor = new TagPageProcessor();

    public static Collection<Request> getSeedRequests(){
        return Collections.singletonList(new Request("http://dm.10086.cn/w/p/dh_list.jsp?tc=14&vt=4&f=3188&pg=524", processor)
        );
    }
    @Override
    public void process(ResultItems page) {
        Document doc = (Document) page.getResource();
        Elements elements = doc.select("div.mo_sel a");

        for (Element item : elements) {
            String url = item.absUrl("href");
            if(StringUtils.isEmpty(url)){
                continue;
            }
            String name = item.text();
            CategoryEntity category = new CategoryEntity().setName(name)
                    .setUrl(url)
                    .setSite(SiteName.Hedongman);
            getLogger().trace(category);
            page.addItem(category);
        }
    }
}