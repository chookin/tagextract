package cmri.tagbase.music.baidu;

import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.utils.web.NetworkHelper;
import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.downloader.JsoupDownloader;
import cmri.etl.spider.SpiderAdapter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class MusicPageProcessorTest {
    @Before
    public void setUp() {
        NetworkHelper.setDefaultProxy();
    }

    @Test
    public void testProcess() throws IOException {
        String startUrl = "http://music.baidu.com/tag/%E7%BB%8F%E5%85%B8%E8%80%81%E6%AD%8C?start=975&size=25";
        ResultItems page = JsoupDownloader.getInstance().download(new Request().setUrl(startUrl).putExtra("category", new CategoryEntity()), new SpiderAdapter());
        new MusicPageProcessor().process(page);
        System.out.println(page.getAllFields());
    }
}