package cmri.tagbase.music.qq;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.lang.StringHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by zhuyin on 4/2/15.
 */
public class MusicListPageProcessor implements PageProcessor {
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
