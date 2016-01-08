package cmri.tagbase.game.heyouxi;

import cmri.utils.web.NetworkHelper;
import cmri.etl.common.Request;
import cmri.etl.spider.SpiderAdapter;
import cmri.tagbase.SiteName;
import cmri.tagbase.orm.domain.CategoryEntity;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by zhuyin on 6/11/15.
 */
public class GamePageProcessorTest {

    @Before
    public void setUp() {
        NetworkHelper.setDefaultProxy();
    }

    @Test
    public void testProcess() throws Exception {
        Request request = GamePageProcessor.getRequest(new CategoryEntity().setName("休闲益智").setCode("1").setSite(SiteName.Heyouxi));
        new SpiderAdapter().addRequest(request).run();
    }
}