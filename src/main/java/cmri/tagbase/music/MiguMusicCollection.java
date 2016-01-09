package cmri.tagbase.music;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.downloader.CasperJsDownloader;
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
public class MiguMusicCollection extends CKCollection {
    @Override
    public String getSiteName() {
        return SiteName.MiguMusic;
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
                new MiguMusicCollection().init(getOptions().options())
                        .start();
                return true;
            }
        }.setArgs(args).action();
    }

    static class TagPageProcessor implements PageProcessor {
        private static final TagPageProcessor processor = new TagPageProcessor();

        public static Collection<Request> getSeedRequests(){
            return Collections.singletonList(new Request("http://music.migu.cn/tag", processor)
                            .putExtra("header", "Referer=http://music.migu.cn/")
                            .putExtra("cookie", "uid=ChmIkFUaT/hTMU7gBprMAg==; MIGU_SESSION=9208F96BC47B4C1092DD4183161E01D3; _ada_kl=260918610; _ada_ki=14C7E7DF69408B85001BE000400; _ada_kt=20150331154331; _ada_kp=14C6EC908FC0E4AA50B09500E80; _ada_ki30=14C7E7DF69408B85001BE000400; _ada_lvt=1428053441508; _ada_refer=http://music.migu.cn/tag")
            );
        }

        @Override
        public void process(ResultItems page) {
            Document doc = (Document) page.getResource();
            Elements elements = doc.select(".sort_t > a");
            for (Element item : elements) {
                // http://music.migu.cn/#/tag/1000587690/P2Z1Y1L2N3/17/001002A
                String name = item.attr("title");
                String url = item.absUrl("href");
                url = url.replace("/#/", "/");
                String code = StringHelper.parseRegex(url, "tag/([\\d]+)/", 1);
                CategoryEntity category = new CategoryEntity().setName(name)
                        .setCode(code)
                        .setUrl(url)
                        .setSite(SiteName.MiguMusic);
                getLogger().trace(category);
                page.addItem(category);
            }
        }
    }

    static class MusicPageProcessor implements PageProcessor{
        private static final MusicPageProcessor processor = new MusicPageProcessor();

        public static Request getRequest(CategoryEntity category){
            return new Request(category.getUrl(), processor)
                    .putExtra("category", category)
                    .setDownloader(CasperJsDownloader.getInstance())
                    ;
        }
        @Override
        public void process(ResultItems page) {
            CategoryEntity category = page.getRequest().getExtra("category", CategoryEntity.class);
            Document doc = (Document) page.getResource();

            Elements elements = doc.select("ul.mod_song_list > li");
            for (Element element : elements) {
                Element eName = element.select(".song_name").first();
                if(eName == null){
                    continue;
                }

                String name = eName.attr("title");
                Element eUrl = eName.select("a").first();
                // <a href="http://music.migu.cn/#/song/3914214/P2Z1Y2L1N1/1/001002A">时间都去哪儿了</a>
                String url = eUrl.absUrl("href");
                url = url.replace("/#/", "/");
                String songId = StringHelper.parseRegex(url, "song/([\\w]+)", 1);

                KindEntity music = new KindEntity("music")
                        .setName(name)
                        .setUrl(url)
                        .setCode(songId)
                        .setCategory(category);

                Elements eSingers = element.select(".singer_name a");
                List<Map<String,String>> singers = new ArrayList<>();
                for(Element sItem : eSingers){
                    // <a title="黄绮珊" href="http://music.migu.cn/#/singer/8729/P2Z1Y2L1N1/1/001002A">黄绮珊</a>
                    String singerName = sItem.attr("title");
                    String singerUrl = sItem.absUrl("href");
                    singerUrl = singerUrl.replace("/#/", "/");
                    String singerId = StringHelper.parseRegex(singerUrl, "singer/([\\w]+)/", 1);

                    Map<String, String> singer = new HashMap<>();
                    singer.put("name", singerName);
                    singer.put("id", singerId);
                    singer.put("url", singerUrl);

                    singers.add(singer);
                }
                if(!singers.isEmpty()){

                    music.set("singer", singers);
                }

                getLogger().trace(music);
                page.addTargetRequest(MusicDetailPageProcessor.getRequest(music));
            }

            generateNextPagesRequest(page);
        }

        private void generateNextPagesRequest(ResultItems page){
            Integer curPage = page.getRequest().getExtra("page", Integer.class);
            if (null != curPage) {// if not the first page
                return;
            }

            Document doc = (Document) page.getResource();
            Elements elements = doc.select("div.page_jf > a");
            if(elements.size() < 2){
                getLogger().error("Fail to get page num of "+page.getRequest());
                return;
            }
            Element element = elements.get(elements.size() -2 );
            String url = element.absUrl("href");
            String str = element.attr("pg");
            int pageTotalNum = Integer.valueOf(str);
            CategoryEntity category = page.getRequest().getExtra("category", CategoryEntity.class);

            for(int index = 2; index <= pageTotalNum; ++index) {
                // <a href="http://music.migu.cn/webfront/song/tagdetail.do?id=1000587710&amp;loc=P2Z1Y1L1N1&amp;locno=10&amp;cid=001002A&amp;pagenum=100" pg="100">100</a>
                String myurl = url.replaceAll("pagenum=[\\d]+$","pagenum="+index);
                Request request = new Request(myurl, processor)
                        .putExtra("category", category)
                        .putExtra("page", index)
                        .setPriority(7)
                        ;
                page.addTargetRequest(request);
            }
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

        @Override
        public void process(ResultItems page) {
            KindEntity music = page.getRequest().getExtra("music", KindEntity.class);
            Document doc = (Document) page.getResource();

            Element element = doc.select("div.gqxx_con > ul > li:nth-child(3) > a").first();
            if(element != null){
                // <a title="情感频道I·罪爱" href="http://music.migu.cn/#/album/1000623548/P2Z3Y12L1N1/3/001002A">情感频道I·罪爱</a>
                String name = element.attr("title");
                String url = element.absUrl("href");
                url = url.replace("/#/", "/");

                String code = StringHelper.parseRegex(url, "album/([\\d]+)/", 1);
                music.set("album", name);
                music.set("albumId", code);
                music.set("albumUrl", url);
            }

            element = doc.select(".migu_zs").first();
            if(element != null){
                // <p class="migu_zs" style="top:58px">咪咕指数：344191</p>
                String txt = element.text();
                String num = StringHelper.parseRegex(txt, "([\\d]+)", 1);
                try {
                    music.set("playNum", Integer.valueOf(num));
                }catch (NumberFormatException e){
                    getLogger().warn("Fail to parse 咪咕指数 of " + music + ". " + e.getMessage());
                }
            }

            element = doc.select(".favorCount").first();
            if(element != null){
                String txt = element.text();
                try{
                    music.set("favorNum", Integer.valueOf(txt));
                }catch (NumberFormatException e){
                    getLogger().warn("Fail to parse favorNum of " + music + ". " + e.getMessage());
                }
            }

            getLogger().trace("Detail:" + music);
            page.addItem(music);
        }
    }
}
