package cmri.tagbase.music;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.SiteName;
import cmri.tagbase.base.CKCollection;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.configuration.ConfigFileManager;
import cmri.utils.io.FileHelper;
import cmri.utils.lang.BaseOper;
import cmri.utils.lang.StringHelper;
import cmri.utils.lang.TimeHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

/**
 * Created by zhuyin on 1/9/16.
 */
public class QQMusicCollection extends CKCollection {
    @Override
    public String getSiteName() {
        return SiteName.QQMusic;
    }

    @Override
    public Collection<Request> getSeedCategoryRequests() {
        return TagPageProcessor.getSeedRequests();
    }

    @Override
    public Request getRequest(CategoryEntity category) {
        return MusicPageProcessor.getRequest(category);
    }

    public static void main(String[] args){
        new BaseOper() {
            @Override
            public boolean action() {
                new QQMusicCollection().init(getOptions().options())
                        .start();
                return true;
            }
        }.setArgs(args).action();
    }

    static class TagPageProcessor implements PageProcessor {
        private static final TagPageProcessor processor = new TagPageProcessor();

        public static Collection<Request> getSeedRequests(){
            return Collections.singletonList(new Request("http://y.qq.com/#type=taogelist", processor)
                    // .setDownloader(CasperJsDownloader.instance()) // no use to render the js page.
            );
        }
        @Override
        public void process(ResultItems page) {
            Document doc;
            try {
                doc = getDoc(page);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Elements elements = doc.select(".gedan_sort ul li a");
            for (Element item : elements) {
                String name = item.attr("title");
                String code = item.attr("id");
                code = StringHelper.parseRegex(code, "([\\d]+)", 1);
                CategoryEntity category = new CategoryEntity().setName(name)
                        .setCode(code)
                        .setSite(SiteName.QQMusic);
                getLogger().trace(category);
                page.addItem(category);
            }
        }

        private Document getDoc(ResultItems page) throws IOException {
            String fileName = "music.qq.tags.html";
            ConfigFileManager.dumpIfNotExists(fileName);
            return Jsoup.parse(new java.io.File(ConfigFileManager.getPath(fileName)), FileHelper.DEFAULT_ENCODING.name(), page.getRequest().getUrl());
        }
    }

    static class MusicPageProcessor implements PageProcessor {
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

    static class MusicListPageProcessor implements PageProcessor {
        private static final PageProcessor processor = new MusicListPageProcessor();

        private MusicListPageProcessor(){}

        public static Request getRequest(KindEntity musicList){
            String url = String.format("http://qzone-music.qq.com/fcg-bin/fcg_ucc_getcdinfo_byids.fcg?type=1&json=1&utf8=1&onlysong=0&jsonpCallback=jsonCallback&nosign=1&disstid=%s&g_tk=5381&loginUin=0&hostUin=0&format=jsonp&inCharset=GB2312&outCharset=utf-8&notice=0&platform=yqq&jsonpCallback=jsonCallback&needNewCode=0", musicList.getCode());
            return new Request(url, processor)
                    .putExtra("musicList", musicList)
                    .setTarget(Request.TargetResource.Json)
                    .setPriority(8)
                    ;
        }

        @Override
        public void process(ResultItems page) {
            KindEntity musicList = page.getRequest().getExtra("musicList", KindEntity.class);

            String strJson = (String) page.getResource();
            strJson = StringHelper.parseRegex(strJson, "^jsonCallback\\(([\\w\\W]+)\\)$", 1);

            JsonParser parser = new JsonParser();
            JsonObject ojson = parser.parse(strJson).getAsJsonObject();
            JsonArray list = ojson.get("cdlist").getAsJsonArray();


            for (JsonElement e : list) {
                JsonObject oItem = e.getAsJsonObject();

                Set<String> keywords = new HashSet<>();
                JsonArray oKeyWords = oItem.get("tags").getAsJsonArray();
                for (JsonElement item : oKeyWords) {
                    JsonObject oWord = item.getAsJsonObject();
                    String word = oWord.get("name").getAsString();
                    keywords.add(word);
                }

                if(!keywords.isEmpty()){
                    musicList.set("keywords", keywords);
                }
                {
                    String desc = oItem.get("desc").getAsString();
                    desc = MusicPageProcessor.getPlainText(desc);
                    musicList.set("desc", desc);
                    musicList.set("scoreNum", oItem.get("scoreusercount").getAsString());
                }

                JsonArray osonglist = oItem.get("songlist").getAsJsonArray();
                for (JsonElement itemSong : osonglist) {
                    JsonObject oSong = itemSong.getAsJsonObject();
                    String songName = oSong.get("songname").getAsString();
                    songName = MusicPageProcessor.getPlainText(songName);

                    String songid = oSong.get("songid").getAsString();
                    // String url = oSong.get("url").getAsString();

                    String singerName = oSong.get("singername").getAsString();
                    singerName = MusicPageProcessor.getPlainText(singerName);
                    String singerId = oSong.get("singerid").getAsString();

                    String album = oSong.get("diskname").getAsString();
                    album = MusicPageProcessor.getPlainText(album);
                    String albumId = oSong.get("diskid").getAsString();

                    String strSongData = oSong.get("songdata").getAsString();
                    if(StringUtils.isBlank(strSongData)){
                        continue;
                    }
                    String[] songData = strSongData.split("\\|");
                    try {
                        String mid = songData[20];
                        String url = "http://s.plcloud.music.qq.com/fcgi-bin/fcg_yqq_song_detail_info.fcg?songmid=" + mid;

                        String singerMid = songData[21];
                        Map<String, String> singer = new HashMap<>();
                        singer.put("name", singerName);
                        singer.put("id", singerId);
                        singer.put("mid", singerMid);

                        String albumMid = songData[22];
                        KindEntity music = new KindEntity("music")
                                .setName(songName)
                                .setCode(mid) // use mid as code
                                .setUrl(url)
                                .setCategory(musicList.getCategory())
                                .set("songId", songid)
                                .set("album", album)
                                .set("albumId", albumId) // http://y.qq.com/#type=album&id=9806
                                .set("albumMid", albumMid) // http://y.qq.com/#type=album&mid=000lFtVh4WintF
                                .set("singer", Arrays.asList(singer));
                        if (!keywords.isEmpty()) {
                            music.set("keywords", keywords);
                        }
                        getLogger().trace(music);
                        page.addItem(music);
                    }catch (ArrayIndexOutOfBoundsException ae){
                        throw new RuntimeException(oItem.toString(), ae);
                    }
                }
            }
            page.addItem(musicList);
        }
    }
}
