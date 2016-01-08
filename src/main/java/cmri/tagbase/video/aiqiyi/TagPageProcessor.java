package cmri.tagbase.video.aiqiyi;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.SiteName;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.utils.lang.StringHelper;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

/**
 * Created by zhuyin on 5/13/15.
 */
public class TagPageProcessor implements PageProcessor {
    private static final TagPageProcessor processor = new TagPageProcessor();

    public static TagPageProcessor getInstance(){return processor;}

    public static Collection<Request> getSeedRequests(){
        return Collections.singletonList(new Request("http://list.iqiyi.com/www/2/-------------4-1-1-iqiyi--.html", processor)
        );
    }

    static Request getRequest(CategoryEntity category) {
        return new Request(category.getUrl(),processor)
                .putExtra("category", category)
                ;
    }

    @Override
    public void process(ResultItems page) {
        if(page.getRequest().getExtra("category") == null){
            processLevel1(page);
        }else{
            processLevel2(page);
        }
    }

    private void processLevel1(ResultItems page){
        List<CategoryEntity> categories = processItems((Document) page.getResource(), null, Sets.newHashSet("频道"));
        categories.forEach(page::addItem);
        for (CategoryEntity category : categories) {
            page.addTargetRequest(getRequest(category));
        }
    }

    private void processLevel2(ResultItems page){
        CategoryEntity parent = page.getRequest().getExtra("category", CategoryEntity.class);
        List<CategoryEntity> categories = processItems((Document) page.getResource(), parent, Sets.newHashSet("地区", "分类", "年代"));
        categories.forEach(page::addItem);
    }

    private List<CategoryEntity> processItems(Element element, CategoryEntity parent, Set<String> validLabels) {
        List<CategoryEntity> categories = new ArrayList<>();

        Elements elements = element.select("div.mod_sear_list");
        for (Element item : elements) {
            Element eLabel = item.select("h3").first();
            if (eLabel == null) {
                continue;
            }
            String label = eLabel.text().replace("：", "");
            if (!validLabels.contains(label)) {
                continue;
            }
            List<CategoryEntity> subCategories = processItem(item, parent);
            categories.addAll(subCategories);
        }
        return categories;
    }

    private List<CategoryEntity> processItem(Element element, CategoryEntity parent) {
        List<CategoryEntity> categories = new ArrayList<>();
        Elements elements = element.select(".mod_category_item > li > a");
        for (Element item : elements) {
            // <a href="#">全部</a>
            // <a href="/www/2/15-------------11-1-1-iqiyi--.html">内地</a>
            String name = item.text();
            if (name.equals("全部")) {
                continue;
            }
            String url = item.absUrl("href");
            if (StringUtils.isEmpty(url)){
                continue;
            }
            String code = StringHelper.parseRegex(url, "www/([\\w\\W]+).html", 1);
            CategoryEntity category = new CategoryEntity().setName(name)
                    .setCode(code)
                    .setUrl(url);
            if(parent ==null){
                category.setSite(SiteName.Aiqiyi);
            }else {
                category.setParent(parent);
            }
            getLogger().trace(category);
            categories.add(category);
        }
        return categories;
    }
}
