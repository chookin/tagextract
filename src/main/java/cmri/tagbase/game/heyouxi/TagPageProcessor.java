package cmri.tagbase.game.heyouxi;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.SiteName;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.utils.lang.StringHelper;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by zhuyin on 6/10/15.
 */
public class TagPageProcessor implements PageProcessor {
    private static final TagPageProcessor processor = new TagPageProcessor();

    public static Collection<Request> getSeedRequests(){
        return Collections.singletonList(new Request("http://g.10086.cn/a/?dotype=djclass", processor)
        );
    }
    @Override
    public void process(ResultItems page) {
        Document doc = (Document) page.getResource();
        Elements elements = doc.select("ul.bpflclassBody li a");
        /**
         <a href="/a/list.php?dotype=3adj&amp;classid=392&amp;dType=2&amp;spm=a.pdindex.djclass.djgameclass.2">
         <span><img src="/skin/html4/androidMetro/images/bpcl.jpg" width="60" height="60"></span>
         <p class="C043077">策略塔防</p>
         <p><span>        1108</span>款</p>
         </a>
         */
        for (Element item : elements) {
            String name = item.select("p.C043077").text();
            if(StringUtils.isEmpty(name)){
                continue;
            }
            String url = item.absUrl("href");
            if(StringUtils.isEmpty(url)){
                continue;
            }
            String code = StringHelper.parseRegex(url, "dType=([\\d]+)", 1);
            CategoryEntity category = new CategoryEntity().setName(name)
                    .setUrl(url)
                    .setCode(code)
                    .setSite(SiteName.Heyouxi);
            getLogger().trace(category);
            page.addItem(category);
        }
    }
}
