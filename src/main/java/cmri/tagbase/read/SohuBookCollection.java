package cmri.tagbase.read;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.SiteName;
import cmri.tagbase.base.CKCollection;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.lang.StringHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhuyin on 7/14/15.
 */
public class SohuBookCollection extends CKCollection {
    @Override
    public String getSiteName() {
        return SiteName.SohuBook;
    }

    @Override
    public Request getRequest(CategoryEntity category) {
        return BookPageProcessor.getRequest(category);
    }

    @Override
    public Collection<Request> getSeedCategoryRequests() {
        return TagPageProcessor.getSeedRequests();
    }

    static class TagPageProcessor implements PageProcessor {
        public static Collection<Request> getSeedRequests(){
            return Collections.singletonList(new Request("http://lz.book.sohu.com/", new TagPageProcessor()));
        }

        @Override
        public void process(ResultItems page) {
            Document doc = (Document) page.getResource();
            Elements elements = doc.select("a[href~=^http://lz\\.book\\.sohu\\.com\\?cs=[\\d]+$]");
            for(Element element: elements){
                String url = element.absUrl("href");
                CategoryEntity category = new CategoryEntity().setSite(SiteName.SohuBook)
                        .setName(element.text())
                        .setUrl(url)
                        .setCode(StringHelper.parseRegex(url, "cs=([\\d]+)", 1))
                        ;
                getLogger().trace(category);
                page.addItem(category);
            }
        }
    }

    static class BookPageProcessor implements PageProcessor {
        private static final BookPageProcessor processor = new BookPageProcessor();

        public static Request getRequest(CategoryEntity category){
            return getRequest(category, 1);
        }

        private static Request getRequest(CategoryEntity category, int pageIndex){
            String url = String.format("http://lz.book.sohu.com/Search/getDataInfo?cs=%s&page=%s", category.getCode(), pageIndex);
            return new Request(url, processor)
                    .putExtra("categoryEntity", category)
                    .putExtra("pageIdx", pageIndex)
                    .setTarget(Request.TargetResource.Json)
                    ;
        }

        @Override
        public void process(ResultItems page) {
            String txt = (String) page.getResource();
            if(StringUtils.isBlank(txt)){
                return;
            }
            JsonObject ojson = new JsonParser().parse(txt).getAsJsonObject();
            CategoryEntity category = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);
            JsonArray ajson = ojson.get("data").getAsJsonArray();
            for (JsonElement e : ajson) {
                JsonObject entity = e.getAsJsonObject();
                String name = entity.get("book_name").getAsString();
                String strId = entity.get("id").getAsString();
                String url = String.format("http://lz.book.sohu.com/book-%s.html", strId);
                int viewCount = entity.get("view_count").getAsInt();
                double score_total = entity.get("score_total").getAsDouble();
                String desc = entity.get("brief").getAsString();
                desc = StringEscapeUtils.unescapeHtml4(desc);
                JsonArray keywordsArr = entity.get("keywords").getAsJsonArray();
                Set<String> keywords = new HashSet<>();
                for (JsonElement item: keywordsArr) {
                    String word = item.getAsString();
                    if(StringUtils.isBlank(word)){
                        continue;
                    }
                    keywords.add(word.trim());
                }

                KindEntity book = new KindEntity("book")
                        .setName(name)
                        .setCategory(category)
                        .setCode(strId)
                        .setUrl(url)
                        .set("viewCount", viewCount)
                        .set("scoreValue", score_total * 10)
                        .set("desc", desc)
                        ;
                if(!keywords.isEmpty())
                    book.set("keywords", keywords);
                getLogger().trace(book);
                page.addItem(book);
            }

            int pageIdx = page.getRequest().getExtra("pageIdx", Integer.class);
            if(pageIdx == 1) {
                int total = getTotalPageNum(ojson);
                for (int i = 2; i <= total; ++i) {
                    page.addTargetRequest(getRequest(category, i));
                }
            }
        }

        private int getTotalPageNum(JsonObject doc){
            return doc.get("page_count").getAsInt();
        }
    }
}
