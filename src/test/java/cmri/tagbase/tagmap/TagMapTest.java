package cmri.tagbase.tagmap;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;
import java.util.Set;

/**
 * Created by zhuyin on 6/16/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/applicationContext.xml"})
public class TagMapTest {

    @Test
    public void testGetCategoriesMap() throws Exception {
        Set<String> sites = Sets.newHashSet("youku");
        Map<String, Map<CategoryIdent, Set<String>>> map = TagMap.getInstance().getCategoriesMap(sites);
        System.out.println(map);
    }
}