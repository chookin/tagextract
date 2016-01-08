package cmri.tagbase.b2c.mobile.tb;

import cmri.utils.web.NetworkHelper;
import cmri.etl.common.Request;
import cmri.etl.spider.SpiderAdapter;
import cmri.utils.configuration.ConfigManager;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TbGoodsDownloaderTest {
    @Before
    public void setUp(){
        NetworkHelper.setDefaultProxy();
    }

    @Test
    public void testDownload() throws Exception {
        NetworkHelper.setDefaultProxy();
        String url = "http://s.m.taobao.com/search.htm?q=白酒  &iwRet=true&spm=41.139785.167731.24";
        try {
            Request request = new Request().setUrl(url);
            TbGoodsDownloader downloader = TbGoodsDownloader.getInstance();
            new java.io.File(request.getFilePath()).delete();
            new java.io.File(TbGoodsDownloader.getTotalPageFileName(request)).delete();
            new java.io.File(TbGoodsDownloader.getUrlFileName(request)).delete();
            new java.io.File(downloader.getScript()).delete();
            downloader.download(
                    request,
                    new SpiderAdapter().userAgent(ConfigManager.get("mobile.userAgent"))
            );
            assertEquals(true, new java.io.File(TbGoodsDownloader.getTotalPageFileName(request)).exists());
            assertEquals(true, new java.io.File(TbGoodsDownloader.getUrlFileName(request)).exists());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetUrlFileName() throws Exception {
        String url = "http://s.m.taobao.com/search.htm?q=%E5%AD%95%E5%A6%87%E5%A5%B6%E7%B2%89&spm=41.139785.167729.2";
        String fileName = TbGoodsDownloader.getUrlFileName(new Request().setUrl(url));
        System.out.println(fileName);
        Assert.assertEquals(true, fileName.endsWith("s.m.taobao.com/258cb82d61958265b9a6505adc532596.url"));
    }

    @Test
    public void testGetTotalPageFileName() throws Exception {
        String url = "http://s.m.taobao.com/search.htm?q=%E5%AD%95%E5%A6%87%E5%A5%B6%E7%B2%89&spm=41.139785.167729.2";
        String fileName = TbGoodsDownloader.getTotalPageFileName(new Request().setUrl(url));
        System.out.println(fileName);
        Assert.assertEquals(true, fileName.endsWith("s.m.taobao.com/258cb82d61958265b9a6505adc532596.pages"));
    }
}