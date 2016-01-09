package cmri.tagbase.read;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.SiteName;
import cmri.tagbase.base.CKCollection;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.lang.BaseOper;
import cmri.utils.lang.StringHelper;
import cmri.utils.lang.TimeHelper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

/**
 * Created by zhuyin on 8/24/15.
 */
public class BaiduYueduCollection extends CKCollection {
    @Override
    public String getSiteName() {
        return SiteName.BaiduRead;
    }

    @Override
    public Collection<Request> getSeedCategoryRequests() {
        return CategoryPageProcessor.getSeedRequests();
    }

    @Override
    public Request getRequest(CategoryEntity category) {
        return BookPageProcessor.getRequest(category);
    }

    public static void main(String[] args){
        new BaseOper() {
            @Override
            public boolean action() {
                new BaiduYueduCollection().init(getOptions().options())
                        .start();
                return true;
            }
        }.setArgs(args).action();
    }

    static class CategoryPageProcessor implements PageProcessor {
        private static final CategoryPageProcessor processor = new CategoryPageProcessor();

        public static Collection<Request> getSeedRequests() {
            return Collections.singletonList(new Request("http://yuedu.baidu.com/book/list/0", processor)
            );
        }

        @Override
        public void process(ResultItems page) {
            Document doc = (Document) page.getResource();
            Elements elements = doc.select("#all-category > div > div.cate-menu-box.cate-menu-box-more");
            for (Element item : elements) {
                Elements level1 = item.select("div.tab-cate > div > a");
                CategoryEntity ancestor = new CategoryEntity()
                        .setName(level1.text())
                        .setSite(SiteName.BaiduRead)
                        .setUrl(level1.first().absUrl("href"));
                updateCode(ancestor);
                getLogger().trace(ancestor);
                page.addItem(ancestor);

                Elements level2 = item.select("div.popup-cate > ul > li > a");
                for (Element sub : level2) {

                    CategoryEntity category = new CategoryEntity()
                            .setName(sub.text())
                            .setUrl(sub.absUrl("href"))
                            .setParent(ancestor);
                    updateCode(category);
                    getLogger().trace(category);
                    page.addItem(category);
                }
            }
        }

        private void updateCode(CategoryEntity categoryEntity) {
            String url = categoryEntity.getUrl();
            categoryEntity.setCode(url.substring(url.lastIndexOf("/") + 1));
        }
    }

    static class BookPageProcessor implements PageProcessor {
        private static final BookPageProcessor processor = new BookPageProcessor();

        public static Request getRequest(CategoryEntity category) {
            return new Request(category.getUrl(), processor)
                    .putExtra("categoryEntity", category)
                    ;
        }

        @Override
        public void process(ResultItems page) {
            CategoryEntity categoryEntity = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);
            Document doc = (Document) page.getResource();

            List<KindEntity> books = new ArrayList<>();
            Elements elements = doc.select("#bd > div.bd-wrap > div > div.main > div.bd > div.booklist > div > div > a.al.title-link");
            for (Element element : elements) {
                String name = element.text();
                String url = element.absUrl("href");

                KindEntity book = new KindEntity("book")
                        .setName(name)
                        .setUrl(url)
                        .setCategory(categoryEntity);
                updateCode(book);
                getLogger().trace(book.toString());
                books.add(book);

                page.addTargetRequest(BookDetailPageProcessor.getRequest(book));
            }

            String next = getNextPageUrl(doc);
            if (next != null) {
                Request request = new Request(next, processor)
                        .setPriority(1)
                        .putExtra("categoryEntity", categoryEntity);
                page.addTargetRequest(request);
            }
            if (books.isEmpty() && next == null) { // If web page is empty, then we can guess this request may be declined by the web server because of frequent access.
                page.addTargetRequest(page.getRequest())
                        .skip(true);
                page.getSpider().onError(page.getRequest());
            }
        }

        private void updateCode(KindEntity book) {
            book.setCode(StringHelper.parseRegex(book.getUrl(), "ebook/([a-zA-Z\\d]+)\\?", 1));
        }

        private String getNextPageUrl(Document doc) {
            Element element = doc.select("#pager > div > div > div > a.next").first();
            if (element != null) {
                String url = element.absUrl("href");
                if (!url.trim().isEmpty()) {
                    return url;
                }
            }
            return null;
        }
    }

    static class BookDetailPageProcessor implements PageProcessor{
        static final BookDetailPageProcessor processor = new BookDetailPageProcessor();

        public static Request getRequest(KindEntity book) {
            return new Request(book.getUrl(), processor)
                    .setPriority(8)
                    .putExtra("bookEntity", book)
                    ;
        }

        @Override
        public void process(ResultItems page) {
            KindEntity book = page.getRequest().getExtra("bookEntity", KindEntity.class);
            Document doc = (Document) page.getResource();

            Element element = doc.select("ul > li.doc-info-field.doc-info-author > a").first();
            String author;
            if (element != null) {
                author = element.text().trim();
            } else {
                author = doc.select("a.author-head-name").text();
            }
            book.set("author", author);

            element = doc.select("div.doc-info-score > span.doc-info-score-value").first();
            if (element != null) {
                book.set("scoreValue", Double.valueOf(element.text()) * 10); // 评分 8.8 (31人评论) | 451038人在读
            }

            element = doc.select("div.doc-info-score > a > span").first();
            if (element != null) {
                book.set("scoreNum", Integer.valueOf(element.text())); // 评论数
            }

            element = doc.select("div.doc-info-score > span.doc-info-read-count").first();
            if (element != null) {
                String readCount = element.text();
                readCount = readCount.substring(0, readCount.indexOf("人"));
                book.set("readCount", Integer.valueOf(readCount)); // 在读人数
            }

            Elements elements = doc.select("ul > li.box.doc-info-field.doc-info-tags > div > a");
            Set<String> keywords = new HashSet<>();
            for (Element item : elements) {
                String word = item.text().trim();
                keywords.add(word);
            }
            if (!keywords.isEmpty())
                book.set("keywords", keywords);

            element = doc.select("#book-des > div.bd.scaling-content-wp > div > p").first();
            if (element != null) {
                book.set("bookIntro", element.text().trim()); // 图书简介
            }

            element = doc.select("div.mod.editor-recommend.book-intro-block.mb20 > div.bd.scaling-content-wp > div > p").first();
            if (element != null) {
                book.set("editorRecommend", element.text().trim()); // 编辑推荐
            }
            element = doc.select("div.mod.about-author.book-intro-block.mb20 > div.bd > p").first();
            if (element != null) {
                book.set("aboutAuthor", element.text().trim()); // 作者简介
            }

            String text = doc.text();
            String value = StringHelper.parseRegex(text, "出版时间[\\s\\S]*(\\d{4}-\\d{2}-\\d{2})", 1);
            if (value != null) {
                book.set("publicationTime", TimeHelper.parseDate(value)); // 出版时间
            }
            value = StringHelper.parseRegex(text, "定价[\\D]*([0-9]+\\.[0-9]+)元", 1);
            if (value != null) {
                book.set("price", Double.valueOf(value)); // 纸书定价,元
            }
            page.addItem(book);
        }
    }
}
