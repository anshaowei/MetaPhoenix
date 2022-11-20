package net.csibio.mslibrary.client.parser.hmdb;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.constants.enums.IonMode;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

@Component
@Slf4j
public class SpectrumParser {

    @Autowired
    SpectrumService spectrumService;

    /**
     * scan all the xml files in the directory and parse them
     *
     * @param directoryPath
     */
    public void parse(String directoryPath) {
        log.info("开始执行文件夹:" + directoryPath + "谱图解析");
        File file = new File(directoryPath);
        File[] files = file.listFiles();
        assert files != null;
        //search all .xml files
        for (File f : files) {
            if (f.isFile() && f.getName().endsWith(".xml")) {
                parseSingleXML(f.getAbsolutePath());
                log.info("解析文件并插入数据库完成:" + f.getName());
            }
        }
    }

    /**
     * parse single xml file to a spectrum
     *
     * @param filePath
     */
    public void parseSingleXML(String filePath) {
        try {
            File file = new File(filePath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            NodeList nodeList = document.getElementsByTagName("ms-ms");
            SpectrumDO spectrumDO = new SpectrumDO();
            spectrumDO.setLibraryId("HMDB");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                NodeList childNodes = node.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    //id
                    if (childNodes.item(j).getNodeName().equals("id")) {
                        spectrumDO.setSpectrumId(childNodes.item(j).getTextContent());
                    }
                    //notes
                    if (childNodes.item(j).getNodeName().equals("notes")) {
                        spectrumDO.setNotes(childNodes.item(j).getTextContent());
                    }
                    //sample-concentration
                    if (childNodes.item(j).getNodeName().equals("sample-concentration")) {
                    }
                    //solvent
                    if (childNodes.item(j).getNodeName().equals("solvent")) {
                    }
                    //sample-mass
                    if (childNodes.item(j).getNodeName().equals("sample-mass")) {
                    }
                    //sample-assessment
                    if (childNodes.item(j).getNodeName().equals("sample-assessment")) {
                    }
                    //sample-source
                    if (childNodes.item(j).getNodeName().equals("sample-source")) {
                        spectrumDO.setSampleSource(childNodes.item(j).getTextContent());
                    }
                    //collection-date
                    if (childNodes.item(j).getNodeName().equals("collection-date")) {
                    }
                    //instrument-type
                    if (childNodes.item(j).getNodeName().equals("instrument-type")) {
                        spectrumDO.setInstrument(childNodes.item(j).getTextContent());
                    }
                    //peak-counter
                    if (childNodes.item(j).getNodeName().equals("peak-counter")) {
                    }
                    //created-at
                    if (childNodes.item(j).getNodeName().equals("created-at")) {
                    }
                    //updated-at
                    if (childNodes.item(j).getNodeName().equals("updated-at")) {
                    }
                    //mono-mass
                    if (childNodes.item(j).getNodeName().equals("mono-mass")) {
                    }
                    //energy-filed
                    if (childNodes.item(j).getNodeName().equals("energy-filed")) {
                    }
                    //collision-energy-level
                    if (childNodes.item(j).getNodeName().equals("collision-energy-level")) {
                    }
                    //collision-energy-voltage
                    if (childNodes.item(j).getNodeName().equals("collision-energy-voltage")) {
//                        spectrumDO.setCollisionEnergy(Double.parseDouble(childNodes.item(j).getTextContent()));
                    }
                    //ionization-mode
                    if (childNodes.item(j).getNodeName().equals("ionization-mode")) {
                        if (childNodes.item(j).getTextContent().equalsIgnoreCase(IonMode.Positive.getName())) {
                            spectrumDO.setIonMode(IonMode.Positive.getName());
                        } else if (childNodes.item(j).getTextContent().equalsIgnoreCase(IonMode.Negative.getName())) {
                            spectrumDO.setIonMode(IonMode.Negative.getName());
                        }
                    }
                    //sample-concentration-units
                    if (childNodes.item(j).getNodeName().equals("sample-concentration-units")) {
                    }
                    //sample-mass-units
                    if (childNodes.item(j).getNodeName().equals("sample-mass-units")) {
                    }
                    //predicted
                    if (childNodes.item(j).getNodeName().equals("predicted")) {
                        spectrumDO.setPredicted(Boolean.parseBoolean(childNodes.item(j).getTextContent()));
                    }
                    //structure-id
                    if (childNodes.item(j).getNodeName().equals("structure-id")) {
                        spectrumDO.setStructureId(childNodes.item(j).getTextContent());
                    }
                    //splash-key
                    if (childNodes.item(j).getNodeName().equals("splash-key")) {
                        spectrumDO.setSplash(childNodes.item(j).getTextContent());
                    }
                    //chromatography-type
                    if (childNodes.item(j).getNodeName().equals("chromatography-type")) {
                    }
                    //analyzer-type
                    if (childNodes.item(j).getNodeName().equals("analyzer-type")) {
                    }
                    //ionization-type
                    if (childNodes.item(j).getNodeName().equals("ionization-type")) {
                        spectrumDO.setIonSource(childNodes.item(j).getTextContent());
                    }
                    //charge-type
                    if (childNodes.item(j).getNodeName().equals("charge-type")) {
                    }
                    //data-source
                    if (childNodes.item(j).getNodeName().equals("data-source")) {
                    }
                    //data-source-id
                    if (childNodes.item(j).getNodeName().equals("data-source-id")) {
                    }
                    //adduct
                    if (childNodes.item(j).getNodeName().equals("adduct")) {
                        spectrumDO.setAdduct(childNodes.item(j).getTextContent());
                    }
                    //adduct-type
                    if (childNodes.item(j).getNodeName().equals("adduct-type")) {
                    }
                    //adduct-mass
                    if (childNodes.item(j).getNodeName().equals("adduct-mass")) {
                    }
                    //database-id
                    if (childNodes.item(j).getNodeName().equals("database-id")) {
                        spectrumDO.setCompoundId(childNodes.item(j).getTextContent());
                    }
                    //references
                    if (childNodes.item(j).getNodeName().equals("references")) {
                    }
                    //ms-ms-peaks
                    if (childNodes.item(j).getNodeName().equals("ms-ms-peaks")) {
                        NodeList peaks = childNodes.item(j).getChildNodes();
                        double[] mzs = new double[(peaks.getLength() - 1) / 2];
                        double[] intensities = new double[(peaks.getLength() - 1) / 2];
                        int peakIndex = 0;
                        for (int k = 0; k < peaks.getLength(); k++) {
                            if (peaks.item(k).getNodeName().equals("ms-ms-peak")) {
                                NodeList peak = peaks.item(k).getChildNodes();
                                mzs[peakIndex] = Double.parseDouble(peak.item(5).getTextContent());
                                intensities[peakIndex] = Double.parseDouble(peak.item(7).getTextContent());
                                peakIndex++;
                            }
                        }
                        spectrumDO.setMzs(mzs);
                        spectrumDO.setInts(intensities);
                    }
                }
            }
            spectrumService.insert(spectrumDO, "HMDB");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
