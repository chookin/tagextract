package cmri.tagbase.music.qq;

import cmri.tagbase.SiteName;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.utils.web.NetworkHelper;
import cmri.etl.common.ResultItems;
import cmri.etl.pipeline.FilePipeline;
import cmri.etl.pipeline.Pipeline;
import cmri.etl.spider.SpiderAdapter;
import cmri.tagbase.orm.domain.KindEntity;
import org.junit.Before;
import org.junit.Test;

public class MusicListPageProcessorTest {
    @Before
    public void setUp(){
        NetworkHelper.setDefaultProxy();
    }
    @Test
    public void testProcess() throws Exception {
        KindEntity musicList = new KindEntity("music")
                .setName("泰语温柔呼唤 R&B寂寞召回")
                .setCode("32598905")
                .setCategory(new CategoryEntity().setSite(SiteName.QQMusic))
                ;
        new SpiderAdapter()
                .addRequest(MusicListPageProcessor.getRequest(musicList))
                .addPipeline(new FilePipeline(), new Pipeline() {
                    @Override
                    public void process(ResultItems resultItems) {
                        System.out.println(resultItems.getRequest().getExtra("musicList", KindEntity.class));
                        System.out.println(resultItems.getField("kinds"));
                    }
                })
                .run();
    }
}