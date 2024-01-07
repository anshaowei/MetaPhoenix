package net.csibio.mslibrary.core.components;

import net.csibio.mslibrary.client.algorithm.decoy.generator.SpectrumGenerator;
import net.csibio.mslibrary.client.algorithm.identification.Identify;
import net.csibio.mslibrary.client.filter.NoiseFilter;
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
import java.io.IOException;
import java.io.InputStreamReader;

@Component
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
    VMProperties vmProperties;

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String input = null;
            try {
                input = reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String[] params = input.split("\\s+");
            String command = params[0];

            if ("import".equals(command)) {
                importLibrary(params);
            } else if ("decoy".equals(command)) {
                decoy(params);
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
            if (params[i].equals("-n")) {
                libraryName = params[i + 1];
            }
        }
        if (libraryName == null || filePath == null) {
            System.out.print("Miss parameters");
        }
        mspParser.execute(filePath, libraryName);
    }

    public void decoy(String[] params) {

    }

}