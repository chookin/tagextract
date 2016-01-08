package cmri.tagbase.video.aiqiyi;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.utils.lang.StringHelper;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by zhuyin on 5/14/15.
 */
public class CategoryPageProcessor2 implements PageProcessor {
    private static final Logger LOG = Logger.getLogger(CategoryPageProcessor2.class);

    private static final CategoryPageProcessor2 processor = new CategoryPageProcessor2();

    public static CategoryPageProcessor2 getInstance() {
        return processor;
    }

    public static Request getRequest(CategoryEntity category) {
        return new Request(category.getUrl(),processor)
                .putExtra("category", category)
                ;
    }

    @Override
    public void process(ResultItems page) {
        CategoryEntity parent = page.getRequest().getExtra("category", CategoryEntity.class);
        Document doc = (Document) page.getResource();
        List<CategoryEntity> categories = processItems(doc, parent);
        categories.forEach(page::addItem);
    }

    private List<CategoryEntity> processItems(Element element, CategoryEntity parent) {
        List<CategoryEntity> categories = new ArrayList<>();

        Elements elements = element.select("div.mod_sear_list");
        Set<String> validLabels = Sets.newHashSet("地区", "分类", "年代");
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
                    .setUrl(url)
                    .setParent(parent);
            LOG.trace(category);
            categories.add(category);
        }
        return categories;
    }
}
