package cmri.tagbase.video.heshijie;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.SiteName;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by zhuyin on 6/4/15.
 */
public class CategoryPageProcessor implements PageProcessor {
    private static final CategoryPageProcessor processor = new CategoryPageProcessor();

    public static Collection<Request> getSeedRequests(){
        return Collections.singletonList(new Request("http://www.lovev.com/", processor)
        );
    }

    @Override
    public void process(ResultItems page) {
        CategoryEntity category = new CategoryEntity().setName("电影")
                .setUrl("http://www.lovev.com/pc/movie/index.jsp")
                .setCode("1000")
                .setSite(SiteName.Heshijie);
        getLogger().trace(category);
        page.addItem(category);
        page.addTargetRequest(CategoryPageProcessor2.getRequest(category));

        category = new CategoryEntity().setName("电视剧")
                .setUrl("http://www.lovev.com/pc/drama/index.jsp")
                .setCode("1001")
                .setSite(SiteName.Heshijie);
        getLogger().trace(category);
        page.addItem(category);
        page.addTargetRequest(CategoryPageProcessor2.getRequest(category));
    }
}
