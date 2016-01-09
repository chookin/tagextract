package cmri.tagbase.music;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.SiteName;
import cmri.tagbase.base.CKCollection;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.lang.BaseOper;
import cmri.utils.lang.StringHelper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

/**
 * Created by zhuyin on 1/9/16.
 */
public class BaiduMusicCollection extends CKCollection {
    @Override
    public String getSiteName() {
        return SiteName.BaiduMusic;
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
                new BaiduMusicCollection().init(getOptions().options())
                        .start();
                return true;
            }
        }.setArgs(args).action();
    }

    static class TagPageProcessor implements PageProcessor {
        private static final TagPageProcessor processor = new TagPageProcessor();

        public static Collection<Request> getSeedRequests(){
            return Collections.singletonList(new Request("http://music.baidu.com/tag", processor)
            );
        }

        @Override
        public void process(ResultItems page) {
            Document doc = (Document) page.getResource();
            Elements elements = doc.select("a.tag-item");
            for (Element item : elements) {
                String name = item.text();
                String url = item.absUrl("href");
                CategoryEntity category = new CategoryEntity().setName(name)
                        .setUrl(url)
                        .setSite(SiteName.BaiduMusic);
                getLogger().trace(category);
                page.addItem(category);
            }
        }
    }

    static  class MusicPageProcessor implements PageProcessor {
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

    static class MusicDetailPageProcessor implements PageProcessor {
        private static final PageProcessor processor = new MusicDetailPageProcessor();

        public static Request getRequest(KindEntity music){
            return new Request(music.getUrl(), processor)
                    .setPriority(8)
                    .putExtra("music", music)
                    ;
        }

        private MusicDetailPageProcessor(){}

        @Override
        public void process(ResultItems page) {
            KindEntity music = page.getRequest().getExtra("music", KindEntity.class);
            Document doc = (Document) page.getResource();

            Element element = doc.select("span.song-play-num > span.num").first();
            if(element != null){
                String num = element.text().replace(",","");
                try {
                    music.set("playNum", Integer.valueOf(num));
                }catch (NumberFormatException e){
                    getLogger().warn("Fail to parse planNum of "+music+". "+e.getMessage());
                }
            }

            Elements elements = doc.select("a.tag-list");
            Set<String> keywords = new HashSet<>();
            for (Element item : elements) {
                String word = item.text().trim();
                keywords.add(word);
            }
            if(!keywords.isEmpty())
                music.set("keywords", keywords);

            page.addItem(music);
        }
    }
}
