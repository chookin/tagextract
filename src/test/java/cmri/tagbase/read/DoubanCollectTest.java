package cmri.tagbase.read;

import cmri.tagbase.SiteName;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.utils.web.NetworkHelper;
import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.downloader.JsoupDownloader;
import cmri.etl.pipeline.FilePipeline;
import cmri.etl.spider.SpiderAdapter;
import cmri.tagbase.orm.domain.KindEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

/**
 * Created by zhuyin on 8/12/15.
 */
public class DoubanCollectTest {
    @Before
    public void setUp(){
        NetworkHelper.setDefaultProxy();
    }

    @Test
    public void testCategoryProcess() throws Exception {
        Collection<Request> requests = DoubanCollection.CategoryPageProcessor.getSeedRequests();
        new SpiderAdapter().addRequest(requests)
                .run();
    }

    @Test
    public void testPageProcess() throws Exception {
        String url = "http://book.douban.com/tag/%E7%A8%8B%E5%BA%8F?start=0&type=T";
        ResultItems page =  new ResultItems(
                new Request().setUrl(url)
                        .putExtra("categoryEntity", new CategoryEntity()),
                new SpiderAdapter())
                .setResource(JsoupDownloader.getInstance().getDocument(url));
        new DoubanCollection.BookPageProcessor().process(page);
        Assert.assertEquals(true, ((List<KindEntity>) page.getField("kinds")).size() > 0);
        Assert.assertEquals(true, page.getTargetRequests().size() > 0);
    }

    @Test
    public void testDetailPageProcess() throws Exception {
        KindEntity book = new KindEntity("book")
                .setUrl("http://book.douban.com/subject/1281519/")
                .setName("C程序设计语言")
                .setCode("1139336")
                .setSite(SiteName.DoubanRead);
        new SpiderAdapter().addPipeline(new FilePipeline())
                .addPipeline(resultItems -> System.out.println(resultItems.getRequest().getExtra("bookEntity", KindEntity.class)))
                .addPipeline(new FilePipeline())
                .test(DoubanCollection.BookDetailPageProcessor.getRequest(book));
    }
}
