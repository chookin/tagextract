package cmri.tagbase.read;

import cmri.tagbase.orm.dao.KindDAO;
import cmri.tagbase.base.TagUpdate;

/**
 * Created by zhuyin on 3/25/15.
 */
public class BookTagUpdate extends TagUpdate {
    @Override
    public KindDAO getDAO() {
        return new KindDAO("book");
    }

    public static void main(String[] args){
        new BookTagUpdate().setArgs(args).action();
    }
}
