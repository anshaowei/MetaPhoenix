package net.csibio.metaphoenix.client.domain.bean.parser.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.fastjson2.JSON;
import net.csibio.metaphoenix.client.domain.bean.parser.csv.CsvCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExcelListener extends AnalysisEventListener<CsvCompound> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelListener.class);

    private static final int BATCH_COUNT = 5;
    List<CsvCompound> list = new ArrayList<CsvCompound>();

    public ExcelListener() {

    }

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        super.invokeHead(headMap, context);
    }

    @Override
    public void invoke(CsvCompound csvTarget, AnalysisContext analysisContext) {
        LOGGER.info("解析到一条数据：{}", JSON.toJSONString(csvTarget));
        list.add(csvTarget);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        super.onException(exception, context);
    }

    @Override
    public boolean hasNext(AnalysisContext context) {
        return super.hasNext(context);
    }
}
