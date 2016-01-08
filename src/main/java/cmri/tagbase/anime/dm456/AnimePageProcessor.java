package cmri.tagbase.anime.dm456;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
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
        String url = String.format("%sindex_%d.html", category.getUrl(), pageIndex);
        return new Request(url, processor)
                .putExtra("categoryEntity", category)
                .putExtra("pageIdx", pageIndex)
                ;
    }

    @Override
    public void process(ResultItems page) {
        CategoryEntity category = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);
        Document doc = (Document) page.getResource();

        Elements elements = doc.select("#dmList > ul > li");
        for (Element element : elements) {
            Element eUrl = element.select("dl > dt > a").first();
            if(eUrl == null){
                continue;
            }
            String url = eUrl.absUrl("href");
            String name = eUrl.text();
            KindEntity video = new KindEntity("anime").setName(name)
                    .setUrl(url)
                    .setCategory(category);
            getLogger().trace(video);
            page.addTargetRequest(AnimeDetailPageProcessor.getRequest(video));
        }

        int pageIdx = page.getRequest().getExtra("pageIdx", Integer.class);
        if(pageIdx == 1) {
            int total = getTotalPageNum(doc);
            for (int i = 2; i <= total; ++i) {
                page.addTargetRequest(getRequest(category, i));
            }
        }
    }

    private int getTotalPageNum(Document doc){
        Element element = doc.select("#pager > span.total > strong").first();
        if(element == null){
            throw new IllegalArgumentException("fail to parse total page num");
        }
        String str = element.text();
        return Integer.valueOf(str);
    }
}
