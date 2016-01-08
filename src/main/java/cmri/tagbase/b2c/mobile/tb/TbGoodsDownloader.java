package cmri.tagbase.b2c.mobile.tb;

import cmri.etl.proxy.Proxy;
import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.downloader.Cache;
import cmri.etl.downloader.CasperJsDownloader;
import cmri.etl.spider.Spider;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuyin on 3/3/15.
 */
public class TbGoodsDownloader extends CasperJsDownloader {
    private static final Logger LOG = Logger.getLogger(TbGoodsDownloader.class);
    private static final TbGoodsDownloader downloader = new TbGoodsDownloader();
    static {
        enableScroll();
    }
    public static TbGoodsDownloader getInstance(){return downloader;}

    private TbGoodsDownloader() {
        super.setScript("js/tbGoodsScroll.js");
    }

    public static void enableScroll(){
        try {
            List<String> command = new ArrayList<>();
            command.add("casperjs");
            command.add(getJsPath("js/enable.js"));

            Proxy myProxy = cmri.etl.proxy.ProxyHelper.getDefaultProxy();
            if (myProxy != null) {
                String host = myProxy.getHost();
                int port = myProxy.getPort();
                command.add(String.format("--proxy=%s:%d", host, port));
            }
            Process proc = new ProcessBuilder(command).start();
            try {
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                long start = System.currentTimeMillis();
                int timeout = 60000;
                while (stdInput.readLine() != null) {
                    if (System.currentTimeMillis() > start + timeout) {
                        break;
                    }
                }
            }finally {
                proc.destroy();
            }
            LOG.info("enable scroll");
        } catch (IOException e) {
            LOG.error(null, e);
        }
    }

    @Override
    public ResultItems useCache(Request request, Spider spider) {
        ResultItems resultItems = new ResultItems(request, spider);
        Cache cache = new Cache(request, spider);
        if (!request.isIgnoreCache()
                && cache.usable()
                && new java.io.File(getUrlFileName(request)).exists()
                && new java.io.File(getTotalPageFileName(request)).exists()
                ) {
            return resultItems.setResource(request.getUrl()).cacheUsed(true);
        }
        return null;
    }

    /**
     * @return name of the file store url format.
     */
    public static String getUrlFileName(Request request){
        return request.getFilePath()
                .replaceAll(".htm$", ".url");
    }

    /**
     * @return name of the file store total json page count.
     */
    public static String getTotalPageFileName(Request request){
        return request.getFilePath()
                .replaceAll(".htm$", ".pages");
    }
}
