package cmri.tagbase.base;

import cmri.etl.job.JobAdapter;
import org.apache.commons.lang3.Validate;

/**
 * Created by zhuyin on 9/9/15.
 */
public abstract class CKCollection extends JobAdapter implements CategoryCollection, KindCollection{
    @Override
    public void run() {
        onStart();
        try {
            String task = optionsPack.get("task");
            Validate.notNull(task, "please assign para 'task'");
            switch (task){
                case "collect-categories":
                    collectCategories(optionsPack.options(), this.spidersListener());
                    break;
                case "collect-kinds":
                    collectKinds(optionsPack, this.spidersListener());
                    break;
                default:
                    throw new IllegalArgumentException("unknown task "+ task);
            }
            onSuccess();
        }catch (Throwable t){
            onFail();
            LOG.error(null, t);
        }
    }
}
