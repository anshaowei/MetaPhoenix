package net.csibio.mslibrary.core.components;

import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.aird.enums.MsLevel;
import net.csibio.mslibrary.client.algorithm.decoy.generator.SpectrumGenerator;
import net.csibio.mslibrary.client.algorithm.identification.Identify;
import net.csibio.mslibrary.client.constants.enums.DecoyStrategy;
import net.csibio.mslibrary.client.constants.enums.SpectrumMatchMethod;
import net.csibio.mslibrary.client.domain.bean.identification.LibraryHit;
import net.csibio.mslibrary.client.domain.db.MethodDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.filter.NoiseFilter;
import net.csibio.mslibrary.client.parser.common.MgfParser;
import net.csibio.mslibrary.client.parser.common.MspParser;
import net.csibio.mslibrary.client.parser.common.MzMLParser;
import net.csibio.mslibrary.client.parser.massbank.MassBankParser;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import net.csibio.mslibrary.core.config.VMProperties;
import net.csibio.mslibrary.core.export.Exporter;
import net.csibio.mslibrary.core.export.Reporter;
import net.csibio.mslibrary.core.sirius.Sirius;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
public class CommandRunner implements Runnable {

    @Autowired
    SpectrumService spectrumService;
    @Autowired
    LibraryService libraryService;
    @Autowired
    MassBankParser massBankParser;
    @Autowired
    SpectrumGenerator spectrumGenerator;
    @Autowired
    Reporter reporter;
    @Autowired
    NoiseFilter noiseFilter;
    @Autowired
    MspParser mspParser;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    Exporter exporter;
    @Autowired
    Sirius sirius;
    @Autowired
    MzMLParser mzMLParser;
    @Autowired
    Identify identify;
    @Autowired
    MgfParser mgfParser;
    @Autowired
    VMProperties vmProperties;

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String input = null;
            try {
                input = reader.readLine();
            } catch (Exception e) {
                log.info("Error occurred");
            }
            String[] params = input.split("\\s+");
            String command = params[0];

            if ("import".equals(command)) {
                importLibrary(params);
            } else if ("decoy".equals(command)) {
                decoy(params);
            } else if ("filter".equals(command)) {
                filter(params);
            } else if ("identify".equals(command)) {
                identify(params);
            } else {
                System.out.print("No such command");
            }
        }
    }

    public void importLibrary(String[] params) {
        String libraryName = null, filePath = null;
        for (int i = 0; i < params.length; i++) {
            if (params[i].equals("-f")) {
                filePath = params[i + 1];
            }
            if (params[i].equals("-l")) {
                libraryName = params[i + 1];
            }
        }
        if (libraryName == null || filePath == null) {
            System.out.print("Missing parameters");
        }
        mspParser.execute(filePath, libraryName);
    }

    public void filter(String[] params) {
        String libraryName = null;
        for (int i = 0; i < params.length; i++) {
            if (params[i].equals("-l")) {
                libraryName = params[i + 1];
            }
        }
        if (libraryName == null) {
            System.out.print("Missing parameters");
        }
        noiseFilter.filter(libraryName);
    }


    public void decoy(String[] params) {
        String libraryName = null;
        for (int i = 0; i < params.length; i++) {
            if (params[i].equals("-l")) {
                libraryName = params[i + 1];
            }
        }
        if (libraryName == null) {
            System.out.print("Missing parameters");
        }
        MethodDO methodDO = new MethodDO();
        methodDO.setPpm(10);
        methodDO.setPpmForMzTolerance(true);
        methodDO.setDecoyStrategy(DecoyStrategy.IonEntropyBased.getName());
        spectrumGenerator.execute(libraryName, methodDO);
    }

    public void identify(String[] params) {
        String libraryName = null, filePath = null;
        Double fdr = null;
        for (int i = 0; i < params.length; i++) {
            if (params[i].equals("-f")) {
                filePath = params[i + 1];
            }
            if (params[i].equals("-l")) {
                libraryName = params[i + 1];
            }
            if (params[i].equals("-fdr")) {
                fdr = Double.parseDouble(params[i + 1]);
            }
        }
        if (libraryName == null || filePath == null || fdr == null) {
            System.out.print("Missing parameters");
        }
        String decoyLibraryId = libraryName + SymbolConst.DELIMITER + DecoyStrategy.IonEntropyBased.getName();
        MethodDO methodDO = new MethodDO();
        methodDO.setPpm(10);
        methodDO.setPpmForMzTolerance(true);
        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Entropy);

        List<SpectrumDO> querySpectrumDOS = new ArrayList<>();
        if (filePath.endsWith(".mgf")) {
            querySpectrumDOS = mgfParser.execute(filePath);
        } else if (filePath.endsWith(".mzML")) {
            querySpectrumDOS = mzMLParser.execute(filePath);
        } else {
            System.out.print("Unsupported file format");
        }

        //remove low quality spectra
        querySpectrumDOS.removeIf(spectrumDO -> spectrumDO.getMzs() == null || spectrumDO.getMzs().length == 0 ||
                spectrumDO.getInts() == null || spectrumDO.getInts().length == 0 ||
                spectrumDO.getPrecursorMz() == null || spectrumDO.getPrecursorMz() == 0);
        for (SpectrumDO spectrumDO : querySpectrumDOS) {
            List<Double> mzs = new ArrayList<>();
            List<Double> ints = new ArrayList<>();
            for (int i = 0; i < spectrumDO.getMzs().length; i++) {
                if (spectrumDO.getInts()[i] == 0d) {
                    continue;
                }
                mzs.add(spectrumDO.getMzs()[i]);
                ints.add(spectrumDO.getInts()[i]);
            }
            spectrumDO.setMzs(mzs.stream().mapToDouble(Double::doubleValue).toArray());
            spectrumDO.setInts(ints.stream().mapToDouble(Double::doubleValue).toArray());
        }
        querySpectrumDOS.removeIf(spectrumDO -> spectrumDO.getMsLevel() == null);
        querySpectrumDOS.removeIf(spectrumDO -> !spectrumDO.getMsLevel().equals(MsLevel.MS2.getCode()));

        //set querySpectrumID
        Integer m = 0;
        for (SpectrumDO spectrumDO : querySpectrumDOS) {
            spectrumDO.setId(m.toString());
            m++;
        }

        //identify
        HashMap<String, List<LibraryHit>> result = identify.execute(querySpectrumDOS, libraryName, decoyLibraryId, methodDO, fdr);

        //write the result into excel
        List<List<Object>> dataSheet = new ArrayList<>();
        for (String querySpectrumId : result.keySet()) {
            List<LibraryHit> libraryHits = result.get(querySpectrumId);
            for (LibraryHit libraryHit : libraryHits) {
                List<Object> row = new ArrayList<>();
                row.add(querySpectrumId);
                row.add(libraryHit.getCompoundName());
                row.add(libraryHit.getInChIKey());
                row.add(libraryHit.getPrecursorMz());
                row.add(libraryHit.getSmiles());
                row.add(libraryHit.getScore());
                row.add(libraryHit.getLibSpectrumId());
                row.add(libraryHit.getPrecursorAdduct());
                dataSheet.add(row);
            }
        }

        List<Object> header = Arrays.asList("querySpectrumId", "CompoundName", "InChIKey", "PrecursorMz", "Smiles", "Score", "LibrarySpectrumId", "PrecursorAdduct");
        dataSheet.add(0, header);
        String outputFilePath = "/Users/anshaowei/Downloads/report.xlsx";
        EasyExcel.write(outputFilePath).sheet("identification").doWrite(dataSheet);
    }

}