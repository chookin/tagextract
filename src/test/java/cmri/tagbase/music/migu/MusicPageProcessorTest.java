package cmri.tagbase.music.migu;

import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.utils.web.NetworkHelper;
import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.downloader.CasperJsDownloader;
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
        String startUrl = "http://music.migu.cn/#/tag/1000001701/P2Z1Y1L1N1/26/001002A";
        startUrl = "http://music.migu.cn/tag/1000001701/P2Z1Y1L1N1/26/001002A";
        ResultItems page = CasperJsDownloader.getInstance().download(new Request().setUrl(startUrl)
                .putExtra("category", new CategoryEntity())
                .ignoreCache(true),
                new SpiderAdapter());
        new MusicPageProcessor().process(page);
        System.out.println(page.getAllFields());
    }
}