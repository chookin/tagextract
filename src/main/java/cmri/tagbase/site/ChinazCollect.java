package cmri.tagbase.site;

import cmri.etl.common.Request;
import cmri.tagbase.SiteName;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.site.chinaz.CategoryPageProcessor;
import cmri.tagbase.site.chinaz.TopSitePageProcessor;

import java.util.Collection;

/**
 * Created by zhuyin on 3/25/15.
 */
public class ChinazCollect extends SiteCollect {
    @Override
    public String getSiteName() {
        return SiteName.Chinaz;
    }

    @Override
    public Collection<Request> getSeedCategoryRequests() {
        return CategoryPageProcessor.getSeedRequests();
    }

    @Override
    public Request getRequest(CategoryEntity category) {
        return TopSitePageProcessor.getRequest(category);
    }

    public static void main(String[] args){
        new ChinazCollect().setArgs(args).action();
    }
}
