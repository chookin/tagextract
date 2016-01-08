package cmri.tagbase.site;

import cmri.etl.common.IdItem;
import cmri.etl.pipeline.MongoPipeline;
import cmri.etl.pipeline.Pipeline;
import cmri.etl.pipeline.RedisPipeline;
import cmri.tagbase.base.KindCollect;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.SiteEntity;
import cmri.utils.lang.BaseOper;
import cmri.tagbase.base.CategoryCollect;
import cmri.utils.lang.SerializationHelper;
import redis.clients.jedis.Jedis;

/**
 * Created by zhuyin on 3/25/15.
 */
abstract class SiteCollect extends BaseOper implements CategoryCollect, KindCollect {
    @Override
    public boolean action() {
        return collectCategories(getOptionsPack())
                || collectKinds(getOptionsPack());
    }

    @Override
    public Pipeline getKindPipeline(CategoryEntity category) {
        return new RedisSitePipeline().addPipeline(new MongoPipeline(category));
    }

    static class RedisSitePipeline extends RedisPipeline<SiteEntity> {
        public RedisSitePipeline() {
            super(SiteEntity.class);
        }

        @Override
        protected void merge(SiteEntity newOne, Jedis jedis){
            String val = jedis.get(newOne.getId());
            if (val == null)  return;
            IdItem saved = SerializationHelper.deserialize(val);
            if(!(saved instanceof SiteEntity)) {
                return;
            }

            SiteEntity savedKind = (SiteEntity) saved;
            newOne.addCategories(savedKind.getCategories());
        }
    }
}
