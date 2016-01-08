package cmri.tagbase.music.qq;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.lang.TimeHelper;
import cmri.utils.lang.StringHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Jsoup;

import java.util.Date;

/**
 * Created by zhuyin on 4/2/15.
 */
public class MusicPageProcessor implements PageProcessor {
    private static final MusicPageProcessor processor = new MusicPageProcessor();

    public static Request getRequest(CategoryEntity category){
        return getRequest(category, 1);
    }

    static Request getRequest(CategoryEntity category, int pageNum){
        String url = String.format("http://s.plcloud.music.qq.com/fcgi-bin/fcg_get_diss_by_tag.fcg?categoryId=%s&sortId=2&sin=%d&ein=%d&format=jsonp&g_tk=1051965839&loginUin=469308668&hostUin=0&format=jsonp&inCharset=GB2312&outCharset=utf-8&notice=0&platform=yqq&jsonpCallback=MusicJsonCallback&needNewCode=0", category.getCode(), (pageNum-1)*20, pageNum * 20 -1);
        return new Request(url, processor)
                .putExtra("category", category)
                .putExtra("cookie", "pgv_pvid=7073514526; pgv_info=ssid=s3419683262")
                .putExtra("referer", "http://y.qq.com/y/static/taoge/taoge_list.html")
                .putExtra("page", pageNum)
                .setPriority(7)
                .setTarget(Request.TargetResource.Json)
                ;
    }

    @Override
    public void process(ResultItems page) {
        CategoryEntity category = page.getRequest().getExtra("category", CategoryEntity.class);
        String strJson = (String) page.getResource();
        strJson = StringHelper.parseRegex(strJson, "^MusicJsonCallback\\(([\\w\\W]+)\\)$", 1);

        JsonParser parser = new JsonParser();
        JsonObject ojson = parser.parse(strJson).getAsJsonObject();
        JsonObject data = ojson.get("data").getAsJsonObject();
        JsonArray list = data.get("list").getAsJsonArray();


        for (JsonElement e : list) {
            JsonObject oMusic = e.getAsJsonObject();
            String name = oMusic.get("dissname").getAsString();
            name = getPlainText(name);

            String strId = oMusic.get("dissid").getAsString();
            String wUrl = String.format("http://y.qq.com/#type=taoge&id=%s", strId);
            String desc = oMusic.get("introduction").getAsString();
            desc = getPlainText(desc);

            int listenNum = oMusic.get("listennum").getAsInt();
            double score = oMusic.get("score").getAsDouble() * 10;
            String strCreatetime = oMusic.get("createtime").getAsString();
            Date createTime = TimeHelper.parseDate(strCreatetime);

            JsonObject creator = oMusic.get("creator").getAsJsonObject();
            String creatorName = creator.get("name").getAsString();
            creatorName = getPlainText(creatorName);

            String creatorQQ = creator.get("qq").getAsString();

            KindEntity musicList = new KindEntity("music")
                    .setName(name)
                    .setUrl(wUrl)
                    .setCategory(category)
                    .setCode(strId)
                    .set("desc", desc)
                    .set("playNum", listenNum)
                    .set("scoreValue", score)
                    .set("creatorName", creatorName)
                    .set("creatorQQ", creatorQQ)
                    .set("createTime", createTime)
                    .set("type", "musicList")
                    ;
            getLogger().trace(musicList);
            page.addTargetRequest(MusicListPageProcessor.getRequest(musicList));
        }

        Integer curPage = page.getRequest().getExtra("page", Integer.class);
        if (1 != curPage) {// if not the first page
            return;
        }

        int sum = data.get("sum").getAsInt(); // total music list count.
        int pageTotalNum = (int) Math.ceil(sum * 1.0 / 20); // 20 music list in one page
        for(int index = 2; index <= pageTotalNum; ++index) {
            page.addTargetRequest(getRequest(category, index));
        }
    }

    /**
     * for record like this:
     * { "_id" : "music.qq-001mCZFp3T3DLv-心中有个他&#95", "name" : "心中有个他&#95;J", "site" : "music.qq", "code" : "001mCZFp3T3DLv", "url" : "http://s.plcloud.music.qq.com/fcgi-bin/fcg_yqq_song_detail_info.fcg?songmid=001mCZFp3T3DLv", "time" : ISODate("2015-04-14T02:30:41.492Z"), "category" : "心痛", "c_code" : "128", "properties" : { "album" : "对不起,&#32;我爱你", "albumId" : "003My5471eFkkD", "keywords" : [ "影视热歌", "伤感", "节日", "心痛", "动漫" ], "singer" : [ "对不起,&#32;我爱你:003mwz8V0JDh6G" ] }, "tag" : [ "音乐/心情/心痛" ] }
     I need to get plain text for singer, album, name.
     */
    public static String getPlainText(String txt){
        String rst = Jsoup.parse(txt).text();
        rst = StringHelper.convertHtmlLatin2UTF8(rst);
        return Jsoup.parse(rst).text();
    }
}
