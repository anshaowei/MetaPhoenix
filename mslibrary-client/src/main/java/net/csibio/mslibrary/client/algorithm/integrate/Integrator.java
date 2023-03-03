package net.csibio.mslibrary.client.algorithm.integrate;

import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.bean.common.Spectrum;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import net.csibio.mslibrary.client.utils.SpectrumUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("Integrator")
@Slf4j
public class Integrator {
    @Autowired
    SpectrumService spectrumService;
    @Autowired
    LibraryService libraryService;

    public void integrate(String libraryId) {
        log.info("Start integrating library: " + libraryId);
        String integratedLibraryId = libraryId + SymbolConst.DELIMITER + "integrated";

        //insert a new library
        LibraryDO libraryDO = libraryService.getById(libraryId);
        libraryDO.setId(null);
        libraryDO.setName(integratedLibraryId);
        try {
            libraryService.insert(libraryDO);
        } catch (Exception e) {
            log.error("integrated library already exists for {}", libraryId);
        }

        List<SpectrumDO> integratedSpectrumDOS = Collections.synchronizedList(new ArrayList<>());
        List<SpectrumDO> spectrumDOS = spectrumService.getAll(new SpectrumQuery(), libraryId);
        Map<String, List<SpectrumDO>> smilesMap = spectrumDOS.stream().collect(Collectors.groupingBy(SpectrumDO::getSmiles));
        smilesMap.keySet().parallelStream().forEach(smiles -> {
            List<SpectrumDO> spectrumDOList = smilesMap.get(smiles);
            if (spectrumDOList.size() > 1) {
                SpectrumDO spectrumDO = new SpectrumDO();
                //check if these spectra are same one specific items


                //merge all the target spectra
                Spectrum mergedSpectrum = new Spectrum(new double[0], new double[0]);
                for (SpectrumDO tempSpectrumDO : spectrumDOList) {
                    Spectrum tempSpectrum = new Spectrum(tempSpectrumDO.getMzs(), tempSpectrumDO.getInts());
                    mergedSpectrum = SpectrumUtil.mixByWeight(mergedSpectrum, tempSpectrum, 1, 1, 0.001);
                }
                for (int i = 0; i < mergedSpectrum.getInts().length; i++) {
                    mergedSpectrum.getInts()[i] = mergedSpectrum.getInts()[i] / spectrumDOList.size();
                }
                spectrumDO.setMzs(mergedSpectrum.getMzs());
                spectrumDO.setInts(mergedSpectrum.getInts());
                spectrumDO.setLibraryId(integratedLibraryId);
                spectrumDO.setSmiles(spectrumDOList.get(0).getSmiles());
                spectrumDO.setPrecursorMz(spectrumDOList.get(0).getPrecursorMz());
                integratedSpectrumDOS.add(spectrumDO);
            } else {
                //if there is only one spectrum with the smiles, then keep it
                SpectrumDO spectrumDO = spectrumDOList.get(0);
                spectrumDO.setLibraryId(integratedLibraryId);
                integratedSpectrumDOS.add(spectrumDO);
            }
        });
        spectrumService.insert(integratedSpectrumDOS, integratedLibraryId);
        log.info("Integrate library: {} success", libraryId);
    }
}
