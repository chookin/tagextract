package cmri.tagbase.music.migu;

import cmri.utils.web.NetworkHelper;
import cmri.etl.common.ResultItems;
import cmri.etl.downloader.JsoupDownloader;
import cmri.etl.spider.SpiderAdapter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class TagPageProcessorTest {

    @Before
    public void setUp() {
        NetworkHelper.setDefaultProxy();
    }

    @Test
    public void testProcess() throws IOException {
        ResultItems page = JsoupDownloader.getInstance().download(TagPageProcessor.getSeedRequests().iterator().next(),
                new SpiderAdapter());
        new TagPageProcessor().process(page);
        System.out.println(page.getAllFields());
    }
}