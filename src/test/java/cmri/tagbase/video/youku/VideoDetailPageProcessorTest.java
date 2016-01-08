package cmri.tagbase.video.youku;

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
 * Created by zhuyin on 5/13/15.
 */
public class VideoDetailPageProcessorTest {
    @Before
    public void setUp() {
        NetworkHelper.setDefaultProxy();
    }

    @Test
    public void testProcess() throws Exception {
        KindEntity video = new KindEntity("video")
                .setUrl("http://www.youku.com/show_page/id_zf519d1029c9f11e4b2ad.html")
                .setName("电视剧: 武媚娘传奇 浙江卫视TV版 2014")
                .setCategory(new CategoryEntity().setName("古装"))
                .setCode("zf519d1029c9f11e4b2ad");
        process(video);

        video = new KindEntity("video")
                .setUrl("http://www.youku.com/show_page/id_z25ae2098e21511de97c0.html")
                .setCategory(new CategoryEntity().setName("曲艺"))
                .setName("综艺: 郭德纲济公传书场")
                .setCode("z25ae2098e21511de97c0");
        process(video);

        video = new KindEntity("video")
                .setUrl("http://www.youku.com/show_page/id_za84975a650c711e29498.html")
                .setCategory(new CategoryEntity().setName("娱乐"))
                .setName("综艺: 完全娱乐 2013")
                .setCode("za84975a650c711e29498");
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
        VideoDetailPageProcessor.getInstance().process(page);
        System.out.println("series" + video.get("series"));
    }
}