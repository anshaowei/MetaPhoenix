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
@Document(collection = "spectrum")
public class SpectrumDO {

    /**
     * Common Item
     */

    @Id
    String id;

    @Indexed
    String compoundId;

    @Indexed
    String libraryId;

    //The outer database id
    @Indexed
    String spectrumId;

    @Indexed
    Integer msLevel;

    String splash;

    @Indexed
    String ionSource;

    String compoundName;

    @Indexed
    String instrument;

    @Indexed
    String precursorAdduct;

    @Indexed
    Double precursorMz;

    /**
     * @see net.csibio.mslibrary.client.constants.enums.IonMode
     */
    @Indexed
    String ionMode;

    byte[] byteMzs;
    byte[] byteInts;

    @Transient
    double[] mzs;
    @Transient
    double[] ints;

    Date createDate;

    Date updateDate;

    /**
     * GNPS Item
     */
    String sourceFile;

    String task;

    Integer scan;

    String libraryMembership;

    @Indexed
    Integer spectrumStatus;

    String submitUser;

    String compoundSource;

    String pi;

    String dataCollector;

    Double exactMass;

    Integer charge;

    String casNumber;

    String pubmedId;

    String smiles;

    String inchI;

    String inchiAUX;

    Integer libraryClass;

    String taskId;

    String userId;

    String inchiKeySmiles;

    String inchiKeyInchi;

    String formulaSmiles;

    String formulaInchi;

    List<AnnotationHistory> annotationHistoryList;

    String url;

    /**
     * HMDB Item
     */
    @Indexed
    Double collisionEnergy;

    String notes;

    String sampleSource;

    boolean predicted;

    String structureId;

    /**
     * MassBank Item
     */
    String synon;

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
