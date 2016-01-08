package cmri.tagbase.b2c;

import cmri.etl.pipeline.MongoPipeline;
import cmri.etl.pipeline.Pipeline;
import cmri.tagbase.base.KindCollect;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.utils.lang.BaseOper;
import cmri.tagbase.base.CategoryCollect;
import cmri.utils.dao.MongoHandler;
import com.mongodb.DBObject;

import java.util.Collection;

/**
 * Created by zhuyin on 3/24/15.
 */
abstract class GoodsCollect extends BaseOper implements CategoryCollect, KindCollect, GoodsDetailCollect {
    @Override
    public boolean action() {
        return collectCategories(getOptionsPack())
                || collectKinds(getOptionsPack())
                || collectGoodsDetail(getOptionsPack());
    }

    @Override
    public Pipeline getKindPipeline(CategoryEntity category){
        return new MongoPipeline(category){
            @Override
            protected int save(String collectionName, Collection<? extends DBObject> entities) {
                MongoHandler dao = MongoHandler.instance();
                try{
                    return dao.saveIfNotExists(collectionName, entities, (o1, o2) -> o1.get("_id").equals(o2.get("_id")));
                }finally {
                    dao.close();
                }
            }
        };
    }
}