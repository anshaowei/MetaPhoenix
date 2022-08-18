package net.csibio.mslibrary.client.parser.gnps;

import lombok.extern.slf4j.Slf4j;
import net.csibio.mslibrary.client.constants.enums.ResultCode;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.Adduct;
import net.csibio.mslibrary.client.domain.db.CompoundDO;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CompoundGenerator {

    @Autowired
    CompoundService compoundService;
    @Autowired
    LibraryService libraryService;
    @Autowired
    SpectrumService spectrumService;

    /**
     * 以InChI作为唯一标识符来生成化合物，InChI为空的谱图不被计算
     *
     * @param libraryIds
     * @return
     */
    public Result generateByInChI(List<String> libraryIds) {
        List<LibraryDO> libraryDOList = libraryService.getAllByIds(libraryIds);
        if (libraryDOList == null || libraryDOList.size() == 0) {
            return new Result(false);
        }
        for (LibraryDO libraryDO : libraryDOList) {
            List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryDO.getId());
            if (spectrumDOS.size() == 0 || spectrumDOS == null) {
                continue;
            }
            List<CompoundDO> compoundDOS = new ArrayList<>();
            spectrumDOS.removeIf(spectrumDO -> spectrumDO.getInchi().isEmpty() || spectrumDO.getInchi() == null);

            Map<String, List<SpectrumDO>> dataMap = spectrumDOS.stream().collect(Collectors.groupingBy(SpectrumDO::getInchi));
            for (String inchi : dataMap.keySet()) {
                CompoundDO compoundDO = new CompoundDO();
                List<SpectrumDO> currentSpectrumDOS = dataMap.get(inchi);

                //计算化合物加和物
                HashSet<Adduct> adducts = new HashSet<>();
                HashSet<String> synonyms = new HashSet<>();
                for (SpectrumDO spectrumDO : currentSpectrumDOS) {
                    Adduct adduct = new Adduct();
                    adduct = adduct.parse(spectrumDO.getAdduct());
                    if (adduct != null) {
                        adducts.add(adduct);
                    }
                    synonyms.add(spectrumDO.getCompoundName());
                }
                compoundDO.setLibraryId(libraryDO.getId());
                compoundDO.setCount(currentSpectrumDOS.size());
                compoundDO.setName(spectrumDOS.get(0).getCompoundName());
                compoundDO.setSynonyms(new ArrayList<>(synonyms));
                compoundDO.setFormula(spectrumDOS.get(0).getFormulaSmiles());
                compoundDO.setInchi(inchi);
                compoundDO.setInchikey(spectrumDOS.get(0).getInchiKeyInchi());
                compoundDO.setAvgMw(spectrumDOS.get(0).getExactMass());
                compoundDO.setMonoMw(spectrumDOS.get(0).getExactMass());
                compoundDO.setAdducts(adducts);
                compoundDO.setSmiles(spectrumDOS.get(0).getSmiles());
                compoundDO.setPubChemId(spectrumDOS.get(0).getPubmedId());
                compoundDOS.add(compoundDO);
            }
            libraryDO.setCompoundCount(compoundDOS.size());
            libraryService.update(libraryDO);
            compoundService.insert(compoundDOS, libraryDO.getId());
            log.info(libraryDO.getId() + "库的化合物已经生成并更新至数据库");
        }
        return new Result(true);
    }

    /**
     * 以化合物名称作为唯一标识符来生成化合物
     *
     * @param libraryIds
     * @return
     */
    public Result generateByName(List<String> libraryIds) {
        for (String libraryId : libraryIds) {
            LibraryDO libraryDO = libraryService.getById(libraryId);
            List<CompoundDO> compoundDOS = new ArrayList<>();
        }

        return new Result();
    }


}
