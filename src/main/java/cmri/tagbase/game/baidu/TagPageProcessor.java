package cmri.tagbase.game.baidu;

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
 * Created by zhuyin on 6/9/15.
 */
public class TagPageProcessor implements PageProcessor {
    private static final TagPageProcessor processor = new TagPageProcessor();

    public static Collection<Request> getSeedRequests(){
        return Collections.singletonList(new Request("http://iwan.baidu.com/mobilegame", processor)
        );
    }
    @Override
    public void process(ResultItems page) {
        Document doc = (Document) page.getResource();
        Elements elements = doc.select("#j-filter-android > li");

        for (Element item : elements) {
            // <li data-id="2" class="item mp-btn mp-btn-default OP_LOG_LINK" data-click="{act:'b_filter'}" data-rsv="{clickarea_id:'shouyou_filter_2'}">休闲益智</li>
            String name = item.text();
            if(name.contains("全部")){
                continue;
            }
            String code = item.attr("data-id");
            CategoryEntity category = new CategoryEntity().setName(name)
                    .setCode(code)
                    .setSite(SiteName.BaiduMobileGame);
            getLogger().trace(category);
            page.addItem(category);
        }
    }
}