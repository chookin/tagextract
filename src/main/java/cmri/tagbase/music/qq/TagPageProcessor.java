package cmri.tagbase.music.qq;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.SiteName;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.utils.configuration.ConfigFileManager;
import cmri.utils.io.FileHelper;
import cmri.utils.lang.StringHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

/**
 * Created by zhuyin on 4/2/15.
 */
public class TagPageProcessor implements PageProcessor {
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
