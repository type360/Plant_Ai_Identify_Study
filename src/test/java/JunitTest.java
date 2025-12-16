import com.briup.pai.common.utils.JwtUtil;
import com.briup.pai.common.utils.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.TestComponent;

/**
 * @author 86151
 * @program: Plant_Ai_Identify_Study
 * @description
 * @create 2025/12/16 11:12
 **/


public class JunitTest {
    @Test
    public void test() {
        SecurityUtil securityUtil = new SecurityUtil();
        Integer userId = securityUtil.getUserId();
        System.out.println("555555555555555"+userId);
        JwtUtil jwtUtil = new JwtUtil();

    }

}
