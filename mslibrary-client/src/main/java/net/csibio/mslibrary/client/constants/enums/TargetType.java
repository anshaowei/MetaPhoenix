package net.csibio.mslibrary.client.constants.enums;

import java.util.Locale;

public enum TargetType {

    Target("Target"), //非内标型靶标
    IS("IS"), //内标Internal Standard, Standard added before sample processing
    RS("RS"), //回收标Recovery Standard, Standard added after all sample processing
    Endogenous("Endogenous"), // 内源性靶标
    SS("SS"), // 替代标Surrogate Standard
    LIIS("LIIS"), // Labelled internal injection standard
    ;
    String name;

    TargetType(String name) {
        this.name = name;
    }

    //判断指定的名称是否在枚举类中,忽略大小写
    public static String getName(String type) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].getName().toLowerCase(Locale.ROOT).equals(type.toLowerCase())) {
                return values()[i].getName();
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
}
