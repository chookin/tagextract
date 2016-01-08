package cmri.tagbase.read;

import cmri.tagbase.base.KindExport;
import cmri.tagbase.orm.dao.KindDAO;

/**
 * Created by zhuyin on 3/2seq5/15.
 */
public class BookExport extends KindExport {
    @Override
    protected KindDAO getKindDAO() {
        return new KindDAO("book");
    }
    public static void main(String[] args){
        new BookExport().setArgs(args).action();
    }
}