package net.csibio.metaphoenix.client.domain.bean.chemical;

import lombok.Data;
import net.csibio.metaphoenix.client.constants.enums.ElementType;
import net.csibio.metaphoenix.client.constants.enums.Operator;

import java.util.List;

@Data
public class OpElement {

    // 是否是加运算,否则为减运算
    boolean plus;

    Double monoMw;

    String symbol;

    // 对应的数目, 默认为1
    Integer n = 1;

    public static OpElement build(String plus, Integer n, ElementType e){
        OpElement opElement = new OpElement();
        opElement.plus = plus.equals("+");
        opElement.symbol = e.getSymbol();
        opElement.monoMw = e.getMonoMw();
        opElement.n = n;
        return opElement;
    }

    public static OpElement build(Operator operator, Integer n, ElementType e){
        OpElement opElement = new OpElement();
        opElement.plus = operator.getSymbol().equals("+");
        opElement.symbol = e.getSymbol();
        opElement.monoMw = e.getMonoMw();
        opElement.n = n;
        return opElement;
    }

    public static OpElement build(boolean plus, Integer n, Double monoMw, String symbol){
        OpElement opElement = new OpElement();
        opElement.plus = plus;
        opElement.symbol = symbol;
        opElement.monoMw = monoMw;
        opElement.n = n;
        return opElement;
    }

    // 计算一个可操作元素列表的总分子质量
    public static Double getMonoMw(List<OpElement> elementList) {
        Double monoMw = 0d;
        for (OpElement opElement : elementList) {
            if (opElement.isPlus()) {
                monoMw += opElement.getMonoMw() * opElement.getN();
            } else {
                monoMw -= opElement.getMonoMw() * opElement.getN();
            }
        }
        return monoMw;
    }

    // 组装一个可操作元素列表的名称,忽略正负符号,组合方式为 化学元素+数目+化学元素+数目...
    public static String getGroupName(List<OpElement> elementList) {
        StringBuilder groupName = new StringBuilder();
        for (OpElement opElement : elementList) {
            groupName.append(opElement.getSymbol());
            if (opElement.getN() != 1){
                groupName.append(opElement.getSymbol());
            }
        }
        return groupName.toString();
    }
}
