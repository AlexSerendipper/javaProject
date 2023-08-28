package community.boot;

import java.io.IOException;

/**
 @author Alex
 @create 2023-04-27-16:02
 */
public class WkTest {
    public static void main(String[] args) throws IOException {
        String cmd = "d:/wkhtmltopdf/bin/wkhtmltoimage --quality 75 http://www.nowcoder.com d:/data/wk-pdf/1.png";
        Runtime.getRuntime().exec(cmd);
        System.out.println("ok");
    }
}
