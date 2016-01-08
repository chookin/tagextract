package cmri.tagbase.anime.tian52;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.lang.StringHelper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

/**
 * Created by zhuyin on 6/8/15.
 */
public class AnimeDetailPageProcessor implements PageProcessor {
    private static AnimeDetailPageProcessor processor = new AnimeDetailPageProcessor();

    public static AnimeDetailPageProcessor getInstance(){return processor;}

    public static Request getRequest(KindEntity video){
        return new Request(video.getUrl(), processor)
                .setPriority(9)
                .putExtra("videoEntity", video)
                ;
    }

    @Override
    public void process(ResultItems page) {
        KindEntity video = page.getRequest().getExtra("videoEntity", KindEntity.class);
        Document doc = (Document) page.getResource();

        Elements elements = doc.select(".canopen a");
        for(Element element: elements) {
            // <a href="8636.htm">第26集</a>
            String url = element.absUrl("href");
            String name = element.text();
            String code = StringHelper.parseRegex(url, "([\\d]+).htm", 1);
            updateSeries(url, name, code, video);
        }
        getLogger().trace(video);
        page.addItem(video);
    }

    private void updateSeries(String url, String name, String id, KindEntity video){
        Map<String, String> episode = new HashMap<>();
        episode.put("name", name);
        episode.put("id", id);
        episode.put("url", url);
        List<Map> series = (List<Map>) video.get("series");
        if (series == null) {
            series = new ArrayList<>();
            video.set("series", series);
        }
        series.add(episode);
    }
}
