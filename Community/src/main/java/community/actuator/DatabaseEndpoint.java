package community.actuator;

import community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 @author Alex
 @create 2023-05-04-11:19
 */
@Component
@Endpoint(id = "database")
public class DatabaseEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseEndpoint.class);

    @Autowired
    private DataSource dataSource;

    // 该注解表示这个方法是通过get请求访问的
    @ReadOperation
    public String checkConnection(){
        // try后的括号内的数据，会在finnally中自动关闭
        try (Connection conn = dataSource.getConnection();)
            {
            return CommunityUtil.getJsonString(0,"获取连接成功！");
        } catch (SQLException e) {
            logger.error("获取连接失败:" + e.getMessage());
            return CommunityUtil.getJsonString(1,"获取连接失败！");
        }
    }
}
