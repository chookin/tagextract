package cmri.tagbase.video.heshijie;

import cmri.utils.web.NetworkHelper;
import cmri.etl.common.Request;
import cmri.etl.spider.SpiderAdapter;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

/**
 * Created by zhuyin on 6/5/15.
 */
public class TagPageProcessorTest {
    @Before
    public void setUp() {
        NetworkHelper.setDefaultProxy();
    }

    @Test
    public void testProcess() throws Exception {
        Collection<Request> requests = CategoryPageProcessor.getSeedRequests();
        new SpiderAdapter().addRequest(requests)
                .run();
    }
}