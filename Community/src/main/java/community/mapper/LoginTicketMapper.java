package community.mapper;

import community.pojo.LoginTicket;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 @author Alex
 @create 2023-04-05-19:47
 */
@Mapper
@Deprecated
public interface LoginTicketMapper {
    /**
     * 插入凭证
     * @param loginTicket
     * @return
     */
    int insertLoginTicket(LoginTicket loginTicket);

    /**
     * 根据ticket查找凭证
     * @param ticket
     * @return
     */
    LoginTicket selectByTicket(String ticket);

    /**
     * 修改当前凭证状态（是否过期）
     * @param ticket
     * @return
     */
    int updateStatus(String ticket,Integer status);

    /**
     * 根据userId查找用户的ticket
     * @param
     * @return
     */
    List<String> getTicketByUserId(int userId);
}
