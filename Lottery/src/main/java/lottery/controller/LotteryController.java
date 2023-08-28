package lottery.controller;

import com.sun.org.apache.xpath.internal.operations.Mod;
import lottery.mapper.PrizeMapper;
import lottery.mapper.RaffleRecordMapper;
import lottery.mapper.UserMapper;
import lottery.pojo.PrizeInventory;
import lottery.pojo.RaffleRecords;
import lottery.pojo.User;
import lottery.util.CommunityUtil;
import lottery.util.HostHolder;
import lottery.util.RedisKeyUtil;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.*;

/**
 @author Alex
 @create 2023-08-26-20:37
 */

@Controller
public class LotteryController {
    @Autowired
    PrizeMapper prizeMapper;
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserMapper userMapper;

    @Autowired
    HostHolder hostHolder;

    @Autowired RaffleRecordMapper raffleRecordMapper;

    /**
     * 将所有的库存刷到redis
     * @return
     */
    @GetMapping("/refreshStore")
    public String refreshStore(){

        // 将所有库存放到redis中
        for (int i = 1; i < 9; i++) {
            PrizeInventory prize = prizeMapper.getPrizeById(i);
            String inventoryKey = RedisKeyUtil.getInventoryKey(i);
            redisTemplate.opsForValue().set(inventoryKey,prize.getPrizeNum());
        }

        // 将所有抽奖次数 和 积分情况 放到redis中
        for (User user : userMapper.getUser()) {
            int userId = user.getUserId();
            String username = user.getUsername();
            int lotteryTimes = user.getLotteryTimes();
            String lotteryTimesKey = RedisKeyUtil.getLotteryTimesKey(userId,username);
            redisTemplate.opsForValue().set(lotteryTimesKey,lotteryTimes);

            int score = user.getScore();
            String scoreKey = RedisKeyUtil.getScore(userId, username);
            redisTemplate.opsForValue().set(scoreKey,score);
        }


        return "forward:/index";
    }

    @ResponseBody
    @RequestMapping(value = "/getScoreAndTimes", method = RequestMethod.GET)
    public Map getScoreAndTimes(){
        HashMap<String, Integer> map = new HashMap<>();
        int lotteryTimes = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getLotteryTimesKey(hostHolder.getUser().getUserId(), hostHolder.getUser().getUsername()));
        int score = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getScore(hostHolder.getUser().getUserId(), hostHolder.getUser().getUsername()));
        map.put("lotteryTimes",lotteryTimes);
        map.put("score",score);
        return map;
    }

    @ResponseBody
    @RequestMapping(value = "/getRaffleRecord", method = RequestMethod.GET)
    public List<Map<String,Object>> getRaffleRecord(){
        List<RaffleRecords> raffleRecords = raffleRecordMapper.getRaffleRecordByUserId(hostHolder.getUser().getUserId());
        List<Map<String,Object>> records = new ArrayList<>();
        for (RaffleRecords record:raffleRecords) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("prizeId",record.getPrizeId());
            map.put("prizeTime",record.getPrizeTime());
            records.add(map);
        }
        return records;
    }

    @ResponseBody
    @RequestMapping(value = "/getInventory", method = RequestMethod.GET)
    public Map getInventory(){
        int prize_1 = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(1));
        int prize_2 = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(2));
        int prize_3 = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(3));
        int prize_4 = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(4));
        int prize_5 = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(5));
        int prize_6 = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(6));
        int prize_7 = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(7));
        int prize_8 = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(8));
        HashMap<String, Integer> map = new HashMap<>();
        map.put("prize_1",prize_1);
        map.put("prize_2",prize_2);
        map.put("prize_3",prize_3);
        map.put("prize_4",prize_4);
        map.put("prize_5",prize_5);
        map.put("prize_6",prize_6);
        map.put("prize_7",prize_7);
        map.put("prize_8",prize_8);
        return map;
    }



    @GetMapping("draw")
    public String drawPage(){
        return "site/turntable";
    }

    @ResponseBody
    @RequestMapping(value = "/lottery", method = RequestMethod.POST)
    public Map lottery(Model model){
        HashMap<String, Integer> map = new HashMap<>();
        double randomNum = Math.random();
        int lotteryTimes = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getLotteryTimesKey(hostHolder.getUser().getUserId(), hostHolder.getUser().getUsername()));
        int score = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getScore(hostHolder.getUser().getUserId(), hostHolder.getUser().getUsername()));

        if(lotteryTimes==0){
            map.put("iEnd",-1);
            return map;
        }

        if(score<100){
            map.put("iEnd",-2);
            return map;
        }

        if(randomNum<=0.05){
            int count = (int)redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(1));
            if((int)redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(1))==0){
                int num = (int)(Math.random() * 3) + 6;
                map.put("iEnd",num);
                addScore(num,hostHolder,redisTemplate);
                // 当前登录用户的抽奖次数-1
                reduceLotteryTimes(hostHolder,redisTemplate);
                // 当前登录用户的分数-100
                reduceScore(hostHolder,redisTemplate);
                // 插入抽奖记录
                addRaffleRecord(raffleRecordMapper,num,hostHolder.getUser().getUserId());
                return map;
            }
            count --;
            redisTemplate.opsForValue().set(RedisKeyUtil.getInventoryKey(1),count);
            map.put("iEnd",1);
            // 当前登录用户的抽奖次数-1
            reduceLotteryTimes(hostHolder,redisTemplate);
            // 当前登录用户的分数-100
            reduceScore(hostHolder,redisTemplate);
            // 插入抽奖记录
            addRaffleRecord(raffleRecordMapper,1,hostHolder.getUser().getUserId());
        }else if(randomNum>0.05 && randomNum<=0.15){
            int count = (int)redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(2));
            if((int)redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(2))==0){
                int num = (int)(Math.random() * 3) + 6;
                map.put("iEnd",num);
                addScore(num,hostHolder,redisTemplate);
                // 当前登录用户的抽奖次数-1
                reduceLotteryTimes(hostHolder,redisTemplate);
                // 当前登录用户的分数-100
                reduceScore(hostHolder,redisTemplate);
                // 插入抽奖记录
                addRaffleRecord(raffleRecordMapper,num,hostHolder.getUser().getUserId());
                return map;
            }
            count--;
            redisTemplate.opsForValue().set(RedisKeyUtil.getInventoryKey(2),count);
            map.put("iEnd",2);
            // 当前登录用户的抽奖次数-1
            reduceLotteryTimes(hostHolder,redisTemplate);
            // 当前登录用户的分数-100
            reduceScore(hostHolder,redisTemplate);
            // 插入抽奖记录
            addRaffleRecord(raffleRecordMapper,2,hostHolder.getUser().getUserId());
        }else if(randomNum>0.15 && randomNum<=0.3){
            int count = (int)redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(3));
            if((int)redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(3))==0){
                int num = (int)(Math.random() * 3) + 6;
                map.put("iEnd",num);
                addScore(num,hostHolder,redisTemplate);
                // 当前登录用户的抽奖次数-1
                reduceLotteryTimes(hostHolder,redisTemplate);
                // 当前登录用户的分数-100
                reduceScore(hostHolder,redisTemplate);
                // 插入抽奖记录
                addRaffleRecord(raffleRecordMapper,num,hostHolder.getUser().getUserId());
                return map;
            }
            count --;
            redisTemplate.opsForValue().set(RedisKeyUtil.getInventoryKey(3),count);
            map.put("iEnd",3);
            // 当前登录用户的抽奖次数-1
            reduceLotteryTimes(hostHolder,redisTemplate);
            // 当前登录用户的分数-100
            reduceScore(hostHolder,redisTemplate);
            // 插入抽奖记录
            addRaffleRecord(raffleRecordMapper,3,hostHolder.getUser().getUserId());
        }else if(randomNum>0.3 && randomNum<=0.5){
            int count = (int)redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(4));
            if((int)redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(4))==0){
                int num = (int)(Math.random() * 3) + 6;
                map.put("iEnd",num);
                addScore(num,hostHolder,redisTemplate);
                // 当前登录用户的抽奖次数-1
                reduceLotteryTimes(hostHolder,redisTemplate);
                // 当前登录用户的分数-100
                reduceScore(hostHolder,redisTemplate);
                // 插入抽奖记录
                addRaffleRecord(raffleRecordMapper,num,hostHolder.getUser().getUserId());
                return map;
            }
            count --;
            redisTemplate.opsForValue().set(RedisKeyUtil.getInventoryKey(5),count);
            map.put("iEnd",4);
            // 当前登录用户的抽奖次数-1
            reduceLotteryTimes(hostHolder,redisTemplate);
            // 当前登录用户的分数-100
            reduceScore(hostHolder,redisTemplate);
            // 插入抽奖记录
            addRaffleRecord(raffleRecordMapper,4,hostHolder.getUser().getUserId());
        }else if(randomNum>0.5 && randomNum<=0.9){
            int count = (int)redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(5));
            if((int)redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(5))==0){
                int num = (int)(Math.random() * 3) + 6;
                map.put("iEnd",num);
                addScore(num,hostHolder,redisTemplate);
                // 当前登录用户的抽奖次数-1
                reduceLotteryTimes(hostHolder,redisTemplate);
                // 当前登录用户的分数-100
                reduceScore(hostHolder,redisTemplate);
                // 插入抽奖记录
                addRaffleRecord(raffleRecordMapper,num,hostHolder.getUser().getUserId());
                return map;
            }
            count --;
            redisTemplate.opsForValue().set(RedisKeyUtil.getInventoryKey(5),count);
            map.put("iEnd",5);
            // 当前登录用户的抽奖次数-1
            reduceLotteryTimes(hostHolder,redisTemplate);
            // 当前登录用户的分数-100
            reduceScore(hostHolder,redisTemplate);
            // 插入抽奖记录
            addRaffleRecord(raffleRecordMapper,5,hostHolder.getUser().getUserId());
        }else{
            int num = (int)(Math.random() * 3) + 6;
            map.put("iEnd",num);
            addScore(num,hostHolder,redisTemplate);
            // 当前登录用户的抽奖次数-1
            reduceLotteryTimes(hostHolder,redisTemplate);
            // 当前登录用户的分数-100
            reduceScore(hostHolder,redisTemplate);
            // 插入抽奖记录
            addRaffleRecord(raffleRecordMapper,num,hostHolder.getUser().getUserId());
        }
        return map;
    }


    @ResponseBody
    @RequestMapping(value = "/lotteryTest", method = RequestMethod.GET)
    public synchronized Map lotteryTest(Model model,String usernameId){
        HashMap<String, Integer> map = new HashMap<>();
        String[] strs = usernameId.split(":");
        int userId = Integer.parseInt(strs[0]);
        String  username = strs[1];

        double randomNum = Math.random();
        int lotteryTimes = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getLotteryTimesKey(userId,username));
        int score = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getScore(userId, username));

        if(lotteryTimes==0){
            map.put("iEnd",-1);
            return map;
        }

        if(score<100){
            map.put("iEnd",-2);
            return map;
        }

        if(randomNum<=0.05){
            int count = (int)redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(1));
            if((int)redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(1))==0){
                int num = (int)(Math.random() * 3) + 6;
                map.put("iEnd",num);
                addScore1(num,userId,username,redisTemplate);
                // 当前登录用户的抽奖次数-1
                reduceLotteryTimes1(userId,username,redisTemplate);
                // 当前登录用户的分数-100
                reduceScore1(userId,username,redisTemplate);
                // 插入抽奖记录
                addRaffleRecord(raffleRecordMapper,num,userId);
                return map;
            }
            redisTemplate.opsForValue().set(RedisKeyUtil.getInventoryKey(1),--count);
            map.put("iEnd",1);
            // 当前登录用户的抽奖次数-1
            reduceLotteryTimes1(userId,username,redisTemplate);
            // 当前登录用户的分数-100
            reduceScore1(userId,username,redisTemplate);
            // 插入抽奖记录
            addRaffleRecord(raffleRecordMapper,1,userId);
        }else if(randomNum>0.05 && randomNum<=0.15){
            int count = (int)redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(2));
            if((int)redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(2))==0){
                int num = (int)(Math.random() * 3) + 6;
                map.put("iEnd",num);
                addScore1(num,userId,username,redisTemplate);
                // 当前登录用户的抽奖次数-1
                reduceLotteryTimes1(userId,username,redisTemplate);
                // 当前登录用户的分数-100
                reduceScore1(userId,username,redisTemplate);
                // 插入抽奖记录
                addRaffleRecord(raffleRecordMapper,num,userId);
                return map;
            }
            redisTemplate.opsForValue().set(RedisKeyUtil.getInventoryKey(2),--count);
            map.put("iEnd",2);
            // 当前登录用户的抽奖次数-1
            reduceLotteryTimes1(userId,username,redisTemplate);
            // 当前登录用户的分数-100
            reduceScore1(userId,username,redisTemplate);
            // 插入抽奖记录
            addRaffleRecord(raffleRecordMapper,2,userId);
        }else if(randomNum>0.15 && randomNum<=0.3){
            int count = (int)redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(3));
            if((int)redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(3))==0){
                int num = (int)(Math.random() * 3) + 6;
                map.put("iEnd",num);
                addScore1(num,userId,username,redisTemplate);
                // 当前登录用户的抽奖次数-1
                reduceLotteryTimes1(userId,username,redisTemplate);
                // 当前登录用户的分数-100
                reduceScore1(userId,username,redisTemplate);
                // 插入抽奖记录
                addRaffleRecord(raffleRecordMapper,num,userId);
                return map;
            }
            redisTemplate.opsForValue().set(RedisKeyUtil.getInventoryKey(3),--count);
            map.put("iEnd",3);
            // 当前登录用户的抽奖次数-1
            reduceLotteryTimes1(userId,username,redisTemplate);
            // 当前登录用户的分数-100
            reduceScore1(userId,username,redisTemplate);
            // 插入抽奖记录
            addRaffleRecord(raffleRecordMapper,3,userId);
        }else if(randomNum>0.3 && randomNum<=0.5){
            int count = (int)redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(4));
            if((int)redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(4))==0){
                int num = (int)(Math.random() * 3) + 6;
                map.put("iEnd",num);
                addScore1(num,userId,username,redisTemplate);
                // 当前登录用户的抽奖次数-1
                reduceLotteryTimes1(userId,username,redisTemplate);
                // 当前登录用户的分数-100
                reduceScore1(userId,username,redisTemplate);
                // 插入抽奖记录
                addRaffleRecord(raffleRecordMapper,num,userId);
                return map;
            }
            redisTemplate.opsForValue().set(RedisKeyUtil.getInventoryKey(4),--count);
            map.put("iEnd",4);
            // 当前登录用户的抽奖次数-1
            reduceLotteryTimes1(userId,username,redisTemplate);
            // 当前登录用户的分数-100
            reduceScore1(userId,username,redisTemplate);
            // 插入抽奖记录
            addRaffleRecord(raffleRecordMapper,4,userId);
        }else if(randomNum>0.5 && randomNum<=0.9){
            int count = (int)redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(5));
            if((int)redisTemplate.opsForValue().get(RedisKeyUtil.getInventoryKey(5))==0){
                int num = (int)(Math.random() * 3) + 6;
                map.put("iEnd",num);
                addScore1(num,userId,username,redisTemplate);
                // 当前登录用户的抽奖次数-1
                reduceLotteryTimes1(userId,username,redisTemplate);
                // 当前登录用户的分数-100
                reduceScore1(userId,username,redisTemplate);
                // 插入抽奖记录
                addRaffleRecord(raffleRecordMapper,num,userId);
                return map;
            }
            redisTemplate.opsForValue().set(RedisKeyUtil.getInventoryKey(5),--count);
            map.put("iEnd",5);
            // 当前登录用户的抽奖次数-1
            reduceLotteryTimes1(userId,username,redisTemplate);
            // 当前登录用户的分数-100
            reduceScore1(userId,username,redisTemplate);
            // 插入抽奖记录
            addRaffleRecord(raffleRecordMapper,5,userId);
        }else{
            int num = (int)(Math.random() * 3) + 6;
            map.put("iEnd",num);
            addScore1(num,userId,username,redisTemplate);
            // 当前登录用户的抽奖次数-1
            reduceLotteryTimes1(userId,username,redisTemplate);
            // 当前登录用户的分数-100
            reduceScore1(userId,username,redisTemplate);
            // 插入抽奖记录
            addRaffleRecord(raffleRecordMapper,num,userId);
        }
        return map;
    }


    public static void reduceLotteryTimes(HostHolder hostHolder,RedisTemplate redisTemplate){
        // 当前登录用户的抽奖次数-1
        int lotteryTimes = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getLotteryTimesKey(hostHolder.getUser().getUserId(), hostHolder.getUser().getUsername()));
        lotteryTimes--;
        redisTemplate.opsForValue().set(RedisKeyUtil.getLotteryTimesKey(hostHolder.getUser().getUserId(), hostHolder.getUser().getUsername()),lotteryTimes);
    }

    public static void reduceLotteryTimes1(int userId,String username,RedisTemplate redisTemplate){
        // 当前登录用户的抽奖次数-1
        int lotteryTimes = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getLotteryTimesKey(userId,username));
        lotteryTimes--;
        redisTemplate.opsForValue().set(RedisKeyUtil.getLotteryTimesKey(userId,username),lotteryTimes);
    }


    /**
     * 减少用户积分
     */
    public static void reduceScore(HostHolder hostHolder,RedisTemplate redisTemplate){
        // 当前登录用户的抽奖次数-1
        int score = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getScore(hostHolder.getUser().getUserId(), hostHolder.getUser().getUsername()));
        score = score -100;
        redisTemplate.opsForValue().set(RedisKeyUtil.getScore(hostHolder.getUser().getUserId(), hostHolder.getUser().getUsername()),score);
    }

    public static void reduceScore1(int userId,String username,RedisTemplate redisTemplate){
        // 当前登录用户的抽奖次数-1
        int score = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getScore(userId,username));
        score = score -100;
        redisTemplate.opsForValue().set(RedisKeyUtil.getScore(userId,username),score);
    }



    /**
     * 根据用户抽到的奖加积分。如果抽到积分奖
     */
    public static void addScore(int num,HostHolder hostHolder,RedisTemplate redisTemplate){
        int score = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getScore(hostHolder.getUser().getUserId(), hostHolder.getUser().getUsername()));
        if(num == 6){
            score = score + 100;
            redisTemplate.opsForValue().set(RedisKeyUtil.getScore(hostHolder.getUser().getUserId(), hostHolder.getUser().getUsername()),score);
        }else if(num == 7){
            score = score + 50;
            redisTemplate.opsForValue().set(RedisKeyUtil.getScore(hostHolder.getUser().getUserId(), hostHolder.getUser().getUsername()),score);
        }else if(num == 8){
            score = score + 10;
            redisTemplate.opsForValue().set(RedisKeyUtil.getScore(hostHolder.getUser().getUserId(), hostHolder.getUser().getUsername()),score);
        }
    }

    public static void addScore1(int num,int userId,String username,RedisTemplate redisTemplate){
        int score = (int) redisTemplate.opsForValue().get(RedisKeyUtil.getScore(userId, username));
        if(num == 6){
            score = score + 100;
            redisTemplate.opsForValue().set(RedisKeyUtil.getScore(userId, username),score);
        }else if(num == 7){
            score = score + 50;
            redisTemplate.opsForValue().set(RedisKeyUtil.getScore(userId, username),score);
        }else if(num == 8){
            score = score + 10;
            redisTemplate.opsForValue().set(RedisKeyUtil.getScore(userId, username),score);
        }
    }

    /**
     * 插入抽奖记录到抽奖记录表中
     */
    public static void addRaffleRecord(RaffleRecordMapper raffleRecordMapper,int prizeId,int userId){
        RaffleRecords raffleRecord = new RaffleRecords();
        raffleRecord.setRaffleId(CommunityUtil.generateUUID());
        raffleRecord.setUserId(userId);
        raffleRecord.setPrizeId(prizeId);
        raffleRecord.setPrizeTime(new Date());
        raffleRecordMapper.insertRaffleRecord(raffleRecord);
    }
}
