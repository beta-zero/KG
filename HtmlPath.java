/*
 * Copyright (C), 2018-2018, http://www.joyoung.com.cn/
 * FileName : HtmlPath
 * Author : Fu Weilin
 * Date : 2018/5/31
 * Description : DOM路径表示
 * History :
 * <author>          <time>          <version>          <desc>
 * 付维林           2018-05-31          0.0.1             创建
 */
package similar;

import java.util.List;

/**
 *
 * 定义树路径 (出现次数，标签路径，出现位置)
 * 计算树路径相似度
 * 对于两条路径：
 *      P_i = {m, t_i_1, ..., t_i_n, s_i_1, ..., s_i_m}
 *      P_j = {g, t_j_1, ..., t_j_h, s_j_1, ..., s_j_g}
 *
 *  sim(P_i, P_j) = w * st(P_i, P_j) + (1-w) * sp(P_i, P_j)
 *  其中：
 *                          clen(P_i, P_j)
 *      st(P_i, P_j) = -------------------------
 *                      max(len(P_i), len(P_j))
 *      st()表示路径的标签序列相似度
 *      len()表示路径的标签序列长度
 *      clen()表示两条路径从根节点开始的最长公共标签序列长度
 *
 *                              sum([md(s_i_k) for k in 1~m])      sum([md(s_j_k) for k in 1~g])
 *                            --------------------------------- + --------------------------------- + |m-g|
 *                                           pn                                  pn
 *      sp(P_i, P_j) = 1 - -----------------------------------------------------------------------------
 *                                                         2 * max(m, g)
 *                pn = max(pn_i, pn_j) - 1
 *                md(s_i_k) = min(|s_i_k - s_j_1|, ..., |s_i_k - s_j_g|)
 *      sp()表示路径的位置相似度
 *      md()表示P_i路径在位置s_i_k处与P_j的最近距离
 *      pn_i表示P_i所在的树的叶子节点总数
 *
 *      w 用于调节这两部分在相似性中的重要性
 *
 * @author : fuweilin
 * @create 2018/5/31
 */


public class HtmlPath {

    public static int count = 0;

    public static double w = 0.5;

    private HtmlTree tree;
    int times = 0;              // 出现次数
    List<String> tagPath;       // 标签路径
    List<Integer> pathOrder;    // 出现位置

    HtmlPath(HtmlTree tree){
        this.tree = tree;
    }

    public HtmlPath(HtmlTree htmlTree, int times, List<String> tagPath, List<Integer> pathOrder){
        this.tree = htmlTree;
        this.times = times;
        this.tagPath = tagPath;
        this.pathOrder = pathOrder;
    }

    private int length(){
        return tagPath.size();
    }

    private String getTagNode(int idx){
        return this.tagPath.get(idx);
    }

    private HtmlTree getTree(){
        return this.tree;
    }

    int getTimes() {
        return this.times;
    }

    private List<Integer> getPathOrder() {
        return this.pathOrder;
    }

    // 相似度
    public static double similar(HtmlPath srcPath, HtmlPath dstPath){
        HtmlPath.count ++;
        if (srcPath.equals(dstPath))
            return 1.0;
        return w * tagSimilar(srcPath, dstPath) + (1 - w) * posSimilar(srcPath, dstPath);
    }

    // 标签序列相似度
    private static double tagSimilar(HtmlPath srcPath, HtmlPath dstPath){
        return commonLength(srcPath, dstPath) / Integer.max(srcPath.length(), dstPath.length());
    }

    // 表示两条路径从根节点开始的最长公共标签序列长度
    private static double commonLength(HtmlPath srcPath, HtmlPath dstPath){
        if (srcPath.length() == 0 || dstPath.length() == 0)
            return 0;
        int maxCommonLength = Integer.min(srcPath.length(), dstPath.length());
        for (int idx = 0; idx < maxCommonLength; idx++)
            if (!(srcPath.getTagNode(idx).equals(dstPath.getTagNode(idx))))
                return idx;
        return maxCommonLength;
    }

    // 位置相似度
    private static double posSimilar(HtmlPath srcPath, HtmlPath dstPath){
        int pn = Integer.max(srcPath.getTree().leafCount(), dstPath.getTree().leafCount()) - 1;

        double totalDistance = 0;
        for (int k = 0; k < srcPath.getTimes(); k++) {
            totalDistance += minDistance(srcPath, dstPath, k);
        }
        for (int k = 0; k < dstPath.getTimes(); k++) {
            totalDistance += minDistance(dstPath, srcPath, k);
        }

        return 1.0 - (totalDistance / pn + Math.abs(srcPath.getTimes() - dstPath.getTimes()))
                / (2 * Integer.max(srcPath.getTimes(), dstPath.getTimes()));
    }

    /**
     * P_i路径在位置s_i_k处与P_j的最近距离
     *      md(s_i_k) = min(|s_i_k - s_j_1|, ..., |s_i_k - s_j_g|)
     */
    private static double minDistance(HtmlPath srcPath, HtmlPath dstPath, int k){
        int sik = srcPath.getPathOrder().get(k);
        int distance = Integer.MAX_VALUE;
        for (Integer sjg : dstPath.getPathOrder()){
            distance = Integer.min(distance, Math.abs(sjg - sik));
        }
        return distance;
    }

    @Override
    public String toString(){
        return "(" + times + ", " + tagPath + ", " + pathOrder + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof HtmlPath))
            return false;
        return toString().equals(other.toString());
    }
}

