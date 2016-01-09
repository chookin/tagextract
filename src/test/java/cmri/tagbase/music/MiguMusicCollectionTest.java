package cmri.tagbase.music;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.downloader.CasperJsDownloader;
import cmri.etl.downloader.JsoupDownloader;
import cmri.etl.spider.SpiderAdapter;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.utils.web.NetworkHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by zhuyin on 1/9/16.
 */
public class MiguMusicCollectionTest {
    @Before
    public void setUp() {
        NetworkHelper.setDefaultProxy();
    }

    @Test
    public void testTagPageProcess() throws IOException {
        ResultItems page = JsoupDownloader.getInstance().download(MiguMusicCollection.TagPageProcessor.getSeedRequests().iterator().next(),
                new SpiderAdapter());
        new MiguMusicCollection.TagPageProcessor().process(page);
        System.out.println(page.getAllFields());
    }

    @Test
    public void testPageProcess() throws IOException {
        String startUrl = "http://music.migu.cn/#/tag/1000001701/P2Z1Y1L1N1/26/001002A";
        startUrl = "http://music.migu.cn/tag/1000001701/P2Z1Y1L1N1/26/001002A";
        ResultItems page = CasperJsDownloader.getInstance().download(new Request().setUrl(startUrl)
                        .putExtra("category", new CategoryEntity())
                        .ignoreCache(true),
                new SpiderAdapter());
        new MiguMusicCollection.MusicPageProcessor().process(page);
        System.out.println(page.getAllFields());
    }
}