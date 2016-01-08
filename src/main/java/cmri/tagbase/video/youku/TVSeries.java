package cmri.tagbase.video.youku;

import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.lang.StringHelper;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuyin on 5/22/15.
 */
class TVSeries {
    public static TVSeries getInstance(){
        return new TVSeries();
    }
    public void parse(Document doc, KindEntity video){
        Elements elements = doc.select("li > a");
        for(Element element: elements){
            updateSeries(element, video);
        }
    }
    private void updateSeries(Element element, KindEntity video){
        Map<String, Object> episode = new HashMap<>();
        parseBase(element, episode);
        if(episode.isEmpty()){
            return;
        }

        List<Map> series = (List<Map>) video.get("series");
        if (series == null) {
            series = new ArrayList<>();
            video.set("series", series);
        }
        series.add(episode);
    }

    private void parseBase(Element element, Map<String, Object> episode){
        String url = element.absUrl("href");
        String name = element.text();
        if(StringUtils.isEmpty(name)){
            return;
        }
        // sometimes, the url is "", such as the html content is
        //   <a charset="419-5-6" href="javascript:;" onclick="y.episode.showContent(this)">更多剧集</a>
        if(StringUtils.isEmpty(url)){
            return;
        }
        if(url.contains("?"))
            url = url.substring(0, url.indexOf("?"));

        String id = StringHelper.parseRegex(url, "id_([a-zA-Z\\d]+).html", 1);
        episode.put("name", name);
        episode.put("id", id);
        episode.put("url", url);
    }
}
