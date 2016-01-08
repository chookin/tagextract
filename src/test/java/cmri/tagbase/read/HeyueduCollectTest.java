package cmri.tagbase.read;

import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.utils.web.NetworkHelper;
import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.downloader.CasperJsDownloader;
import cmri.etl.downloader.JsoupDownloader;
import cmri.etl.spider.SpiderAdapter;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.configuration.ConfigManager;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

/**
 * Created by zhuyin on 8/12/15.
 */
public class HeyueduCollectTest {
    @Before
    public void setUp(){
        NetworkHelper.setDefaultProxy();
    }

    @Test
    public void testCategoryPageProcess() throws Exception {
        Collection<Request> requests = HeyueduCollection.CategoryPageProcessor.getSeedRequests();
        new SpiderAdapter().addRequest(requests)
                .run();
    }

    @Test
    public void testPageProcess() throws Exception {
        String url = "http://www.cmread.com/u/booklist?nodeId=6897898";
        ResultItems page =  new ResultItems(
                new Request().setUrl(url)
                        .putExtra("categoryEntity", new CategoryEntity().setCode("6897898")),
                new SpiderAdapter())
                .setResource(JsoupDownloader.getInstance().getDocument(url));
        new HeyueduCollection.BookPageProcessor().process(page);
        Assert.assertEquals(true, ((List<KindEntity>) page.getField("kinds")).size() > 0);
    }

    @Test
    public void testAddNewRequest() throws Exception {
        String url = "http://www.cmread.com/u/booklist?nodeId=6897898";
        ResultItems page =  new ResultItems(
                new Request().setUrl(url)
                        .putExtra("categoryEntity", new CategoryEntity().setCode("6897898")),
                new SpiderAdapter());
        new HeyueduCollection.BookPageProcessor().addNewRequest(page);
        System.out.println("target requests count: " + page.getTargetRequests().size());
        Assert.assertEquals(true, page.getTargetRequests().size() > 10);
    }

    @Test
    public void testGetPageTotalNum() throws Exception {
        String url = "http://www.cmread.com/u/booklist?nodeId=6897898";
        Request request = new Request().setUrl(url);
        new java.io.File(request.getFilePath()).delete();

        Document doc = CasperJsDownloader.getInstance().getDocument(url, ConfigManager.get("web.userAgent"));
        Integer num = new HeyueduCollection.BookPageProcessor().getPageTotalNum(doc);
        System.out.println("total page number: " + num);
        Assert.assertNotNull(num);
    }
}
