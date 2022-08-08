package net.csibio.mslibrary.client.service;

import net.csibio.mslibrary.client.constants.enums.StatDim;
import net.csibio.mslibrary.client.domain.db.StatDO;
import net.csibio.mslibrary.client.domain.query.StatQuery;
import net.csibio.mslibrary.client.exceptions.XException;

import java.text.ParseException;
import java.util.Date;

public interface StatService extends BaseService<StatDO, StatQuery> {

    StatDO getByUniqueKey(String dim, String type, Date date);

    /**
     * data格式按照20220607-133002格式传入,如果没有时分秒,则直接传入20220607
     * @param dim 统计维度, 见StatDim
     * @param type 统计类型, 见StatType
     * @param date 统计日期
     * @return
     */
    StatDO getByUniqueKey(String dim, String type, String date) throws XException;

    void globalStat(StatDim dim, Date date) throws ParseException;
}
