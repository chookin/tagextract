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
class VarietySeries {
    final static VarietySeries instance = new VarietySeries();

    /**
     * @return 如果不是综艺类视频，返回false，否则true
     */
    public boolean parse(Document doc, KindEntity video){
        Elements elements = doc.select("ul.item");
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
        parseGuests(element, episode);

        List<Map> series = (List<Map>) video.get("series");
        if (series == null) {
            series = new ArrayList<>();
            video.set("series", series);
        }
        series.add(episode);
    }

    private void parseBase(Element element, Map<String, Object> episode){
        Element eTitle = element.select("li.ititle > a").first();
        if(eTitle == null){
            return;
        }
        String url = eTitle.absUrl("href");
        String name = eTitle.text();
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

    private void parseLabel(Element element, Map<String, Object> episode){
        Element eLabel = element.select("li.ititle > label").first();
        if(eLabel != null){
            episode.put("label", eLabel.text());
        }
    }

    private void parseGuests(Element element, Map<String, Object> episode){
        Elements eGuests = element.select("li.iguests > a");
        List<Map<String,String>> guests = new ArrayList<>();
        for(Element eGuest : eGuests){
            // <a href="http://www.youku.com/star_page/uid_UNDgyNzI=.html" charset="411-2-10" target="_blank">范冰冰</a>
            // <a href="http://www.youku.com/star_page/uid_UNDg1NjA=.html" charset="411-2-10" target="_blank">张丰毅</a>
            String guestUrl = eGuest.absUrl("href");
            String guestName = eGuest.text();

            Map<String, String> guest = new HashMap<>();
            guest.put("name", guestName);
            guest.put("url", guestUrl);

            guests.add(guest);
        }
        if(!guests.isEmpty()){
            episode.put("guests", guests);
        }
    }
}
