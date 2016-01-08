package cmri.tagbase.video.aiqiyi;

import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.lang.StringHelper;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

/**
 * Created by zhuyin on 5/22/15.
 */
class VarietySeries {
    static final VarietySeries instance = new VarietySeries();
    /**
     * @return 如果不是综艺类视频，返回false，否则true
     */
    public boolean parse(Document doc, KindEntity video){
        Elements elements = doc.select("#albumpic-showall-wrap > li");
        if(elements.isEmpty()){
            return false;
        }
        for(Element element: elements){
            updateSeries(element, video);
        }
        return true;
    }

    private void updateSeries(Element element, KindEntity video){
        Map<String, Object> episode = new HashMap<>();
        parseBase(element, episode);
        if(episode.isEmpty()){
            return;
        }
        parseLabel(element, episode);
        parsePlayCount(element, episode);

        List<Map> series = (List<Map>) video.get("series");
        if (series == null) {
            series = new ArrayList<>();
            video.set("series", series);
        }
        series.add(episode);
    }

    private void parseBase(Element element, Map<String, Object> episode){
        // <a href="http://www.iqiyi.com/v_19rrnqipgk.html">非诚勿扰之X-girl首露真容 男嘉宾为爱割胃</a>
        Element eTitle = element.select("div.graphics-type-rt.pr.fl h3 > a").first();
        if(eTitle == null){
            return;
        }
        String url = eTitle.absUrl("href");
        String name = eTitle.text();
        if(StringUtils.isEmpty(name)){
            return;
        }
        if(StringUtils.isEmpty(url)){
            return;
        }
        if(url.contains("?"))
            url = url.substring(0, url.indexOf("?"));

        String id = StringHelper.parseRegex(url, "([a-zA-Z\\d=_]+).html", 1);
        episode.put("name", name);
        episode.put("id", id);
        episode.put("url", url);
    }

    private void parseLabel(Element element, Map<String, Object> episode){
        // <span class="mod-listTitle_right">2015-03-21期</span>
        Element eTarget = element.select(".mod-listTitle_right").first();
        if(eTarget == null){
            return;
        }
        String label = eTarget.text();
        episode.put("label", label);
    }
    private void parsePlayCount(Element element, Map<String, Object> episode){
        // <p class="playcount"> <i class="site-icons icon-play-bgGray"></i> <span title="播放" rseat="xj_pc0" data-pc-albumid="332104400" data-widget-counter="player">336万</span> </p>
        Element eTarget = element.select(".playCount").first();
        if(eTarget == null){
            return;
        }
        String str = eTarget.text();
        String strNum = StringHelper.parseRegex(str, "(\\d*[\\.\\d+]?)万", 1);
        if(strNum != null){
            episode.put("playNum", (int)(Double.valueOf(strNum)*10000));
        }
    }
}
