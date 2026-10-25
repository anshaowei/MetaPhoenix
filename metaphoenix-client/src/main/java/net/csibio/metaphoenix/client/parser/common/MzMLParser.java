package net.csibio.metaphoenix.client.parser.common;

import io.github.msdk.datamodel.MsScan;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import io.github.msdk.io.mzml.data.MzMLCVParam;
import io.github.msdk.io.mzml.data.MzMLMsScan;
import io.github.msdk.io.mzml.data.MzMLPrecursorElement;
import io.github.msdk.io.mzml.data.MzMLRawDataFile;
import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.enums.MsLevel;
import net.csibio.metaphoenix.client.domain.db.SpectrumDO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("mzMLParser")
@Slf4j
public class MzMLParser {

    public List<SpectrumDO> execute(String filePath) {
        List<SpectrumDO> spectrumDOS = new ArrayList<>();
        MzMLFileImportMethod importer = new MzMLFileImportMethod(filePath);
        try {
            importer.execute();
        } catch (Exception e) {
            log.error("Error while parsing mzML file: {}", filePath);
            return null;
        }
        MzMLRawDataFile mzMLRawDataFile = (MzMLRawDataFile) importer.getResult();
        assert mzMLRawDataFile != null;
        List<MsScan> msScans = mzMLRawDataFile.getScans();
        for (MsScan msScan : msScans) {
            MzMLMsScan mzMLMsScan = (MzMLMsScan) msScan;
            SpectrumDO spectrumDO = new SpectrumDO();
            //mz and intensity values
            double[] intensityArray = new double[mzMLMsScan.getIntensityValues().length];
            for (int i = 0; i < mzMLMsScan.getIntensityValues().length; i++) {
                intensityArray[i] = mzMLMsScan.getIntensityValues()[i];
            }
            spectrumDO.setMzs(mzMLMsScan.getMzValues());
            spectrumDO.setInts(intensityArray);

            //precursor
            if (mzMLMsScan.getPrecursorList().getPrecursorElements().size() != 1) {
                continue;
            }
            MzMLPrecursorElement precursor = mzMLMsScan.getPrecursorList().getPrecursorElements().get(0);
            List<MzMLCVParam> mzMLCVParams = precursor.getSelectedIonList().get().getSelectedIonList().get(0).getCVParamsList();
            for (MzMLCVParam mzMLCVParam : mzMLCVParams) {
                if (mzMLCVParam.getName().get().equals("selected ion m/z")) {
                    spectrumDO.setPrecursorMz(Double.parseDouble(mzMLCVParam.getValue().get()));
                }
            }

            //MS Level
            spectrumDO.setMsLevel(MsLevel.MS2.getCode());
            spectrumDOS.add(spectrumDO);
        }
        return spectrumDOS;
    }
}
