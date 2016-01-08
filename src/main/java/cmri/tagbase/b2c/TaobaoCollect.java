package cmri.tagbase.b2c;

import cmri.etl.common.Request;
import cmri.tagbase.SiteName;
import cmri.tagbase.b2c.mobile.tb.CategoryPageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.b2c.mobile.tb.GoodsPageProcessor;
import cmri.tagbase.b2c.web.tb.GoodsDetailPageProcessor;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.configuration.ConfigManager;

import java.util.Collection;

/**
 * Created by zhuyin on 3/24/15.
 */
public class TaobaoCollect extends GoodsCollect {
    @Override
    public String getSiteName() {
        return SiteName.Taobao;
    }

    @Override
    public Request getRequest(KindEntity good) {
        return GoodsDetailPageProcessor.getRequest(good);
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
        new TaobaoCollect().setArgs(args).action();
    }
}