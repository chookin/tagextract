package cmri.tagbase.read;

import cmri.utils.web.NetworkHelper;
import cmri.etl.common.Request;
import cmri.etl.pipeline.FilePipeline;
import cmri.etl.spider.SpiderAdapter;
import cmri.tagbase.SiteName;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

/**
 * Created by zhuyin on 8/12/15.
 */
public class ZenghengCollectTest {
    @Before
    public void setUp(){
        NetworkHelper.setDefaultProxy();
    }

    @Test
    public void testCategoryPageProcess() throws Exception {
        Collection<Request> requests = ZonghengCollection.CategoryPageProcessor.getSeedRequests();
        new SpiderAdapter().addRequest(requests)
                .run();
    }

    @Test
    public void testPageProcess() throws Exception {
        CategoryEntity category = new CategoryEntity()
                .setName("古装言情")
                .setCode("1062")
                .setUrl("http://book.zongheng.com/store/c6/c1062/b0/u0/p1/v9/s9/t0/ALL.html")
                .setSite(SiteName.Zongheng);
        new SpiderAdapter()
                .addRequest(ZonghengCollection.BookPageProcessor.getRequest(category))
                .run();
    }

    @Test
    public void testDetailPageProcess() throws Exception {
        KindEntity book = new KindEntity("book")
                .setUrl("http://book.zongheng.com/book/39813.html")
                .setName("天才医生")
                .setCode("39813")
                .setSite(SiteName.Zongheng);
        new SpiderAdapter().addPipeline(new FilePipeline())
                .addPipeline(resultItems -> System.out.println(resultItems.getRequest().getExtra("bookEntity", KindEntity.class)))
                .test(ZonghengCollection.BookDetailPageProcessor.getRequest(book));
    }
}
