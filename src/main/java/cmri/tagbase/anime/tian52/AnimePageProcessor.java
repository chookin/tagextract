package cmri.tagbase.anime.tian52;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.lang.StringHelper;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by zhuyin on 6/8/15.
 */
public class AnimePageProcessor implements PageProcessor {
    private static final AnimePageProcessor processor = new AnimePageProcessor();

    public static Request getRequest(CategoryEntity category){
        return getRequest(category, 1);
    }

    private static Request getRequest(CategoryEntity category, int pageIndex){
        // http://www.52tian.net/---1--------/
        // http://www.52tian.net/---1--------72/
        String url;
        url = category.getUrl();
        if(pageIndex != 1) {
            url = StringUtils.removeEnd(url, "/");
            url = url + pageIndex+"/";
        }
        return new Request(url, processor)
                .putExtra("categoryEntity", category)
                .putExtra("pageIdx", pageIndex)
                ;
    }

    @Override
    public void process(ResultItems page) {
        CategoryEntity category = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);
        Document doc = (Document) page.getResource();

        Elements elements = doc.select(" div.imagelist2 > ul > li > a");
        for (Element element : elements) {
            // <a href="/html/movie/371/" title="天空之艾斯嘉科尼"><img alt="天空之艾斯嘉科尼" width="120" height="168" src="http://cdn3.52tian.net/dh/371.jpg"><em></em><span>天空之艾斯嘉科尼</span></a>
            String url = element.absUrl("href");
            String name = element.text();
            String code = StringHelper.parseRegex(url, "/([\\d]+)/", 1);
            KindEntity video = new KindEntity("anime").setName(name)
                    .setUrl(url)
                    .setCode(code)
                    .setCategory(category);

            getLogger().trace(video);
            page.addTargetRequest(AnimeDetailPageProcessor.getRequest(video));
        }
    }
}
