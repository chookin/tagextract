package cmri.tagbase.game.heyouxi;

import cmri.etl.common.Request;
import cmri.etl.common.ResultItems;
import cmri.etl.processor.PageProcessor;
import cmri.tagbase.orm.domain.CategoryEntity;
import cmri.tagbase.orm.domain.KindEntity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuyin on 6/10/15.
 */
public class GamePageProcessor implements PageProcessor {
    static final GamePageProcessor processor = new GamePageProcessor();
    static int pageline = 1000; // 每次请求最多获取的游戏数

    public static Request getRequest(CategoryEntity category){
        return getRequest(category, 0);
    }

    /**
     * 生成用于下载web资源的请求
     * @param category 该资源所属的分类
     * @param startNum 该次请求从第几个游戏开始获取
     * @return 生成的request
     */
    static Request getRequest(CategoryEntity category, int startNum){
        return new Request("http://g.10086.cn/a/public/showNextPage", processor)
                .putExtra("data", String.format("classid=392&pageline=%d&startNum=%d&orderby=newstime desc&spm=a.list.3adj.djclasslistgamet&type=%s&sctypeid=",pageline, startNum, category.getCode())) // cannot use '+' to replace the inner ' '
                .putExtra("header", "X-Requested-With: XMLHttpRequest")
                .putExtra("method", "POST") // it's vital
                .putExtra("categoryEntity", category)
                .putExtra("startNum", startNum)
                .setTarget(Request.TargetResource.Json)
                .ignoreCache(true)
                ;
    }

    @Override
    public void process(ResultItems page) {
        List<KindEntity> games = new ArrayList<>();

        String str = (String) page.getResource();
        CategoryEntity category = page.getRequest().getExtra("categoryEntity", CategoryEntity.class);
        JsonObject ojson = new JsonParser().parse(str).getAsJsonObject();
        JsonElement data = ojson.get("data");
        if (data == null) {
            getLogger().info("Get 0 game for " + page.getRequest());
            return;
        }
        if(data.isJsonArray()) {
            JsonArray listJson = data.getAsJsonArray();
            for (JsonElement e : listJson) {
                addGame(games, e, category);
            }
        }else if(data.isJsonObject()){
            for(Map.Entry<String, JsonElement> entry : data.getAsJsonObject().entrySet()){
                addGame(games, entry.getValue(), category);
            }
        }
        int startNum = (int) page.getRequest().getExtra("startNum");
        getLogger().info("get " + games.size() + " of " + category + " with startNum="+startNum);
        games.forEach(page::addItem);

        page.addTargetRequest(getRequest(category, startNum + pageline));
    }

    void addGame(Collection<KindEntity> games, JsonElement e, CategoryEntity category){
        KindEntity game = parseGame(e, category);
        if(game == null){
            return;
        }
        getLogger().trace(game);
        games.add(game);
    }

    KindEntity parseGame(JsonElement e, CategoryEntity category){
        JsonObject jGame = e.getAsJsonObject();

        String name = jGame.get("title").getAsString();
        name = StringEscapeUtils.unescapeJava(name);
        String strId = jGame.get("dServiceId").getAsString();

        KindEntity game = new KindEntity("game")
                .setName(name)
                .setCategory(category)
                .setCode(strId);
        if (jGame.has("url")) {
            String url = jGame.get("url").getAsString().replace("\\", "");
            url = "http://g.10086.cn" + url;
            game.setUrl(url);
        } else {
            getLogger().warn("fail to get url for " + game);
            return null;
        }
        if (jGame.has("totaldown")) {
            JsonElement o = jGame.get("totaldown");
            if (!o.isJsonNull()) {
                int downloadTimes = o.getAsInt();
                game.set("downloadTimes", downloadTimes);
            }
        }
        return game;
    }
}
