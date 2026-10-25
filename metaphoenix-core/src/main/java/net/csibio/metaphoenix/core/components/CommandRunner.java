package net.csibio.metaphoenix.core.components;

import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.aird.enums.MsLevel;
import net.csibio.metaphoenix.client.algorithm.decoy.generator.SpectrumGenerator;
import net.csibio.metaphoenix.client.algorithm.identification.Identify;
import net.csibio.metaphoenix.client.constants.enums.DecoyStrategy;
import net.csibio.metaphoenix.client.constants.enums.SpectrumMatchMethod;
import net.csibio.metaphoenix.client.domain.bean.identification.LibraryHit;
import net.csibio.metaphoenix.client.domain.db.MethodDO;
import net.csibio.metaphoenix.client.domain.db.SpectrumDO;
import net.csibio.metaphoenix.client.domain.query.SpectrumQuery;
import net.csibio.metaphoenix.client.filter.NoiseFilter;
import net.csibio.metaphoenix.client.parser.common.MgfParser;
import net.csibio.metaphoenix.client.parser.common.MspParser;
import net.csibio.metaphoenix.client.parser.common.MzMLParser;
import net.csibio.metaphoenix.client.parser.massbank.MassBankParser;
import net.csibio.metaphoenix.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@Component
@Slf4j
public class CommandRunner implements Runnable {

    static final String USAGE = """
            Commands:
              import   -f <library.msp> -l <library> [-t <format>]
              filter   -l <library>
              decoy    -l <library> [-s <strategy>] [-ppm <ppm>] [-seed <seed>] [-overwrite]
              identify -f <spectra.mgf|spectra.mzML> -l <library> -fdr <fdr> [-s <strategy>] [-ppm <ppm>] [-o <report.xlsx>]
            Import formats (-t): msp (default), massbank (MassBank-Europe MSP), mona (MassBank-MoNA MSP)
            Decoy strategies (-s): Naive, XYMeta, SpectralEntropyBased, SpectralEntropyBased_PR, IonEntropyBased (default), IonEntropyBased_PR
            Defaults: -ppm 10, -o report.xlsx, no seed (a different decoy library on every run)""";

    static final int DEFAULT_PPM = 10;
    static final String DEFAULT_REPORT = "report.xlsx";
    static final Set<String> FLAGS = Set.of("-overwrite");

    @Autowired
    SpectrumService spectrumService;
    @Autowired
    SpectrumGenerator spectrumGenerator;
    @Autowired
    NoiseFilter noiseFilter;
    @Autowired
    MspParser mspParser;
    @Autowired
    MassBankParser massBankParser;
    @Autowired
    MzMLParser mzMLParser;
    @Autowired
    Identify identify;
    @Autowired
    MgfParser mgfParser;

    @Override
    public void run() {
        run(new BufferedReader(new InputStreamReader(System.in)));
    }

    /**
     * Read and execute commands until the input is closed. A failing command is reported and the next one is read.
     */
    void run(BufferedReader reader) {
        System.out.println(USAGE);
        while (true) {
            String input;
            try {
                input = reader.readLine();
            } catch (IOException e) {
                log.error("Failed to read command, command runner stopped", e);
                return;
            }
            if (input == null) {
                log.info("Input closed, command runner stopped");
                return;
            }
            try {
                execute(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid argument: " + e.getMessage());
            } catch (Exception e) {
                log.error("Command failed: {}", input, e);
            }
        }
    }

    void execute(String input) {
        String[] params = input.trim().split("\\s+");
        String command = params[0];
        if (command.isEmpty()) {
            return;
        }
        Map<String, String> options = parseOptions(params);
        switch (command) {
            case "import" -> importLibrary(options);
            case "filter" -> filter(options);
            case "decoy" -> decoy(options);
            case "identify" -> identify(options);
            default -> System.out.println("No such command: " + command + "\n" + USAGE);
        }
    }

    /**
     * Parse "-key value" pairs and the flags in {@link #FLAGS} (stored as "true") after the command name.
     */
    static Map<String, String> parseOptions(String[] params) {
        Map<String, String> options = new HashMap<>();
        for (int i = 1; i < params.length; i++) {
            String key = params[i];
            if (!key.startsWith("-")) {
                throw new IllegalArgumentException("unexpected argument " + key);
            }
            if (FLAGS.contains(key)) {
                options.put(key, "true");
            } else if (i + 1 < params.length) {
                options.put(key, params[++i]);
            } else {
                throw new IllegalArgumentException("missing value for " + key);
            }
        }
        return options;
    }

    /**
     * @return false (after printing the usage) if any of the keys is missing
     */
    static boolean require(Map<String, String> options, String... keys) {
        List<String> missing = Arrays.stream(keys).filter(key -> !options.containsKey(key)).toList();
        if (!missing.isEmpty()) {
            System.out.println("Missing parameters: " + String.join(" ", missing) + "\n" + USAGE);
            return false;
        }
        return true;
    }

    static DecoyStrategy parseStrategy(Map<String, String> options) {
        String name = options.getOrDefault("-s", DecoyStrategy.IonEntropyBased.getName());
        return Arrays.stream(DecoyStrategy.values()).filter(strategy -> strategy.getName().equals(name)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown decoy strategy " + name));
    }

    static MethodDO buildMethod(Map<String, String> options) {
        MethodDO methodDO = new MethodDO();
        methodDO.setPpm(options.containsKey("-ppm") ? Integer.parseInt(options.get("-ppm")) : DEFAULT_PPM);
        methodDO.setPpmForMzTolerance(true);
        methodDO.setDecoyStrategy(parseStrategy(options).getName());
        if (options.containsKey("-seed")) {
            methodDO.setRandomSeed(Long.parseLong(options.get("-seed")));
        }
        return methodDO;
    }

    public void importLibrary(Map<String, String> options) {
        if (!require(options, "-f", "-l")) {
            return;
        }
        String filePath = options.get("-f");
        String libraryName = options.get("-l");
        String format = options.getOrDefault("-t", "msp");
        switch (format) {
            case "msp" -> mspParser.execute(filePath, libraryName);
            case "massbank" -> massBankParser.parseMspEU(filePath, libraryName);
            case "mona" -> massBankParser.parseMspMoNA(filePath, libraryName);
            default -> throw new IllegalArgumentException("unknown import format " + format + ", use msp, massbank or mona");
        }
    }

    public void filter(Map<String, String> options) {
        if (!require(options, "-l")) {
            return;
        }
        noiseFilter.filter(options.get("-l"));
    }

    public void decoy(Map<String, String> options) {
        if (!require(options, "-l")) {
            return;
        }
        spectrumGenerator.execute(options.get("-l"), buildMethod(options), options.containsKey("-overwrite"));
    }

    public void identify(Map<String, String> options) {
        if (!require(options, "-f", "-l", "-fdr")) {
            return;
        }
        String filePath = options.get("-f");
        String libraryName = options.get("-l");
        double fdr = Double.parseDouble(options.get("-fdr"));
        String outputFilePath = options.getOrDefault("-o", DEFAULT_REPORT);
        MethodDO methodDO = buildMethod(options);
        methodDO.setSpectrumMatchMethod(SpectrumMatchMethod.Entropy);
        String decoyLibraryId = libraryName + SymbolConst.DELIMITER + methodDO.getDecoyStrategy();

        //without a decoy library every hit would pass the FDR threshold
        if (!spectrumService.exists(new SpectrumQuery(), libraryName)) {
            System.out.println("Library " + libraryName + " does not exist or is empty");
            return;
        }
        if (!spectrumService.exists(new SpectrumQuery(), decoyLibraryId)) {
            System.out.println("Decoy library " + decoyLibraryId + " does not exist, run: decoy -l " + libraryName + " -s " + methodDO.getDecoyStrategy());
            return;
        }

        List<SpectrumDO> querySpectrumDOS;
        if (filePath.endsWith(".mgf")) {
            querySpectrumDOS = mgfParser.execute(filePath);
        } else if (filePath.endsWith(".mzML")) {
            querySpectrumDOS = mzMLParser.execute(filePath);
        } else {
            System.out.println("Unsupported file format, use .mgf or .mzML: " + filePath);
            return;
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
        EasyExcel.write(outputFilePath).sheet("identification").doWrite(dataSheet);
        log.info("Identified {} of {} query spectra, report written to {}", result.size(), querySpectrumDOS.size(), outputFilePath);
    }
}
