package cmri.tagbase.music.migu;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.downloader.CasperJsDownloader;
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
 * Created by zhuyin on 4/7/15.
 */
public class MusicPageProcessor implements PageProcessor{
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
