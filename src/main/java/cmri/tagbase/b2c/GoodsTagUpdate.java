package cmri.tagbase.b2c;

import cmri.tagbase.base.TagUpdate;
import cmri.tagbase.orm.dao.KindDAO;

/**
 * Created by zhuyin on 3/25/15.
 */
public class GoodsTagUpdate extends TagUpdate {
    @Override
    public KindDAO getDAO() {
        return new KindDAO("goods");
    }

    public static void main(String[] args){
        new GoodsTagUpdate().setArgs(args).action();
    }
}
