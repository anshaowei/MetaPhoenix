package net.csibio.mslibrary.client.domain.db;

import lombok.Data;
import net.csibio.mslibrary.client.domain.bean.spectrum.AnnotationHistory;
import net.csibio.mslibrary.client.utils.CompressUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "outerSpectrum")
public class SpectrumDO {

    //数据库索引信息
    @Id
    String id;

    @Indexed
    String compoundId;

    @Indexed
    String libraryId;

    //自定义谱图唯一标识字段
    String spectrumId;

    String sourceFile;

    String task;

    Integer scan;

    @Indexed
    Integer msLevel;

    @Indexed
    String libraryMembership;

    @Indexed
    Integer spectrumStatus;

    byte[] byteMzs;
    byte[] byteInts;

    @Transient
    double[] mzs;
    @Transient
    double[] ints;

    String splash;

    String submitUser;

    String compoundName;

    @Indexed
    String ionSource;

    @Indexed
    String compoundSource;

    @Indexed
    String instrument;

    String pi;

    String dataCollector;

    @Indexed
    String adduct;

    Double precursorMz;

    Double exactMass;

    @Indexed
    Integer charge;

    String casNumber;

    String pubmedId;

    String smiles;

    @Indexed
    String inchi;

    String inchiAUX;

    @Indexed
    Integer libraryClass;

    @Indexed
    String ionMode;

    Date createTime;

    String taskId;

    String userId;

    String inchiKeySmiles;

    String inchiKeyInchi;

    String formulaSmiles;

    String formulaInchi;

    String url;

    List<AnnotationHistory> annotationHistoryList;

    /**
     * 创建日期
     */
    Date createDate;

    /**
     * 最后修改日期
     */
    Date lastModifiedDate;

    public double[] getMzs() {
        if (mzs == null && byteMzs != null) {
            mzs = CompressUtil.decode(byteMzs);
        }
        return mzs;
    }

    public void setMzs(double[] mzs) {
        this.mzs = mzs;
        this.byteMzs = CompressUtil.encode(mzs);
    }

    public double[] getInts() {
        if (ints == null && byteInts != null) {
            ints = CompressUtil.decode(byteInts);
        }
        return ints;
    }

    public void setInts(double[] ints) {
        this.ints = ints;
        this.byteInts = CompressUtil.encode(ints);
    }

}
