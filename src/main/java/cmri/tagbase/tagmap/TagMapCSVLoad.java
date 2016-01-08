package cmri.tagbase.tagmap;

import cmri.tagbase.SiteName;
import cmri.tagbase.orm.domain.TagMapEntity;
import cmri.tagbase.orm.repository.TagMapRepository;
import cmri.tagbase.utils.SpringHelper;
import cmri.utils.lang.BaseOper;
import cmri.utils.configuration.ConfigFileManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by zhuyin on 4/15/15.
 * Used to parse tag map configuration from csv file, and save parsed results to db.
 */
@Service
public class TagMapCSVLoad extends BaseOper {
    private static final Logger LOG = Logger.getLogger(TagMap.class);

    @Autowired
    private TagMapRepository tagMapRepository;

    @Override
    public boolean action() {
        String fileName = this.getOptionsPack().get("--file");
        if(fileName != null) {
            loadAndSave(fileName);
        }else{
            List<String> siteNames = new ArrayList<>();
            Collections.addAll(siteNames, SiteName.Amazon, SiteName.Jd, SiteName.Yihaodian, SiteName.Taobao, SiteName.Tmall);
            Collections.addAll(siteNames, SiteName.BaiduRead, SiteName.DoubanRead, SiteName.Heyuedu, SiteName.Qidian);
            Collections.addAll(siteNames, SiteName.BaiduMusic, SiteName.KugouMusic, SiteName.MiguMusic, SiteName.QQMusic);
            Collections.addAll(siteNames, SiteName.Chinaz);

            for(String name: siteNames) {
                loadAndSave(getFileName(name));
            }
        }
        return true;
    }

    private void loadAndSave(String fileName){
        List<TagMapEntity> mapping = loadCSV(fileName);
        if(mapping.isEmpty()){
            return;
        }
        tagMapRepository.deleteBySite(parseSiteName(fileName));
        tagMapRepository.save(mapping);
    }

    private String parseSiteName(String fileName){
        return fileName.substring(fileName.lastIndexOf("/")+1, fileName.lastIndexOf("."));
    }

    private String getFileName(String siteName){
        return "tagmap/" + siteName + ".csv";
    }

    /**
     * Load tag category mapping that configured in csv file.
     * csv file format:
     *      tagName, siteName,categoryName,categoryId
     */
    private List<TagMapEntity> loadCSV(String fileName) {
        List<TagMapEntity> mapping = new ArrayList<>();
        try {
            InputStream in = ConfigFileManager.getResourceFile(fileName);
            if(in == null){
                LOG.warn("Not exists file: "+ fileName);
                return mapping;
            }
            InputStreamReader freader = new InputStreamReader(in, "utf-8");
            ICsvListReader reader = new CsvListReader(freader,
                    CsvPreference.EXCEL_PREFERENCE);
            reader.getHeader(true);
            String site = parseSiteName(fileName);

            List<String> arr;
            while ((arr = reader.read()) != null) {
                int index = 0;
                String tag = arr.get(index++);
                String categoryName = arr.get(index++);
                String categoryCode = null;
                if(arr.size() > 2){
                    categoryCode = arr.get(index);
                }
                updateMap(mapping, tag, site, categoryName, categoryCode);
            }
            reader.close();
        } catch (IOException e) {
            LOG.fatal("failed to get tag-category map", e);
            System.exit(-1);
        }
        return mapping;
    }
    private void updateMap(List<TagMapEntity> mapping, String tag, String site, String name, String code) {
        if (StringUtils.isBlank(name)) {
            return;
        }
        try {
            String separator = "\\|\\|";
            String[] names = name.split(separator);
            String[] codes;
            if(StringUtils.isBlank(code)){
                codes = new String[names.length];
            }else{
                codes = code.split(separator);
            }
            for (int i = 0; i < codes.length; ++i) {
                TagMapEntity rec = new TagMapEntity();
                rec.setCategory(names[i]);
                rec.setCategoryId(codes[i]);
                rec.setSite(site);
                rec.setTag(tag);
                mapping.add(rec);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Build tag map for category: " + tag + ", site: " + site, e);
        }
    }

    public static void main(String[] args){
        TagMapCSVLoad oper = (TagMapCSVLoad) SpringHelper.getAppContext().getBean("tagMapCSVLoad");
        oper.setArgs(args).action();
    }
}
