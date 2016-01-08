package cmri.tagbase.video.youku;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.downloader.JsoupDownloader;
import cmri.etl.pipeline.FilePipeline;
import cmri.etl.processor.PageProcessor;
import cmri.etl.spider.SpiderAdapter;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.lang.TimeHelper;
import cmri.utils.lang.StringHelper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

/**
 * Created by zhuyin on 5/13/15.
 */
class VideoDetailPageProcessor implements PageProcessor {
    private static VideoDetailPageProcessor processor = new VideoDetailPageProcessor();

    public static VideoDetailPageProcessor getInstance(){return processor;}

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

        parseBaseInfo(doc, video);
        parseBaseData(doc, video);
        parseSeries(doc, video);

        getLogger().trace(video);
        page.addItem(video);
    }

    /**
     评分:9.3
     别名: Frozen	/	白雪皇后
     上映:2014-02-05优酷上映:2014-04-29
     语言: 英语 / 国语 / 粤语
     地区: 美国类型: 动画 / 歌舞 / 奇幻 / 冒险 / 喜剧
     导演: 詹妮弗·李 / 克里斯·巴克 主演: 伊迪娜·门泽尔 / 克里斯汀·贝尔
     */
    private void parseBaseInfo(Document doc, KindEntity video){
        Element baseInfo = doc.select(".baseinfo").first();
        if(baseInfo == null){
            return;
        }
        Element eRating = baseInfo.select(".ratingstar rating > em.num").first();
        if(eRating != null){
            String strRating = eRating.text();
            double rating = Double.valueOf(strRating)*10;
            video.set("scoreValue", rating);
        }

        Element eAlias = baseInfo.select("li.alias").first();
        if(eAlias != null){
            String strAlias = eAlias.text().replace("别名:","").trim();
            video.set("alias", strAlias);
        }

        Element ePub = baseInfo.select(".pub").first();
        if(ePub != null){
            String strDate = ePub.text();
            strDate = StringHelper.parseRegex(strDate, "(\\d{4}-\\d{2}-\\d{2})", 1);
            Date screenDate = TimeHelper.parseDate(strDate);
            video.set("screenTime", screenDate);
        }

        Elements eTypes = baseInfo.select(".type > a");
        Set<String> keywords = new HashSet<>();
        for(Element eType: eTypes){
            String name = eType.text();
            keywords.add(name);
        }
        keywords.add(video.getCategory().getName());
        video.set("keywords", keywords);

        Elements eDirectors = baseInfo.select(".director > a");
        List<Map<String,String>> directors = new ArrayList<>();
        for(Element eDirector: eDirectors){
            String url = eDirector.absUrl("href");
            String name = eDirector.text();

            Map<String, String> director = new HashMap<>();
            director.put("name", name);
            director.put("url", url);

            directors.add(director);
        }
        if(!directors.isEmpty()){
            video.set("director", directors);
        }

        Elements eActors = baseInfo.select(".actor > a");
        List<Map<String,String>> actors = new ArrayList<>();
        for(Element eActor : eActors){
            // <a href="http://www.youku.com/star_page/uid_UNDgyNzI=.html" charset="411-2-10" target="_blank">范冰冰</a>
            // <a href="http://www.youku.com/star_page/uid_UNDg1NjA=.html" charset="411-2-10" target="_blank">张丰毅</a>
            String url = eActor.absUrl("href");
            String name = eActor.text();

            Map<String, String> actor = new HashMap<>();
            actor.put("name", name);
            actor.put("url", url);

            actors.add(actor);
        }
        if(!actors.isEmpty()){
            video.set("star", actors);
        }
    }

    /**
     总播放:47,800,116 评论:11,253 / 收藏:20,035
     今日新增播放:82,269 时长:102分钟
     指数: 54,032
     */
    private void parseBaseData(Document doc, KindEntity video){
        Element eBaseData = doc.select("ul.basedata").first();
        if(eBaseData == null){
            return;
        }

        Element ePlay = eBaseData.select(".play").first();
        if(ePlay != null){
            String strPlay = ePlay.text();
            strPlay = strPlay.replace(",", "");
            strPlay = StringHelper.parseRegex(strPlay, "([\\d]+)", 1);

            int play = Integer.valueOf(strPlay);
            video.set("playNum", play); // 播放次数
        }

        Elements eComment = eBaseData.select(".comment .num");
        if(eComment.size() == 2){
            String strScoreNum = eComment.get(0).text().replace(",","");
            int scoreNum = Integer.valueOf(strScoreNum);

            String strFavNum = eComment.get(1).text().replace(",", "");
            int favNum = Integer.valueOf(strFavNum);

            video.set("scoreNum", scoreNum);
            video.set("favorNum", favNum);
        }

        Element eDuration = eBaseData.select("duration").first();
        if(eDuration != null){
            String strDuration = eDuration.text();
            strDuration = StringHelper.parseRegex(strDuration, "([\\d]+)", 1);
            double duration = Double.valueOf(strDuration);
            video.set("duration", duration);
        }
    }

    /**
     * 对于综艺类、电视剧类视频页面，一个视频可能包含多个子视频，而这些子视频是通过点击一些链接才能显示其视频链家的, the link content is like
     <li data="reload_201411" class="">
     <a href="javascript:;" charset="419-3-1" onclick="y.episode.show('reload_201411')">2014年11月</a>
     </li>

     http://www.youku.com/show_page/id_z98a3f0c4b3ed11e3b8b7.html
     contains:
     http://www.youku.com/show_episode/id_z98a3f0c4b3ed11e3b8b7.html?dt=json&divid=reload_201411&__rt=1&__ro=reload_201411
     =>
     http://www.youku.com/show_episode/id_z98a3f0c4b3ed11e3b8b7.html?divid=reload_201411
     */
    private void parseSeries(Document doc, KindEntity video){
        Elements elements = doc.select("#zySeriesTab > li");
        if(elements.isEmpty()){
            return;
        }
        for(Element element: elements){
            String data = element.attr("data");
            String url = String.format("http://www.youku.com/show_episode/id_%s.html?divid=%s", video.getCode(), data);
            parsePartSeries(url, video);
        }
    }

    private void parsePartSeries(String url, KindEntity video){
        try {
            Request request = new Request().setUrl(url);
            ResultItems resultItems = JsoupDownloader.getInstance().download(request, new SpiderAdapter());
            if(!resultItems.isCacheUsed())
                new FilePipeline().process(resultItems);

            Document doc = (Document) resultItems.getResource();
            if(!VarietySeries.instance.parse(doc, video)){ // if not variety, then TV
                TVSeries.getInstance().parse(doc, video);
            }
        } catch (IOException e) {
            getLogger().error(null, e);
        }
    }
}
