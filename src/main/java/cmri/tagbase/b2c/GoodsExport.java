package cmri.tagbase.b2c;

import cmri.tagbase.orm.dao.KindDAO;
import cmri.tagbase.base.KindExport;

/**
 * Created by zhuyin on 3/25/15.
 */
public class GoodsExport extends KindExport {
    @Override
    protected KindDAO getKindDAO() {
        return new KindDAO("goods");
    }
    public static void main(String[] args){
        new GoodsExport().setArgs(args).action();
    }
}
