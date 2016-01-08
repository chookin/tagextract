package cmri.tagbase.video.aiqiyi;

import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.utils.web.NetworkHelper;
import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.downloader.JsoupDownloader;
import cmri.etl.pipeline.FilePipeline;
import cmri.etl.spider.SpiderAdapter;
import cmri.tagbase.orm.domain.KindEntity;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by zhuyin on 5/14/15.
 */
public class VideoDetailPageProcessorTest {
    @Before
    public void setUp() {
        NetworkHelper.setDefaultProxy();
    }

    @Test
    public void testProcess() throws Exception {
        KindEntity video = new KindEntity("video").setName("虎妈猫爸").setUrl("http://www.iqiyi.com/a_19rrhaywod.html").setCategory(new CategoryEntity().setName("喜剧"));
        process(video);

        video = new KindEntity("video").setName("武媚娘传奇浙江卫视版").setUrl("http://www.iqiyi.com/a_19rrhaxo5d.html").setCategory(new CategoryEntity().setName("古装剧"));
        process(video);

        video = new KindEntity("video").setName("非诚勿扰").setUrl("http://www.iqiyi.com/a_19rrguakdx.html").setCategory(new CategoryEntity().setName("综艺/情感"));
        process(video);
    }

    private void process(KindEntity video) throws IOException {
        Request request = VideoDetailPageProcessor
                .getRequest(video)
                ;
        ResultItems page = JsoupDownloader.getInstance().download(request,
                new SpiderAdapter());
        if(!page.isCacheUsed())
            new FilePipeline().process(page);
        VideoDetailPageProcessor.instance.process(page);
        System.out.println("series" + video.get("series"));
    }
}