package community.util;

/**
 @author Alex
 @create 2023-04-11-14:40
 */

/**
 * 用来生成redis key的工具类
 */
public class RedisKeyUtil {
    private static final String SPLIT=":";
    // 前缀，实体的赞
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    // 存储用户收到的赞
    private static final String PREFIX_USER_LIKE="like:user";
    // 被关注者前缀
    private static final String PREFEX_FOLLOWEE = "followee";
    // 关注者前缀
    private static final String PREFIX_FOLLOWER = "follower";
    // 验证码前缀
    private static final String PREFIX_KAPTCHA = "kaptcha";
    // 用户登录凭证前缀
    private static final String PREFIX_TICKET = "ticket";
    // 用户缓存信息前缀
    private static final String PREFIX_USER = "user";
    // 独立访客
    private static final String PREFIX_UV = "uv";
    // 日活跃用户
    private static final String PREFIX_DAU = "dau";
    // 计算分数，将分数变化的帖子存储到redis中的前缀
    private static final String PREFIX_POST = "post";

    // 生成某个 实体的赞 的key 如： like:entity:entityType --> entityId
    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 生成某个 用户的赞 的key。如： like:user --> userId
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE+SPLIT+userId;
    }

    // 生成某个用户 关注实体 的key（你的关注），即当前用户:关注的类型:关注实体id。如： followee:userId:entityType --> entityId
    public static String getFolloweeKey(int userId,int entityType) {
        return PREFEX_FOLLOWEE+SPLIT+userId+SPLIT+entityType;
    }

    // 生成实体 拥有粉丝 的key。即实体类型:实体Id:关注者的Id。如： follower:entityType:entityId --> userId
    public static String getFollowerKey(int entityType,int entityId) {
        return PREFIX_FOLLOWER+SPLIT+entityType+SPLIT+entityId;
    }

    // 生成登录验证码 的key（不同的用户登陆时是有着不同的验证码的，但是这时用户还未登录，怎么标记用户呢，可以在用户点开登陆页面时，给用户发送一个临时凭证owner用来标记用户，为owner设置一个自动过期就好了）
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA+SPLIT+owner;
    }

    // 生成用户登录凭证 的key
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET+SPLIT+ticket;
    }

    // 生成用户缓存信息 的key
    public static String getUserKey(Integer userId){
        return PREFIX_USER+SPLIT+userId;
    }

    // 单日UV
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 区间UV
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 单日活跃用户
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    // 区间活跃用户
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    // 帖子分数
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }
}
