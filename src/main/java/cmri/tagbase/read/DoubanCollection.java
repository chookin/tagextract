package cmri.tagbase.read;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.SiteName;
import cmri.tagbase.base.CKCollection;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.lang.TimeHelper;
import cmri.utils.lang.StringHelper;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhuyin on 9/9/15.
 */
public class DoubanCollection extends CKCollection {
    @Override
    public String getSiteName() {
        return SiteName.DoubanRead;
    }

    @Override
    public Request getRequest(CategoryEntity category) {
        return BookPageProcessor.getRequest(category);
    }

    @Override
    public Collection<Request> getSeedCategoryRequests() {
        return CategoryPageProcessor.getSeedRequests();
    }

    static class CategoryPageProcessor implements PageProcessor {
        private static final CategoryPageProcessor processor = new CategoryPageProcessor();

        public static Collection<Request> getSeedRequests() {
            return Collections.singletonList(new Request("http://book.douban.com/tag/?view=type", processor)
            );
        }

        @Override
        public void process(ResultItems page) {
            Document doc = (Document) page.getResource();
            Elements elements = doc.select(".tagCol td a");
            for (Element element : elements) {
                String name = element.text();
                String url = element.absUrl("href");
                CategoryEntity category = new CategoryEntity().setName(name)
                        .setUrl(url)
                        .setSite(SiteName.DoubanRead);
                getLogger().trace(category);
                page.addItem(category);
            }
        }
    }

    static class BookPageProcessor implements PageProcessor {
        private static final BookPageProcessor processor = new BookPageProcessor();

        public static Request getRequest(CategoryEntity category){
            return new Request(category.getUrl(), processor)
                    .putExtra("categoryEntity", category)
                    ;
        }
        @Override
        public void process(ResultItems page) {
            CategoryEntity category = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);

            Document doc = (Document) page.getResource();
            Elements elements = doc.select("#subject_list > ul > li > div.info");
            for (Element element : elements) {
                KindEntity book = new KindEntity("book").setCategory(category);
                Element item = element.select("h2 a").first();
                if(item != null){
                    String name = item.attr("title");
                    String url = item.absUrl("href");
                    String code = StringHelper.parseRegex(url, "subject/(\\d+)", 1);
                    book.setName(name)
                            .setUrl(url)
                            .setCode(code);
                    getLogger().trace(book);
                    page.addTargetRequest(BookDetailPageProcessor.getRequest(book));
                }
            }
            addNewRequest(page);
        }
        void addNewRequest(ResultItems page) {
            Document doc = (Document) page.getResource();
            Element item = doc.select("#subject_list > div.paginator > span.next > a").first();
            if(item == null){
                return;
            }
            String url = item.absUrl("href");
            CategoryEntity category = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);
            page.addTargetRequest(new Request(url, processor)
                            .putExtra("categoryEntity", category)
            );
        }
    }

    static class BookDetailPageProcessor implements PageProcessor {
        static final BookDetailPageProcessor processor = new BookDetailPageProcessor();

        public static Request getRequest(KindEntity book){
            return new Request(book.getUrl(), processor)
                    .setPriority(8)
                    .putExtra("bookEntity", book)
                    ;
        }

        @Override
        public void process(ResultItems page) {
            KindEntity book = page.getRequest().getExtra("bookEntity", KindEntity.class);
            Document doc = (Document) page.getResource();

            Set<String> names = new HashSet<>();
            Elements elements = doc.select("#info > span:nth-child(1) > a");
            for(Element element: elements){
                String name = element.text().trim();
                names.add(name);
            }
            if (!names.isEmpty())
                book.set("author", names);

            Element element = doc.select(".rating_num").first();
            if(element != null){
                String scoreValue = element.text();
                if(StringUtils.isNotBlank(scoreValue))
                    book.set("scoreValue", Double.valueOf(element.text())* 10 );
            }
            element = doc.select("p.rating_self.font_normal.clearbox > span > a > span").first();
            if (element != null) {
                book.set("scoreNum", Integer.valueOf(element.text())); // 评论数
            }

            String text = doc.text();
            String value = StringHelper.parseRegex(text, "出版年[\\s\\S]*(\\d{4})", 1);
            if(value != null)
                book.set("publicationTime", TimeHelper.parseDate(value, "yyyy")); // 出版时间

            String aboutAuthor = StringHelper.parseRegex(text, "作者简介([\\s\\S]+)目录", 1);
            if(aboutAuthor != null) {
                aboutAuthor = StringUtils.strip(aboutAuthor).replace("(展开全部)", "").replace("· · · · · ·", "");
                book.set("aboutAuthor", aboutAuthor); // 作者简介
            }
            String bookIntro = StringHelper.parseRegex(text, "内容简介([\\s\\S]+)作者简介", 1);
            if(bookIntro == null){
                bookIntro = StringHelper.parseRegex(text, "内容简介([\\s\\S]+)目录", 1);
            }
            if(bookIntro != null){
                bookIntro = StringUtils.strip(bookIntro).replace("(展开全部)", "").replace("· · · · · ·", "");
                book.set("bookIntro", bookIntro);
            }

            elements = doc.select("#db-tags-section > div a");
            Set<String> keywords = new HashSet<>();
            for (Element item : elements) {
                String word = item.text().trim();
                keywords.add(word);
            }
            if(!keywords.isEmpty())
                book.set("keywords", keywords);
            page.addItem(book);
        }
    }
}
