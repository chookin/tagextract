package cmri.tagbase.video.heshijie;

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
 * Created by zhuyin on 6/5/15.
 */
public class VideoPageProcessorTest {
    @Before
    public void setUp() {
        NetworkHelper.setDefaultProxy();
    }

    @Test
    public void testProcess() throws Exception {
        Request request = VideoPageProcessor.getRequest(new CategoryEntity().setUrl("http://www.lovev.com/pc/list/ScreeningPage.jsp?n=1000&type=%E6%88%98%E4%BA%89").setName("战争").setSite(SiteName.Heshijie));
        ResultItems page = JsoupDownloader.getInstance().download(request,
                new SpiderAdapter());
        VideoPageProcessor.getInstance().process(page);
    }
}