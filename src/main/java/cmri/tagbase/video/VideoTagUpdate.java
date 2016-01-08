package cmri.tagbase.video;

import cmri.tagbase.base.TagUpdate;
import cmri.tagbase.orm.dao.KindDAO;

/**
 * Created by zhuyin on 6/12/15.
 */
public class VideoTagUpdate extends TagUpdate {
    @Override
    public KindDAO getDAO() {
        return new KindDAO("video");
    }

    public static void main(String[] args){
        new VideoTagUpdate().setArgs(args).action();
    }
}
