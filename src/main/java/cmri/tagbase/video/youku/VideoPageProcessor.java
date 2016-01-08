package cmri.tagbase.video.youku;

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
 * Created by zhuyin on 4/8/15.
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

        Elements elements = doc.select("div > div.p-link > a");
        if(elements.isEmpty()){
            elements = doc.select("div > div.v-link > a");
        }
        for(Element element: elements){
            String url = element.absUrl("href");
            String title = element.attr("title");
            // http://v.youku.com/v_show/id_XNjcxOTIwMDg=.html
            String id = StringHelper.parseRegex(url, "([a-zA-Z\\d=]+).html", 1);
            KindEntity video = new KindEntity("video").setCategory(category)
                    .setName(title)
                    .setUrl(url)
                    .setCode(id);
            getLogger().trace(video);
            page.addTargetRequest(VideoDetailPageProcessor.getRequest(video));
        }
        addNextPageRequest(page, doc, category);
        processEmptyPage(page);
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

    private String getNextPageUrl(Document doc) {
        Element element = doc.select("#listofficial > div.yk-pager > ul > li.next > a").first();
        if(element == null){
            return null;
        }
        return element.absUrl("href");
    }

    /**
     * 体育CBA页面，默认是“今日”排序，有时页面中的视频数非常少，只有选择“历史”排序，页面中的视频数才会多
     *
     排序：最多播放，今日
     与
     排序：最多播放，历史
     有的“今日”数目为0，即当“今日”排序时，页面是空的，而“历史”的非空
     http://www.youku.com/v_showlist/c98d1s1g2114.html
     http://www.youku.com/v_showlist/c98g2114d4s1.html

     http://www.youku.com/v_showlist/c174.html
     http://www.youku.com/v_showlist/c174d4s1.html
     http://www.youku.com/v_showlist/c171.html
     http://www.youku.com/v_showlist/c171d4s1.html
     */
    private void processEmptyPage(ResultItems page){
        if(page.getTargetRequests().size() > 10) {
            return;
        }
        String url = page.getRequest().getUrl();
        if(url.contains("d4s1")){
            return;
        }

        CategoryEntity category = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);
        url = url.replace("d1s1", "").replace(".html", "d4s1.html");
        Request request = new Request(url, processor)
                .putExtra("categoryEntity", category)
                ;
        page.addTargetRequest(request);
        getLogger().info("get 0 video of " + page.getRequest() + ". Retry "+ url);
    }
}
