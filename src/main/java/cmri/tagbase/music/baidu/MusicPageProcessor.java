package cmri.tagbase.music.baidu;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.lang.StringHelper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuyin on 3/31/15.
 */
public class MusicPageProcessor implements PageProcessor {
    private static final MusicPageProcessor processor = new MusicPageProcessor();

    public static Request getRequest(CategoryEntity category){
        return new Request(category.getUrl(), processor)
                .putExtra("category", category)
                ;
    }
    @Override
    public void process(ResultItems page) {
        CategoryEntity category = page.getRequest().getExtra("category", CategoryEntity.class);
        Document doc = (Document) page.getResource();

        Elements elements = doc.select("div.song-item");
        for (Element element : elements) {
            Elements subElements = element.select("a");
            Element item = subElements.first();
            if(item != null){
                String url = item.absUrl("href"); // http://music.baidu.com/song/13139680
                String name = item.attr("title");
                String code = StringHelper.parseRegex(url, "([\\d]+)", 1);

                KindEntity music = new KindEntity("music").setCategory(category)
                        .setName(name)
                        .setUrl(url)
                        .setCode(code);

                Elements singerElements = element.select(".author_list a");
                List<Map<String,String>> singers = new ArrayList<>();
                for(Element sItem: singerElements){
                    String singerName = sItem.text();
                    String singerUrl = sItem.absUrl("href");
                    String singerId = StringHelper.parseRegex(singerUrl, "id=([\\w]+)", 1);

                    Map<String, String> singer = new HashMap<>();
                    singer.put("name", singerName);
                    singer.put("id", singerId);
                    singer.put("url", singerUrl);

                    singers.add(singer);
                }
                if(!singers.isEmpty()){
                    music.set("singer", singers);
                }

                Element albumElement = element.select(".album-title a").first();
                if(albumElement != null){
                    // <a href="/album/13138380" title="Acoustic Best">《Acoustic Best》</a>
                    music.set("album", albumElement.attr("title"));
                    String albumUrl = albumElement.absUrl("href");
                    music.set("albumUrl", albumUrl);
                    String albumId = StringHelper.parseRegex(albumUrl, "/([\\d]+)$", 1);
                    music.set("albumId", albumId);
                }

                /*
                <span class="song-title" style="width: 190px;"><a href="/song/s/830684a3c708551a0d91" class="grayblack" data-songdata="{ &quot;id&quot;: &quot;&quot; }" title="When You Believe">When You Believe</a><a title="歌曲MV" target="_blank" href="/mv/8692679" class="mv-icon"></a><div class="extra-info"></div></span>

                if(subElements.size() > 1){
                    Element item2 = subElements.get(1);
                    boolean isMV = item2.attr("href").contains("mv");
                    if(isMV){
                        music.set("type", "mv");
                    }
                }*/
                if(music.getUrl().contains("/mv/")){
                    music.set("type", "mv");
                }


                getLogger().trace(music);
                if("mv".equals(music.get("type"))) {
                    page.addItem(music);
                }else{
                    page.addTargetRequest(MusicDetailPageProcessor.getRequest(music));
                }
            }
        }

        String next = getNextPageUrl(doc);
        if (next != null) {
            Request request = new Request(next, processor)
                    .setPriority(1)
                    .putExtra("category", category);
            page.addTargetRequest(request);
        }
    }
    private String getNextPageUrl(Document doc) {
        Element element = doc.select("a.page-navigator-next").first();
        if (element != null) {
            return element.absUrl("href");
        }
        return null;
    }
}
