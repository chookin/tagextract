package cmri.tagbase.game;

import cmri.tagbase.base.CategoryCollect;
import cmri.tagbase.base.KindCollect;
import cmri.utils.lang.BaseOper;

/**
 * Created by zhuyin on 6/9/15.
 */
abstract class GameCollect extends BaseOper implements CategoryCollect, KindCollect {
    @Override
    public boolean action() {
        return collectCategories(getOptions())
                || collectKinds(getOptions());
    }
}
