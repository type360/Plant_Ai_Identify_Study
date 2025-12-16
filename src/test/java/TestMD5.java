import cn.hutool.core.util.RandomUtil;
import com.briup.pai.PlantAiIdentifyApplication;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Auther: vanse(lc)
 * @Date: 2025/11/5-11-05-下午2:39
 * @Description：
 */
@SpringBootTest(classes = PlantAiIdentifyApplication.class)
public class TestMD5 {
    @Test
    public void testMD5() {
        String s = DigestUtils.md5Hex("briup");
        // 5fa4d6fc78072f42e0b9817d310bcd35
        // 5fa4d6fc78072f42e0b9817d310bcd35
        System.out.println("s = " + s);
    }

    @Test
    public void testRandom() {
        int i = RandomUtil.randomInt(1000,9999);
        System.out.println("i = " + i);
    }
}
