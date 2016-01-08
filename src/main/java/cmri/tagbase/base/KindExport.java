package cmri.tagbase.base;

import cmri.tagbase.orm.dao.KindDAO;
import cmri.utils.web.UrlHelper;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.configuration.ConfigManager;
import cmri.utils.io.FileHelper;
import cmri.utils.lang.BaseOper;
import cmri.utils.lang.TimeHelper;
import cmri.utils.lang.JsonHelper;
import com.mongodb.QueryBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * Created by zhuyin on 3/25/15.
 */
public abstract class KindExport extends BaseOper {
    private static Logger LOG = Logger.getLogger(KindExport.class);
    private static final String sep = "|";

    @Override
    public boolean action() {
        try {
            return exportCodes();
        } catch (Exception e) {
            LOG.error(null, e);
            return true;
        }
    }

    protected abstract KindDAO getKindDAO();

    Set<String> parseSiteOption(){
        String option = "--sites";
        String paras = getOptionsPack().get(option);
        if(paras == null){
            return null;
        }
        return JsonHelper.parseStringSet(paras);
    }

    boolean exportCodes() throws IOException {
        Set<String> sites = parseSiteOption();
        if (sites == null) {
            throw new IllegalArgumentException("please assign value of '--sites'");
        } else {
            LOG.info("Phase: query goods with tag for site " + sites);
        }

        List<KindEntity> kinds = queryKinds(sites);
        saveCodes2Csv(sites, kinds);
        return true;
    }

    private List<KindEntity> queryKinds(Collection<String> sites){
        KindDAO dao = getKindDAO();
        try {
            List<KindEntity> kinds;
            QueryBuilder queryBuilder = new QueryBuilder()
                    .put("tag").exists(true)
                    .put("site").in(sites);
            kinds = dao.find(queryBuilder.get());
            LOG.info("Find "+kinds.size() +" items.");
            return kinds;
        } finally {
            dao.close();
        }
    }

    private void saveCodes2Csv(Collection<String> sites, Collection<KindEntity> kinds) throws IOException {
        LOG.info("Phase: build csv string");

        int keyMaxLength = ConfigManager.getInt("export.codes.maxLength");
        if (keyMaxLength == -1) {
            keyMaxLength = Integer.MAX_VALUE;
        }

        StringWriter writer = new StringWriter();
        CsvListWriter csv = new CsvListWriter(writer, CsvPreference.EXCEL_PREFERENCE);
        csv.write("标签树节点名称", "domain", "key");

        // use TreeMap to export in order.
        TreeMap<String, List<KindEntity>> kindMap = classifyByTag(kinds);
        for (Map.Entry<String, List<KindEntity>> entry : kindMap.entrySet()) {
            TreeMap<String, List<KindEntity>> cGoods = classifyByDomain(entry.getValue());
            String tag = entry.getKey();
            for (Map.Entry<String, List<KindEntity>> subEntry : cGoods.entrySet()) {
                if (subEntry.getValue().isEmpty()) {
                    continue;
                }
                String domain = subEntry.getKey();
                List<String> keys = getCodesString(subEntry.getValue(), keyMaxLength);
                for (String key : keys) {
                    csv.write(tag, domain, key);
                }
            }
        }
        csv.close();
        LOG.info("Phase: write csv to file");
        String fileName = new File(getCodesFileName(sites)).getAbsolutePath();
        FileHelper.save(writer.toString(), fileName);
    }

    /**
     * Classify by tag
     * @return the classified result
     */
    static TreeMap<String, List<KindEntity>> classifyByTag(Collection<KindEntity> items) {
        TreeMap<String, List<KindEntity>> rst = new TreeMap<>();
        for (KindEntity item : items) {
            Set<String> tags = item.getTags();
            for(String tag: tags) {
                List<KindEntity> list = rst.get(tag);
                if (list == null) {
                    list = new ArrayList<>();
                    rst.put(tag, list);
                }
                list.add(item);
            }
        }
        return rst;
    }

    static TreeMap<String, List<KindEntity>> classifyByDomain(Collection<KindEntity> items) {
        TreeMap<String, List<KindEntity>> rst = new TreeMap<>();
        for (KindEntity item : items) {
            String url = item.getUrl();
            String key = UrlHelper.getBaseDomain(url);

            if (key == null) {
                continue;
            }
            List<KindEntity> list = rst.get(key);
            if (list == null) {
                list = new ArrayList<>();
                rst.put(key, list);
            }
            list.add(item);
        }
        return rst;
    }

    List<String> getCodesString(Collection<KindEntity> items, int keyMaxLength) {
        List<String> paths = new ArrayList<>();
        StringBuilder strb = new StringBuilder();
        for (KindEntity item : items) {
            String code = item.getCode();
            if (code == null || code.isEmpty()) {
                continue;
            }
            if (strb.length() + code.length() > keyMaxLength) {
                paths.add(strb.delete(strb.length() - sep.length(), strb.length()).toString());
                strb = new StringBuilder();
            }
            strb.append(code).append(sep);
        }
        if (strb.length() > 0) {
            paths.add(strb.delete(strb.length() - sep.length(), strb.length()).toString());
        }
        return paths;
    }

    String getCodesFileName(Collection<String> sites) {
        String path = FilenameUtils.concat(ConfigManager.get("results.directory"), "export");
        StringBuilder strb = new StringBuilder("codes-");
        for (String site : sites) {
            strb.append(site).append("-");
        }
        strb.append(TimeHelper.toString(new Date(), "yyyyMMddHHmmss")).append(".csv");
        return FilenameUtils.concat(path, strb.toString());
    }
}
