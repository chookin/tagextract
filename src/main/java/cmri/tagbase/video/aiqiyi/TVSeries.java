package cmri.tagbase.video.aiqiyi;

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
    static final TVSeries instance = new TVSeries();

    public void parse(Document doc, KindEntity video){
        Elements elements = doc.select("#block-I > div > div > ul > li");
        if(elements.isEmpty()){
            return;
        }
        for(Element element: elements){
            Element eLink = element.select("a.site-piclist_pic_link").first();
            if(eLink == null){
                continue;
            }
            // <a href="http://v.youku.com/v_show/id_XODk4OTkyNzM2.html?from=y1.6-97.3.1.f519d1029c9f11e4b2ad" title="武媚娘传奇 81" charset="411-5-1" target="_blank" data-from="y1.6-97.3.1.f519d1029c9f11e4b2ad">81</a>
            String url = eLink.absUrl("href");
            String name = eLink.attr("title");
            // <p class="site-piclist_info_title"><a href="http://www.iqiyi.com/v_19rrnsll6o.html" rseat="juji_jshu_1">第1集</a></p>
            Element eLabel = element.select(".site-piclist_info_title > a").first();
            if(eLabel != null){
                name = eLabel.text() + " " + name;
            }
            updateSeries(url, name, video);
        }
    }
    private void updateSeries(String url, String name, KindEntity video){
        if(StringUtils.isEmpty(name)){
            return;
        }

        if(url.contains("?"))
            url = url.substring(0, url.indexOf("?"));

        String id = StringHelper.parseRegex(url, "/([_a-zA-Z\\d]+).html", 1);
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
