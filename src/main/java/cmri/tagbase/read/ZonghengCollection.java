package cmri.tagbase.read;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.SiteName;
import cmri.tagbase.base.CKCollection;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import cmri.utils.lang.StringHelper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhuyin on 3/24/15.
 */
public class ZonghengCollection extends CKCollection {
    @Override
    public String getSiteName() {
        return SiteName.Zongheng;
    }

    @Override
    public Collection<Request> getSeedCategoryRequests() {
        return CategoryPageProcessor.getSeedRequests();
    }

    @Override
    public Request getRequest(CategoryEntity category) {
        return BookPageProcessor.getRequest(category);
    }

    static class CategoryPageProcessor implements PageProcessor {
        private static CategoryPageProcessor processor = new CategoryPageProcessor();
        public static CategoryPageProcessor getInstance(){
            return processor;
        }

        public static Collection<Request> getSeedRequests(){
            Set<Request> requests = new HashSet<>();
            requests.add(new Request("http://book.zongheng.com/store/c0/c0/b0/u0/p1/v9/s9/t0/ALL.html",processor)
                    .setPriority(8));
            requests.add(new Request("http://book.zongheng.com/store/c0/c0/b1/u0/p1/v9/s9/t0/ALL.html",processor)
                    .setPriority(8));
            return requests;
        }
        @Override
        public void process(ResultItems page) {
            Document doc = (Document) page.getResource();
            Elements elements = doc.select("body > div:nth-child(8) > div.two_main.fl > div.bookstore_select > div.select_con > div:nth-child(1) > div:nth-child(5) > a");
            for (Element element : elements) {
                String name = element.text();
                if(name.contains("全部作品")){
                    continue;
                }
                String code = element.attr("categoryid");
                String url = page.getRequest().getUrl().replaceAll("store/c0", "store/c"+code);
                CategoryEntity category = new CategoryEntity().setName(name)
                        .setCode(code)
                        .setUrl(url)
                        .setSite(SiteName.Zongheng);
                getLogger().trace(category);
                page.addTargetRequest(CategoryPageProcessor2.getRequest(category));
                page.addItem(category);
            }
        }
    }

    static class CategoryPageProcessor2 implements PageProcessor {
        static CategoryPageProcessor2 processor = new CategoryPageProcessor2();
        public static Request getRequest(CategoryEntity category){
            return new Request(category.getUrl(),processor)
                    .putExtra("category", category)
                    .setPriority(9);
        }

        @Override
        public void process(ResultItems page) {
            CategoryEntity parent = page.getRequest().getExtra("category", CategoryEntity.class);

            Document doc = (Document) page.getResource();
            Elements elements = doc.select("body > div:nth-child(8) > div.two_main.fl > div.bookstore_select > div.select_con > div:nth-child(1) > div.detail > a");
            for(Element element: elements){
                String name = element.text();
                if(name.contains("全部")){
                    continue;
                }
                String code = element.attr("childcategoryid");
                String url = page.getRequest().getUrl().replaceAll("store/c\\d+/c0", "store/c"+parent.getCode()+"/c"+code);
                CategoryEntity category = new CategoryEntity().setName(name)
                        .setCode(code)
                        .setUrl(url)
                        .setSite(SiteName.Zongheng)
                        .setParent(parent);
                getLogger().trace(category);
                page.addItem(category);
            }
        }
    }

    static class BookDetailPageProcessor implements PageProcessor {
        static BookDetailPageProcessor processor = new BookDetailPageProcessor();

        public static Request getRequest(KindEntity book){
            return new Request(book.getUrl(), processor)
                    .setPriority(8)
                    .putExtra("bookEntity", book);
        }

        @Override
        public void process(ResultItems page) {
            KindEntity book = page.getRequest().getExtra("bookEntity", KindEntity.class);
            Document doc = (Document) page.getResource();

            Element element = doc.select(".author a").first();
            if (element != null) {
                String  author = element.text().trim();
                book.set("author", author);
            }

            element = doc.select("div.booksub > span:nth-child(7)").first();
            if(element != null){
                book.set("readCount", Integer.valueOf(element.text())); // 点击
            }

            element = doc.select(".vote_info").first();
            if (element != null) {
                String txt = element.text();
                /*
                共被推荐7299716次
                好评率98%
                */
                String regex = "推荐(\\d+)次[\\s\\S]+评率(\\d+)%";
                book.set("scoreNum", Integer.valueOf(StringHelper.parseRegex(txt, regex, 1))); // 评论数
                book.set("scoreValue", Double.valueOf(StringHelper.parseRegex(txt, regex, 2))); // 评分
            }

            Elements elements = doc.select("div.keyword > a");
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

    static class BookPageProcessor implements PageProcessor {
        static final BookPageProcessor processor = new BookPageProcessor();

        public static Request getRequest(CategoryEntity category){
            return new Request(category.getUrl(), processor)
                    .putExtra("categoryEntity", category);
        }

        @Override
        public void process(ResultItems page) {
            CategoryEntity category = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);
            Document doc = (Document) page.getResource();
            Elements elements = doc.select("body > div:nth-child(8) > div.two_main.fl > div.booklist_result.new_chapter.box > div.tabpanel.tabpane2 > div > ul > li > span.chap > a.fs14");
            for (Element element : elements) {
                // <a class="fs14" href="http://book.zongheng.com/book/448220.html" title="天行轶事" target="_blank">天行轶事</a>
                String name = element.attr("title");
                String url = element.absUrl("href");
                String code = StringHelper.parseRegex(url, "book/([a-zA-Z\\d]+).html", 1);
                KindEntity book = new KindEntity("book").setName(name)
                        .setUrl(url)
                        .setCode(code)
                        .setCategory(category);
                getLogger().trace(book);
                page.addTargetRequest(BookDetailPageProcessor.getRequest(book));
            }
            addNewRequest(page);
        }
        void addNewRequest(ResultItems page){
            if(!page.getRequest().getUrl().contains("/p1/")){// if not the first page
                return;
            }

            CategoryEntity category = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);
            Document doc = (Document) page.getResource();
            Integer pageNum = getPageNum(doc);
            String originUrl = page.getRequest().getUrl();
            if(pageNum != null){
                for(int index = 2; index<= pageNum; ++index){
                    // http://book.zongheng.com/store/c15/c1157/b0/u0/p1/v9/s9/t0/ALL.html
                    String url = originUrl.replaceAll("/p1/", "/p"+index+"/");
                    page.addTargetRequest(new Request(url, processor)
                            .putExtra("categoryEntity", category));
                }
            }
        }
        Integer getPageNum(Document doc){
            Element element = doc.select(".pagenumber").first();
            if(element == null){
                getLogger().warn("fail to parse page num of " + doc.baseUri());
                return null;
            }
            try {
                return Integer.parseInt(element.attr("count"));
            }catch (NumberFormatException e){
                throw new RuntimeException(doc.baseUri(), e);
            }
        }
    }
}
