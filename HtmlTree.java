/*
 * Copyright (C), 2018-2018, http://www.joyoung.com.cn/
 * FileName : HtmlTree
 * Author : Fu Weilin
 * Date : 2018/5/31
 * Description : DOM树表示
 * History :
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package similar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.*;

/**
 * 计算网页结构相似度
 * 对于两个网页：
 *      h_i = (P_i_1, P_i_2, ..., P_i_m)
 *      h_j = (P_j_1, P_j_2, ..., P_i_n)
 * 相似度看作h_i和h_j中每条路径与最佳匹配路径相似度的平均值。
 *                        sum([hjbPath(P_i_k) for k in 1~m])       sum([hibPath(P_j_k) for k in 1~n])
 *                      -------------------------------------- + --------------------------------------
 *                                        m                                         n
 *      sim(h_i, h_j) = ------------------------------------------------------------------------------
 *                                                             2
 *      hjbPath(P_i_k) = max([sim(P_i_k, P_j_1), ..., sim(P_i_k, P_j_n)])
 *
 * @author :
 * @create 2018/5/31
 */

public class HtmlTree {

    public static int count = 0;

    // 路径列表
    private List<HtmlPath> lstHtmlPath;
    // 文件名
    private String fileName;
    // 编号
    private int idx = -1;


    public HtmlTree(String fileName){
        this.fileName = fileName;
        this.lstHtmlPath = new ArrayList<>();
    }

    private int size(){
        return lstHtmlPath.size();
    }

    private HtmlPath getHtmlPath(int idx){
        return this.lstHtmlPath.get(idx);
    }

    private void addHtmlPath(HtmlPath htmlPath){
        this.lstHtmlPath.add(htmlPath);
    }

    public void setIdx(int idx){
        this.idx = idx;
    }

    public int getIdx(){
        return this.idx;
    }

    // 计算叶子节点数量
    int leafCount(){
        if (lstHtmlPath == null)
            return 0;
        int count = 0;
        for (HtmlPath htmlPath : lstHtmlPath)
            count += htmlPath.getTimes();
        return count;
    }

    // 相似度
    public static double similar(HtmlTree srcTree, HtmlTree dstTree){
        HtmlTree.count ++;
        if (srcTree.equals(dstTree))
            return 1.0;
        return (totalSimilar(srcTree, dstTree) / srcTree.size() + totalSimilar(dstTree, srcTree) / dstTree.size()) / 2;
    }

    // 对最佳匹配路径相似度求和
    private static double totalSimilar(HtmlTree srcTree, HtmlTree dstTree){
        double total = 0.0;
        for (int k = 0; k < srcTree.size(); k++) {
            total += hjbSimilar(srcTree, dstTree, k);
        }
        return total;
    }

    // h_i和h_j中路径k与最佳匹配路径相似度的平均值
    private static double hjbSimilar(HtmlTree srcTree, HtmlTree dstTree, int k){
        HtmlPath path_i = srcTree.getHtmlPath(k);
        if (dstTree.size() == 0)
            return 0.0;
        double maxSimilar = Double.MIN_VALUE;
        for (HtmlPath path_j : dstTree.lstHtmlPath) {
            maxSimilar = Double.max(maxSimilar, HtmlPath.similar(path_i, path_j));
            if (maxSimilar >= 1.0)
                break;
        }
        return maxSimilar;
    }


    // todo 标签清理
    // 将html转化为路径表示
    public static void html2Tree(HtmlTree srcTree, String html){
        Document doc = Jsoup.parse(html);
        Stack<String> stack = new Stack<>();
        List<List<String>> lstTagPath = new ArrayList<>();
        dfsDomTree(doc, stack, lstTagPath);

        Map<String, List<Integer>> pathMap = new HashMap<>();
        Map<String, List<String>> tmpMap = new HashMap<>();
        for (int i = 0; i < lstTagPath.size(); i++) {
            String pathString = lstTagPath.get(i).toString();
            if (!pathMap.containsKey(pathString)) {
                pathMap.put(pathString, new ArrayList<>());
                tmpMap.put(pathString, lstTagPath.get(i));
            }
            pathMap.get(pathString).add(i);
        }

        for (String path : pathMap.keySet()){
            HtmlPath htmlPath = new HtmlPath(srcTree);
            htmlPath.times = pathMap.get(path).size();
            htmlPath.tagPath = tmpMap.get(path);
            htmlPath.pathOrder = pathMap.get(path);
            srcTree.addHtmlPath(htmlPath);
        }
    }

    // 深度遍历整棵DOM树
    private static void dfsDomTree(Element ele, Stack<String> stack, List<List<String>> lstTagPath){
        if (ele == null)
            return;
        stack.push(ele.tagName());
        if (ele.children().isEmpty()){
            List<String> path = new ArrayList<>();
            for (Object obj : stack.toArray())
                path.add((String) obj);
            lstTagPath.add(path);
        } else {
            for (Element child : ele.children())
                dfsDomTree(child, stack, lstTagPath);
        }
        stack.pop();
    }

    @Override
    public String toString(){
        return "HtmlPathSize: " + size() + ", fileName: " + fileName + ", LeafCount: " + leafCount();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof HtmlTree))
            return false;
        return toString().equals(other.toString());
    }
}
