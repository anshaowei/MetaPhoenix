package net.csibio.mslibrary.client.domain.bean.statistic;

import lombok.Data;
import org.apache.commons.math3.stat.StatUtils;

import java.util.List;

@Data
public class Statistic {

    // 用于存储该项统计值对象的标签
    String[] tags;

    // 统计的值类型名
    String dimName;
    // 平均值
    Double mean;
    // 最小值
    Double min;
    // 最大值
    Double max;
    // 方差
    Double variance;
    // 目标统计数值列表
    List<Double> values;
    // ordinalLastArea - ordinalFirstArea
    Double diff;

    public Statistic() {
    }

    public Statistic(String dimName, List<Double> targetList, String... tags) {
        this.values = targetList;
        this.dimName = dimName;
        this.tags = tags;
        double[] valueArray = new double[targetList.size()];
        for (int i = 0; i < targetList.size(); i++) {
            valueArray[i] = targetList.get(i);
        }
        doAnalyze(valueArray);
        if (valueArray.length > 1) {
            diff = valueArray[valueArray.length - 1] - valueArray[0];
        } else {
            diff = 0d;
        }
    }

    public Statistic(String dimName, List<Double> targetList) {
        this.values = targetList;
        this.dimName = dimName;
        double[] valueArray = new double[targetList.size()];
        for (int i = 0; i < targetList.size(); i++) {
            valueArray[i] = targetList.get(i);
        }
        doAnalyze(valueArray);
        if (valueArray.length > 1) {
            diff = valueArray[valueArray.length - 1] - valueArray[0];
        } else {
            diff = 0d;
        }
    }

    public void doAnalyze(double[] arrays) {
        max = StatUtils.max(arrays);
        min = StatUtils.min(arrays);
        mean = StatUtils.mean(arrays);
        variance = StatUtils.variance(arrays, mean);
    }
}
