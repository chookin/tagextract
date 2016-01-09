package cmri.tagbase.music;

import cmri.etl.pipeline.FilePipeline;
import cmri.etl.spider.SpiderAdapter;
import cmri.tagbase.SiteName;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.web.NetworkHelper;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by zhuyin on 1/9/16.
 */
public class BaiduMusicCollectionTest {
    @Before
    public void setUp(){
        NetworkHelper.setDefaultProxy();
    }
    @Test
    public void testProcess() throws Exception {
        new SpiderAdapter().addRequest(BaiduMusicCollection.MusicDetailPageProcessor.getRequest(
                        new KindEntity("music")
                                .setUrl("http://music.baidu.com/song/13139680")
                                .setName("我可以抱你吗")
                                .setCode("13139680")
                                .setSite(SiteName.BaiduMusic)
                )
        )
                .addPipeline(new FilePipeline(), resultItems -> System.out.println(resultItems.getItems()))
                .run();
    }
}