package cmri.tagbase.video.heshijie;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import com.google.common.collect.Sets;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by zhuyin on 6/5/15.
 */
class CategoryPageProcessor2 implements PageProcessor {
    private static final CategoryPageProcessor2 processor = new CategoryPageProcessor2();

    public static CategoryPageProcessor2 getInstance() {
        return processor;
    }

    public static Request getRequest(CategoryEntity category) {
        return new Request(category.getUrl(), processor)
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

        Elements elements = element.select(".select-area");
        Set<String> validLabels = Sets.newHashSet("按地区", "按类别", "按年份");
        for (Element item : elements) {
            Element eLabel = item.select("h4").first();
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
        Elements elements = element.select("a");
        for (Element item : elements) {
            // <a href="/pc/list/ScreeningPage.jsp?n=1000&amp;type=战争">战争</a>
            String name = item.text();
            String url = item.absUrl("href");
            CategoryEntity category = new CategoryEntity().setName(name)
                    .setUrl(url)
                    .setCode(parent.getCode()) // must has a code because of duplicate names.
                    .setParent(parent);
            getLogger().trace(category);
            categories.add(category);
        }
        return categories;
    }
}
