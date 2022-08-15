package net.csibio.mslibrary.client.parser.gnps;

import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.CompoundDO;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CompoundGenerator {

    @Autowired
    CompoundService compoundService;
    @Autowired
    LibraryService libraryService;
    @Autowired
    SpectrumService spectrumService;

    /**
     * 以InChI作为唯一标识符来生成化合物
     *
     * @param libraryIds
     * @return
     */
    public Result generateByInChI(List<String> libraryIds) {
        List<CompoundDO> allCompounds = new ArrayList<>();
        for (String libraryId : libraryIds) {
            LibraryDO libraryDO = libraryService.getById(libraryId);
            List<SpectrumDO> spectrumDOS = spectrumService.getAllByLibraryId(libraryId);
            List<CompoundDO> compoundDOS = new ArrayList<>();
            spectrumDOS.removeIf(spectrumDO -> spectrumDO.getInchi().isEmpty() || spectrumDO.getInchi() == null);
            Map<String, List<SpectrumDO>> dataMap = spectrumDOS.stream().collect(Collectors.groupingBy(SpectrumDO::getInchi));
            for (String inchi : dataMap.keySet()) {
                CompoundDO compoundDO = new CompoundDO();
                compoundDO.setInchi(inchi);
                compoundDO.setLibraryId(libraryId);
            }
        }

        return new Result();
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
