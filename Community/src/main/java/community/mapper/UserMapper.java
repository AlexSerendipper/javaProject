package community.mapper;

import community.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 @author Alex
 @create 2023-04-03-20:14
 */
@Mapper
public interface UserMapper {
   /**
    * 根据用户id查询用户
     * @param id
    * @return
    */
   User getUserById(Integer id);

   /**
    * 根据用户名查询用户
    * @param username
    * @return
    */
   User getUserByName(String username);

   /**
    * 根据邮箱查询用户
    * @param email
    * @return
    */
   User getUserByEmail(String email);

   /**
    * 插入用户
    * @param user
    * @return
    */
   int insertUser(User user);

   int updateStatus(int id, int status);

   int updateHeader(int id, String headerUrl);

   int updatePassword(int id, String password);
}
