package cmri.tagbase.video.youku;

import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.utils.web.NetworkHelper;
import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.downloader.JsoupDownloader;
import cmri.etl.spider.SpiderAdapter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by zhuyin on 5/13/15.
 */
public class VideoPageProcessorTest {
    @Before
    public void setUp() {
        NetworkHelper.setDefaultProxy();
    }

    @Ignore
    @Test
    public void testProcess() throws Exception {
        Request request = VideoPageProcessor.getRequest(new CategoryEntity().setUrl("http://www.youku.com/v_olist/c_97_s_1_d_1_g_%E6%AD%A6%E4%BE%A0.html"));
        ResultItems page = JsoupDownloader.getInstance().download(request,
                new SpiderAdapter());
        VideoPageProcessor.getInstance().process(page);
    }


    @Test
    public void testProcessEmptyPage() throws Exception {
        Request request = VideoPageProcessor.getRequest(new CategoryEntity().setUrl("http://www.youku.com/v_showlist/c98d1s1g2114.html"));
        ResultItems page = JsoupDownloader.getInstance().download(request,
                new SpiderAdapter());
        VideoPageProcessor.getInstance().process(page);

        request = VideoPageProcessor.getRequest(new CategoryEntity().setUrl("http://www.youku.com/v_showlist/c98g2114d4s1.html"));
        page = JsoupDownloader.getInstance().download(request,
                new SpiderAdapter());
        VideoPageProcessor.getInstance().process(page);
    }
}