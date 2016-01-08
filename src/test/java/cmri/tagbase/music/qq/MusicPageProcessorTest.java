package cmri.tagbase.music.qq;

import cmri.tagbase.SiteName;
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
        CategoryEntity category = new CategoryEntity().setCode("133")
                .setSite(SiteName.QQMusic);
        int pageNum = 1;
        Request request = MusicPageProcessor.getRequest(category, pageNum).ignoreCache(true);
        ResultItems page = JsoupDownloader.getInstance().download(request, new SpiderAdapter());
        System.out.println(page.getResource());

        new MusicPageProcessor().process(page);
        System.out.println(page.getAllFields());
    }
}