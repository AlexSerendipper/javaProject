package community.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 @author Alex
 @create 2023-04-07-16:28
 */
@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    // 替换符
    private static final String REPLACEMENT = "***";
    // 根节点初始化
    private TireNode rootNode = new TireNode();

    /**
     * 这个@PostConstruct是初始化方法注解，当容器实例化后（调用了构造器后），该注解标记的方法被调用
     * 该初始化方法将所有敏感词添加到树中
     * @throws Exception
     */
    @PostConstruct
    public void init() throws Exception {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
        // 转换为字符流后转换为缓冲流
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String keyword;
        while((keyword = reader.readLine())!=null){
            // 添加数据到前缀树
            this.addKeyword(keyword);
        }
    }

    /**
     * 将一个敏感词添加到前缀树中
     * @param keyword
     */
    private void addKeyword(String keyword) {
        // 默认所有节点最初都指向根节点
        TireNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            // 得到的每个字符挂到相应节点后面
            char c = keyword.charAt(i);
            TireNode subNode = tempNode.getSubNode(c);
            // 如果没有字符对应的子节点，则创建一个，如果有就 无需操作
            if(subNode==null){
                // 初始化一个子节点
                subNode = new TireNode();
                tempNode.addSubNode(c,subNode);
            }
            // 指向子节点，进入下一轮循环
            tempNode = subNode;
            // 设置结束标识（标记这个字符是一个敏感词）
            if(i==keyword.length()-1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     * @param text 待过滤文本
     * @return 过滤后的文本
     */
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        // 指针1：指向树的节点，node表示当前节点
        TireNode node = rootNode;
        // 指针2：指向字符串开始位置
        int begin = 0;
        // 指针3：指向字符串结束位置
        int position = 0;
        StringBuilder sb = new StringBuilder();
        // 符号跳过算法流程：以指针2、3为 待过滤文本 的索引从0指到尾，（当指针3 指向结尾时停止遍历）
        while (position<text.length()){
            char c = text.charAt(position);
            // 跳过符号！如：☆赌☆博☆，这也是敏感词！
            if(isSymbol(c)){
                // 若指针1处理根节点，计入此符号，让指针二向下走一位
                if(node == rootNode){
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头或中间，指针三都向下走一步
                position++;
                continue;
            }

            // 检查下一个节点：判断是否有position位置字符的下级节点
            node = node.getSubNode(c);

            // 没有下级节点，说明当前字符就不可能是敏感词，检查下一个字符即可
            if(node == null){
                sb.append(text.charAt(begin));
                // 进入下一个位置
                position = ++begin;
                node = rootNode;
            // 下级节点标记为敏感，则说明当前begin~position范围内是敏感词，检测position的下一位置即可
            }else if(node.isKeywordEnd){
                sb.append(REPLACEMENT);
                begin = ++position;
                node = rootNode;
            // 当前仍可能是敏感词
            }else{
                position++;
            }
        }
        // 将最后一批字符计入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }

    private boolean isSymbol(Character c){
        // 0x2E80~0x9FFF为东亚文字范围（日文 韩文），
        return !CharUtils.isAsciiAlphanumeric(c) && (c<0x2E80 || c>0x9FFF);
    }

    // 前缀树节点，内部类
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    private class TireNode{
        // 关键词结束的标识
        private boolean isKeywordEnd = false;

        // 子节点(key:character是节点字符，value:tirenode是下级节点)
        private Map<Character,TireNode> subnodes = new HashMap<>();

        // 添加子节点
        public void addSubNode(Character c,TireNode node){
            subnodes.put(c,node);
        }

        // 获取子节点
        @Nullable
        public TireNode getSubNode(Character c){
            return subnodes.get(c);
        }
    }
}
