package community.config;


import community.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 @author Alex
 @create 2023-04-25-15:13
 */
@Configuration
public class QuartzConfig {
    // 通常BeanFactory是整个IOC容器的顶层接口
    // 而FactoryBean作用是可简化bean的实例化过程：
    // 1. FactoryBean 中封装了某些bean的实例化过程
    // 2. 将 FactoryBean 装配到spring容器中后， 将 FactoryBean 注入给其他的bean，则该bean得到的是FactoryBean所管理的对象实例

    // 刷新帖子分数的定时任务
    // 配置 JobDetail
    @Bean
    public JobDetailFactoryBean PostScoreRefreshJobDetailFactoryBean(){
        JobDetailFactoryBean JobDetail = new JobDetailFactoryBean();
        JobDetail.setJobClass(PostScoreRefreshJob.class);  // 管理的job
        JobDetail.setName("postScoreRefreshJob");  // 为job取名
        JobDetail.setGroup("communityJobGroup");  // 为job设定一个组（多个Job可以在同一个组中）
        JobDetail.setDurability(true);  // job是否永久保存（job被废弃后，相关信息仍保存）
        JobDetail.setRequestsRecovery(true);  // job出现异常，是否可恢复
        return JobDetail;
    }

    // 配置 Trigger
    @Bean
    public SimpleTriggerFactoryBean simpleTriggerFactoryBean(JobDetail PostScoreRefreshJobDetailFactoryBean){  // 如此处，注入的并不是JobDetailFactoryBean对象，而是其所管理的JobDetail对象
        SimpleTriggerFactoryBean Trigger = new SimpleTriggerFactoryBean();
        Trigger.setJobDetail(PostScoreRefreshJobDetailFactoryBean);   // 指定为哪个job设定的触发器
        Trigger.setName("postScoreRefreshTrigger");  // 为当前trigger取名
        Trigger.setGroup("communityTriggerGroup");  // 为当前trigger设定一个组
        Trigger.setRepeatInterval(1000 * 60 * 5);  // 触发器执行的时间，多长时间触发一次
        Trigger.setJobDataMap(new JobDataMap());  // 使用JobDataMap存储触发器的运行状态
        return Trigger;
    }

}
