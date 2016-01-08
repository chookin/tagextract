package cmri.tagbase.anime.dm456;

import cmri.tagbase.SiteName;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.utils.web.NetworkHelper;
import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.downloader.JsoupDownloader;
import cmri.etl.spider.SpiderAdapter;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by zhuyin on 6/8/15.
 */
public class AnimePageProcessorTest {
    @Before
    public void setUp() {
        NetworkHelper.setDefaultProxy();
    }

    @Test
    public void testProcess() throws Exception {
        Request request = AnimePageProcessor.getRequest(new CategoryEntity().setUrl("http://www.dm456.com/donghua/rexue/").setName("热血").setSite(SiteName.dm456));
        ResultItems page = JsoupDownloader.getInstance().download(request,
                new SpiderAdapter());
        new AnimePageProcessor().process(page);
    }
}