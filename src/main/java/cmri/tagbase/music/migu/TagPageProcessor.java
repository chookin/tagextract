package cmri.tagbase.music.migu;

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
 * Created by zhuyin on 4/3/15.
 */
public class TagPageProcessor implements PageProcessor {
    private static final TagPageProcessor processor = new TagPageProcessor();

    public static Collection<Request> getSeedRequests(){
        return Collections.singletonList(new Request("http://music.migu.cn/tag", processor)
                        .putExtra("header", "Referer=http://music.migu.cn/")
                        .putExtra("cookie", "uid=ChmIkFUaT/hTMU7gBprMAg==; MIGU_SESSION=9208F96BC47B4C1092DD4183161E01D3; _ada_kl=260918610; _ada_ki=14C7E7DF69408B85001BE000400; _ada_kt=20150331154331; _ada_kp=14C6EC908FC0E4AA50B09500E80; _ada_ki30=14C7E7DF69408B85001BE000400; _ada_lvt=1428053441508; _ada_refer=http://music.migu.cn/tag")
        );
    }

    @Override
    public void process(ResultItems page) {
        Document doc = (Document) page.getResource();
        Elements elements = doc.select(".sort_t > a");
        for (Element item : elements) {
            // http://music.migu.cn/#/tag/1000587690/P2Z1Y1L2N3/17/001002A
            String name = item.attr("title");
            String url = item.absUrl("href");
            url = url.replace("/#/", "/");
            String code = StringHelper.parseRegex(url, "tag/([\\d]+)/", 1);
            CategoryEntity category = new CategoryEntity().setName(name)
                    .setCode(code)
                    .setUrl(url)
                    .setSite(SiteName.MiguMusic);
            getLogger().trace(category);
            page.addItem(category);
        }
    }
}
