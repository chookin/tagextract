package cmri.tagbase.video.youku;

import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.utils.web.NetworkHelper;
import cmri.etl.common.ResultItems;
import cmri.etl.downloader.JsoupDownloader;
import cmri.etl.spider.SpiderAdapter;
import cmri.tagbase.SiteName;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class TagPageProcessorTest {
    @Before
    public void setUp() {
        NetworkHelper.setDefaultProxy();
    }

    @Test
    public void testProcessSeedPage() throws IOException {
        ResultItems page = JsoupDownloader.getInstance().download(CategoryPageProcessor.getSeedRequests().iterator().next(),
                new SpiderAdapter());
        new CategoryPageProcessor().process(page);
        System.out.println(page.getAllFields());
    }

    @Test
    public void testProcess() throws IOException {
        ResultItems page = JsoupDownloader.getInstance().download(
                CategoryPageProcessor2.getRequest(new CategoryEntity().setSite(SiteName.Youku)
                                .setUrl("http://www.youku.com/v_olist/c_97.html")
                ),
                new SpiderAdapter());
        new CategoryPageProcessor2().process(page);
        System.out.println(page.getAllFields());
    }
}