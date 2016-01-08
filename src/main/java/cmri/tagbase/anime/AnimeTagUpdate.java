package cmri.tagbase.anime;

import cmri.tagbase.base.TagUpdate;
import cmri.tagbase.orm.dao.KindDAO;

/**
 * Created by zhuyin on 6/12/15.
 */
public class AnimeTagUpdate extends TagUpdate {
    @Override
    public KindDAO getDAO() {
        return new KindDAO("anime");
    }

    public static void main(String[] args){
        new AnimeTagUpdate().setArgs(args).action();
    }
}
