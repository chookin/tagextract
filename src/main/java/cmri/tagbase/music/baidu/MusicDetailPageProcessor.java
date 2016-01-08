package cmri.tagbase.music.baidu;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.KindEntity;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhuyin on 3/31/15.
 */
public class MusicDetailPageProcessor implements PageProcessor {
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
