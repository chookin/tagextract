package cmri.tagbase.read;

import cmri.tagbase.SiteName;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.utils.web.NetworkHelper;
import cmri.etl.pipeline.FilePipeline;
import cmri.etl.spider.SpiderAdapter;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.tagbase.read.BaiduYueduCollection.BookDetailPageProcessor;
import cmri.tagbase.read.BaiduYueduCollection.BookPageProcessor;
import cmri.tagbase.read.BaiduYueduCollection.CategoryPageProcessor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BaiduYueduCollectionTest {
    @Before
    public void setUp(){
        NetworkHelper.setDefaultProxy();
    }

    @Test
    public void testCategoryPageProcess() throws Exception {
        new SpiderAdapter().addPipeline(new FilePipeline())
                .addPipeline(resultItems -> System.out.println(resultItems.getItems()))
                .addPipeline(resultItems -> Assert.assertFalse(resultItems.getRequest().toString(), resultItems.getItems().isEmpty()))
                .test(CategoryPageProcessor.getSeedRequests());
    }

    @Test
    public void testPageProcess() throws Exception {
        CategoryEntity category = new CategoryEntity()
                .setCode("1010")
                .setName("外国文学")
                .setUrl("http://yuedu.baidu.com/book/list/6024?od=0&show=0&pn=1440");
        new SpiderAdapter().addPipeline(new FilePipeline())
                .addPipeline(resultItems -> System.out.println(resultItems.getItems()))
                .addPipeline(resultItems -> Assert.assertFalse(resultItems.getRequest().toString(), resultItems.isSkip()))
                .test(BookPageProcessor.getRequest(category));
    }

    @Test
    public void testDetailPageProcess() throws Exception {
        KindEntity book = new KindEntity("book")
                .setUrl("http://yuedu.baidu.com/ebook/e12e9ae56bec0975f465e2d8?fr=booklist")
                .setName("漂亮女王养成记")
                .setCode("e12e9ae56bec0975f465e2d8")
                .setSite(SiteName.BaiduRead);
        new SpiderAdapter().addPipeline(new FilePipeline())
                .addPipeline(resultItems -> System.out.println(resultItems.getItems()))
                .addPipeline(resultItems -> Assert.assertFalse(resultItems.getRequest().toString(), resultItems.getItems().isEmpty()))
                .test(BookDetailPageProcessor.getRequest(book));
    }
}