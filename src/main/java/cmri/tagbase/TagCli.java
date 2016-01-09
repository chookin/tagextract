package cmri.tagbase;

import cmri.etl.job.Job;
import cmri.utils.lang.BaseOper;
import cmri.utils.lang.JsonHelper;
import org.apache.log4j.Logger;

/**
 * Created by zhuyin on 3/25/15.
 */
public class TagCli extends BaseOper {
    private static final Logger LOG = Logger.getLogger(TagCli.class);
    @Override
    public boolean action() {
        if(getOptions().notExists("class")) {
            String json = "{class:BaiduYueduCollection,task:collect-categories,proxy.enable:false,download.concurrent.num:1,download.sleepMilliseconds:1000,all:true}";
            getOptions().put(JsonHelper.parseStringMap(json));
        }
        try {
            Job.createJob(getOptions().options()).start();
        } catch (ReflectiveOperationException e) {
            LOG.error(null, e);
        }
        return true;
    }

    public static void main(String[] args){
        new TagCli().setArgs(args).action();
    }
}
