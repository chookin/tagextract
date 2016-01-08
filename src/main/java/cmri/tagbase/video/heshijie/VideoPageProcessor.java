package cmri.tagbase.video.heshijie;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by zhuyin on 6/5/15.
 */
public class VideoPageProcessor implements PageProcessor {
    private static final VideoPageProcessor processor = new VideoPageProcessor();

    public static Request getRequest(CategoryEntity category){
        return getRequest(category, 1);
    }
    private static Request getRequest(CategoryEntity category, int pageIndex){
        String url = category.getUrl()+"&pageIdx=" +pageIndex;
        return new Request(url, processor)
                .putExtra("categoryEntity", category)
                .putExtra("pageIdx", pageIndex)
                ;
    }
    public static VideoPageProcessor getInstance(){
        return processor;
    }

    @Override
    public void process(ResultItems page) {
        CategoryEntity category = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);
        Document doc = (Document) page.getResource();

        Elements elements = doc.select("div.misel-area");
        for (Element element : elements) {
            // <a class="sel-imgarea" data-nodeid="-1" data-contid="600696055" data-copyright="0">
            // <img src="/publish/poms/image/3002/306/460/zhaxi_HSJ1080V.jpg" width="180" alt="扎西1935">
            // </a>
            Element eCode = element.select(".sel-imgarea").first();
            if(eCode == null){
                continue;
            }
            String code = eCode.attr("data-contid");
            Element eName = element.select(".details-info span").first();
            if(eName == null){
                continue;
            }
            String name = eName.text();
            String url = "http://www.lovev.com/pc/list/play.jsp?c="+ code;
            KindEntity video = new KindEntity("video").setName(name)
                    .setCode(code)
                    .setUrl(url)
                    .setCategory(category);
            getLogger().trace(video);
            page.addItem(video);
        }

        if(!page.getItems().isEmpty()) {
            int curPageIdx = (int) page.getRequest().getExtra("pageIdx");
            page.addTargetRequest(getRequest(category, curPageIdx + 1));
        }
    }
}
