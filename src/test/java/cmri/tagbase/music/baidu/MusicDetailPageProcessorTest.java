package cmri.tagbase.music.baidu;

import cmri.tagbase.SiteName;
import cmri.utils.web.NetworkHelper;
import cmri.etl.pipeline.FilePipeline;
import cmri.etl.spider.SpiderAdapter;
import cmri.tagbase.orm.domain.KindEntity;
import org.junit.Before;
import org.junit.Test;

public class MusicDetailPageProcessorTest {
    @Before
    public void setUp(){
        NetworkHelper.setDefaultProxy();
    }
    @Test
    public void testProcess() throws Exception {
        new SpiderAdapter().addRequest(MusicDetailPageProcessor.getRequest(
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