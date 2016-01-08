package cmri.tagbase.anime.tian52;

import cmri.utils.web.NetworkHelper;
import cmri.etl.common.Request;
import cmri.etl.spider.SpiderAdapter;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

/**
 * Created by zhuyin on 6/8/15.
 */
public class TagPageProcessorTest {
    @Before
    public void setUp() {
        NetworkHelper.setDefaultProxy();
    }

    @Test
    public void testProcess() throws Exception {
        Collection<Request> requests = TagPageProcessor.getSeedRequests();
        new SpiderAdapter().addRequest(requests)
                .run();
    }
}