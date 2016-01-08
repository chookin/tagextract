package cmri.tagbase.b2c;

import cmri.etl.common.Request;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.SiteName;
import cmri.tagbase.b2c.mobile.amazon.CategoryPageProcessor;
import cmri.tagbase.b2c.mobile.amazon.GoodsPageProcessor;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.configuration.ConfigManager;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collection;

/**
 * Created by zhuyin on 3/24/15.
 */
public class AmazonCollect extends GoodsCollect {

    @Override
    public String getSiteName() {
        return SiteName.Amazon;
    }

    @Override
    public Request getRequest(KindEntity good) {
        throw new NotImplementedException();
    }

    @Override
    public Request getRequest(CategoryEntity category) {
        return GoodsPageProcessor.getRequest(category);
    }

    @Override
    public Collection<Request> getSeedCategoryRequests() {
        return CategoryPageProcessor.getSeedRequests();
    }

    @Override
    public String getCategoryUserAgent() {
        return ConfigManager.get("mobile.userAgent");
    }

    public static void main(String[] args) {
        new AmazonCollect().setArgs(args).action();
    }
}
