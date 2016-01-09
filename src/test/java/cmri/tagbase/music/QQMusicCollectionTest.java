package cmri.tagbase.music;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.downloader.JsoupDownloader;
import cmri.etl.pipeline.FilePipeline;
import cmri.etl.spider.SpiderAdapter;
import cmri.tagbase.SiteName;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.web.NetworkHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by zhuyin on 1/9/16.
 */
public class QQMusicCollectionTest {
    @Before
    public void setUp() {
        NetworkHelper.setDefaultProxy();
    }

    @Test
    public void testPageProcess() throws IOException {
        CategoryEntity category = new CategoryEntity().setCode("133")
                .setSite(SiteName.QQMusic);
        int pageNum = 1;
        Request request = QQMusicCollection.MusicPageProcessor.getRequest(category, pageNum).ignoreCache(true);
        ResultItems page = JsoupDownloader.getInstance().download(request, new SpiderAdapter());
        System.out.println(page.getResource());

        new QQMusicCollection.MusicPageProcessor().process(page);
        System.out.println(page.getAllFields());
    }

    @Test
    public void testMusicListPageProcess() throws Exception {
        KindEntity musicList = new KindEntity("music")
                .setName("泰语温柔呼唤 R&B寂寞召回")
                .setCode("32598905")
                .setCategory(new CategoryEntity().setSite(SiteName.QQMusic))
                ;
        new SpiderAdapter()
                .addRequest(QQMusicCollection.MusicListPageProcessor.getRequest(musicList))
                .addPipeline(new FilePipeline(), resultItems -> {
                    System.out.println(resultItems.getRequest().getExtra("musicList", KindEntity.class));
                    System.out.println(resultItems.getField("kinds"));
                })
                .run();
    }
}
