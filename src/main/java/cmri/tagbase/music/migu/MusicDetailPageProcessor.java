package cmri.tagbase.music.migu;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.lang.StringHelper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by zhuyin on 4/7/15.
 */
class MusicDetailPageProcessor implements PageProcessor {
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
