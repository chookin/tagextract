package cmri.tagbase.game;

import cmri.tagbase.orm.dao.KindDAO;
import cmri.tagbase.base.TagUpdate;

/**
 * Created by zhuyin on 6/12/15.
 */
public class GameTagUpdate extends TagUpdate {
    @Override
    public KindDAO getDAO() {
        return new KindDAO("game");
    }

    public static void main(String[] args){
        new GameTagUpdate().setArgs(args).action();
    }
}
