package cmri.tagbase.anime.dm456;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.lang.StringHelper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by zhuyin on 6/8/15.
 */
public class AnimeDetailPageProcessor implements PageProcessor {
    private static AnimeDetailPageProcessor processor = new AnimeDetailPageProcessor();

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

        Elements elements = doc.select("#intro_l > div.info");
        for(Element element: elements){
            Element em = element.select("em").first();
            if(em == null){
                continue;
            }
            if(!em.text().contains("剧情")){
                continue;
            }
            Elements eKeywords = elements.select("a");
            Set<String> keywords = new HashSet<>();
            keywords.add(video.getCategory().getName());
            keywords.addAll(eKeywords.stream().map(Element::text).collect(Collectors.toList()));
            video.set("keywords", keywords);
        }

        Element eIntr = doc.select("#intro1").first();
        if(eIntr != null){
            video.set("desc", eIntr.text());
        }

        Elements eSeries = doc.select(".plist.pnormal ul li a");
        for(Element eSerie : eSeries){
            // <a href="/donghua/9032/507752.html" title="96版20集" target="_blank">96版20集<img src="/n.gif"></a>
            String name = eSerie.attr("title");
            String url = eSerie.absUrl("href");
            String id = StringHelper.parseRegex(url, "([\\d]+/[\\d]+).html", 1);
            updateSeries(url, name, id, video);
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
