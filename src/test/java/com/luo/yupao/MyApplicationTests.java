package com.luo.yupao;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

@SpringBootTest
class MyApplicationTests {

    @Test
    void testDigest() throws Exception {
        String newPassword = DigestUtils.md5DigestAsHex(("abcd"+"mypassword").getBytes());
        System.out.println(newPassword);
    }


}
