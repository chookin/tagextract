package cmri.tagbase.video.aiqiyi;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.lang.StringHelper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by zhuyin on 5/13/15.
 */
public class VideoPageProcessor implements PageProcessor {
    private static final VideoPageProcessor processor = new VideoPageProcessor();

    public static Request getRequest(CategoryEntity category){
        return new Request(category.getUrl(), processor)
                .putExtra("categoryEntity", category)
                ;
    }

    public static VideoPageProcessor getInstance(){
        return processor;
    }

    @Override
    public void process(ResultItems page) {
        CategoryEntity category = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);
        Document doc = (Document) page.getResource();

        Elements elements = doc.select("div.wrapper-piclist ul li div.site-piclist_pic a");
        for(Element element: elements){
            String url = element.absUrl("href");
            // http://www.iqiyi.com/a_19rrhawj7t.html#vfrm=2-4-0-1
            url = url.substring(0, url.lastIndexOf("#"));
            String title = element.attr("title");
            String id = StringHelper.parseRegex(url, "([a-zA-Z\\d]+).html", 1);
            KindEntity video = new KindEntity("video").setCategory(category)
                    .setName(title)
                    .setUrl(url)
                    .setCode(id);
            getLogger().trace(video);
            page.addTargetRequest(VideoDetailPageProcessor.getRequest(video));
        }
        addNextPageRequest(page, doc, category);
    }

    private void addNextPageRequest(ResultItems page, Document doc, CategoryEntity category){
        String url = getNextPageUrl(doc);
        if(url == null){
            return;
        }
        page.addTargetRequest(new Request(url, processor)
                .putExtra("categoryEntity", category)
                .setPriority(8)
        );
    }

    private String getNextPageUrl(Document doc){
        // <a data-key="down" class="a1" title="跳转至4页" href="/www/2/15-----------2015--11-4-1-iqiyi--.html">下一页</a>
        Element element = doc.select("body > div.mod-page > a").last();
        if(element == null){
            return null;
        }
        if(element.text().contains("下一页")) {
            return element.absUrl("href");
        }
        return null;
    }
}
