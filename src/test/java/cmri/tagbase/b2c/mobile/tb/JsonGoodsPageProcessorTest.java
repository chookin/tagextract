package cmri.tagbase.b2c.mobile.tb;

import cmri.utils.web.NetworkHelper;
import cmri.utils.configuration.ConfigManager;
import org.junit.Before;
import org.junit.Test;

public class JsonGoodsPageProcessorTest {
    @Before
    public void setUp(){
        NetworkHelper.setDefaultProxy();
    }
    @Test
    public void testGetRedirectUrl() throws Exception {
        String url = "http://mclick.simba.taobao.com/cc_im?p=%B7%A2%CA%CE&s=605740464&k=313&e=FsGdj7Hkj5FYtYHqq2%2BS2xtypZYcmcJ8s1g2I3XkzIA9Zv24h0i9TO7b1liV05Qb9uf8ikBK3Si6EEdq%2FrSpmoXQUP4RPTN18BcmGiefeVMecyY9V2Bsda%2Bemhvh19A1sm%2FaJnueynRbV%2FIDyfW8Cq8Ef5Vzu5PeqNuPiJD6jdKBmlrlXMdEnRIwTF%2B0eTHAPcyP%2FhOYI%2BMpd%2B%2FK65ivKEqNGFqlxeqCkc06xwGfU85sdEDcMF7VXo5nkQ%2FSr7oJL6OohRhgo05UMXV0QAmLvemo9j6HcFdiJ06urZHjeQkx4OReT5d%2FWA%3D%3D";
        String out = JsonGoodsPageProcessor.getInstance().getRedirectUrl(url, ConfigManager.get("mobile.userAgent"));
        System.out.println(out);
    }
}