package cmri.tagbase.video.aiqiyi;

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
 * Created by zhuyin on 5/13/15.
 */
class VideoDetailPageProcessor implements PageProcessor {
    static final VideoDetailPageProcessor instance = new VideoDetailPageProcessor();

    public static Request getRequest(KindEntity video){
        return new Request(video.getUrl(), instance)
                .setPriority(9)
                .putExtra("videoEntity", video)
                ;
    }

    @Override
    public void process(ResultItems page) {
        KindEntity video = page.getRequest().getExtra("videoEntity", KindEntity.class);
        Document doc = (Document) page.getResource();

        parseBaseInfo(doc, video);
        parseScoreValue(doc, video);
        parseMoreInfo(doc, video);
        if(!VarietySeries.instance.parse(doc, video)){
            TVSeries.instance.parse(doc, video);
        }

        getLogger().trace(video);
        page.addItem(video);
    }

    /**
     http://www.iqiyi.com/a_19rrhaywod.html
     地区： 内地
     语言： 国语
     类型： 偶像剧 / 言情剧 / 家庭剧 / 青春剧 / 喜剧
     主演： 赵薇 / 佟大为 / 纪姿含 / 董洁 / 潘虹 / 韩童生 / 郭凯敏 / 崔新琴 / 韩青 / 王森 / 蓝盈莹 / 郭晓婷
     导演： 姚晓峰
     */
    private void parseBaseInfo(Document doc, KindEntity video){
        Element element = doc.select("div.album-msg div.msg-bd div.msg-hd-lt").first();
        if(element == null){
            processBaseInfo2(doc, video);
            return;
        }
        Elements elements = element.select("p");
        for(Element item: elements){
            String text = item.text();
            if(text.contains("地区")){
                Element target = item.select("a").first();
                if(target != null){
                    video.set("region", target.text());
                }
            }else if(text.contains("类型")){
                Set<String> keywords = new HashSet<>();
                Elements targets = item.select("a");
                for(Element target: targets){
                    keywords.add(target.text());
                }
                keywords.add(video.getCategory().getName());
                video.set("keywords", keywords);
            }else if(text.contains("主演")){
                List<Map<String,String>> actors = new ArrayList<>();
                Elements targets = item.select("a");
                // <a href="http://www.iqiyi.com/lib/s_200028805.html" target="_blank" rseat="jj-zjxx-text-0923">赵薇</a>
                for(Element target: targets) {
                    String name = target.text();
                    String url = target.absUrl("href");
                    String id = StringHelper.parseRegex(url, "([\\d]+).html", 1);
                    Map<String, String> actor = new HashMap<>();
                    actor.put("name", name);
                    actor.put("url", url);
                    actor.put("id", id);

                    actors.add(actor);
                }
                if(actors.size() > 0){
                    video.set("star", actors);
                }
            }else if(text.contains("导演")){
                List<Map<String,String>> directors = new ArrayList<>();
                Elements targets = item.select("a");
                // <a href="http://www.iqiyi.com/lib/s_202939305.html" target="_blank" rseat="jj-zjxx-text-0923">姚晓峰</a>
                for(Element target: targets) {
                    String name = target.text();
                    String url = target.absUrl("href");
                    String id = StringHelper.parseRegex(url, "([\\d]+).html", 1);
                    Map<String, String> director = new HashMap<>();
                    director.put("name", name);
                    director.put("url", url);
                    director.put("id", id);

                    directors.add(director);
                }
                if(directors.size() > 0){
                    video.set("director", directors);
                }
            }
        }
    }

    /**
     * For old TVs
     主演： 范冰冰 / 张丰毅 / 李治廷 / 张馨予 / 张钧甯 / 李晨 / 张庭 / 周海媚 / 王惠春 / 李李仁 / 李解 / 张彤 / 米露 / 孙佳奇 / 张定涵 / 施诗
     导演： 高翊浚 地区： 内地
     类型： 古装剧 / 历史剧 / 言情剧 / 宫廷剧 语言： 国语
     集数： 86集全
     总播放量：2071万次
     简介：贞观十一年（公元637年），武如意选入大唐后宫。时逢先皇后长孙皇后忌日，
     */
    private void processBaseInfo2(Document doc, KindEntity video){
        Elements elements = doc.select("#block-D > div > div > div.result_detail > div.result_detail-minH em");
        if(elements.isEmpty()){
            return;
        }
        for(Element item: elements){
            String text = item.text();
            if(text.contains("地区")){
                Element target = item.select("a").first();
                if(target != null){
                    video.set("region", target.text());
                }
            }else if(text.contains("类型")){
                Set<String> keywords = new HashSet<>();
                Elements targets = item.select("a");
                for(Element target: targets){
                    keywords.add(target.text());
                }
                keywords.add(video.getCategory().getName());
                video.set("keywords", keywords);
            }else if(text.contains("主演")){
                List<Map<String,String>> actors = new ArrayList<>();
                Elements targets = item.select("a");
                // <a href="http://www.iqiyi.com/lib/s_200028805.html" target="_blank" rseat="jj-zjxx-text-0923">赵薇</a>
                for(Element target: targets) {
                    String name = target.text();
                    String url = target.absUrl("href");
                    String id = StringHelper.parseRegex(url, "([\\d]+).html", 1);
                    Map<String, String> actor = new HashMap<>();
                    actor.put("name", name);
                    actor.put("url", url);
                    actor.put("id", id);

                    actors.add(actor);
                }
                if(actors.size() > 0){
                    video.set("star", actors);
                }
            }else if(text.contains("导演")){
                List<Map<String,String>> directors = new ArrayList<>();
                Elements targets = item.select("a");
                // <a href="http://www.iqiyi.com/lib/s_202939305.html" target="_blank" rseat="jj-zjxx-text-0923">姚晓峰</a>
                for(Element target: targets) {
                    String name = target.text();
                    String url = target.absUrl("href");
                    String id = StringHelper.parseRegex(url, "([\\d]+).html", 1);
                    Map<String, String> director = new HashMap<>();
                    director.put("name", name);
                    director.put("url", url);
                    director.put("id", id);

                    directors.add(director);
                }
                if(directors.size() > 0){
                    video.set("director", directors);
                }
            }
        }
    }
    private void parseScoreValue(Document doc, KindEntity video){
        Element element = doc.select("span.fenshu-r").first();
        if(element == null){
            parseScoreValue2(doc, video);
            return;
        }
        String score = element.text();
        video.set("scoreValue", Double.parseDouble(score)*10);
    }

    private void parseScoreValue2(Document doc, KindEntity video){
        Element element = doc.select(".score_font").first();
        if(element == null){
            return;
        }
        String str = element.text();
        if(str.endsWith("分")){
            str = str.replace(" ", "").replace("分","");
            int score = (int) (Double.parseDouble(str)* 10);
            video.set("playNum", score);
        }
    }
    private void parseMoreInfo(Document doc, KindEntity video){
        Element element = doc.select("div.album-msg span.bigPic-b-jtxt").first();
        if(element == null){
            return;
        }
        String text = element.text();
        video.set("desc", text);
    }
}
