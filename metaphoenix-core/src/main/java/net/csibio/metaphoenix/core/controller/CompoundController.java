package net.csibio.metaphoenix.core.controller;

import net.csibio.aird.bean.common.IdName;
import net.csibio.metaphoenix.client.constants.enums.ResultCode;
import net.csibio.metaphoenix.client.domain.Result;
import net.csibio.metaphoenix.client.domain.bean.common.LabelValue;
import net.csibio.metaphoenix.client.domain.db.CompoundDO;
import net.csibio.metaphoenix.client.domain.query.CompoundQuery;
import net.csibio.metaphoenix.client.domain.vo.CompoundUpdateVO;
import net.csibio.metaphoenix.client.exceptions.XException;
import net.csibio.metaphoenix.client.service.CompoundService;
import net.csibio.metaphoenix.client.service.LibraryService;
import net.csibio.metaphoenix.client.service.SpectrumService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("compound")
public class CompoundController {

    @Autowired
    CompoundService compoundService;
    @Autowired
    LibraryService libraryService;
    @Autowired
    SpectrumService spectrumService;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    Result add(CompoundUpdateVO compoundUpdate) throws XException {
        libraryService.tryGetById(compoundUpdate.getLibraryId(), ResultCode.LIBRARY_NOT_EXISTED);
        CompoundDO newCompound = new CompoundDO();
        BeanUtils.copyProperties(compoundUpdate, newCompound);
        return compoundService.insert(newCompound, compoundUpdate.getLibraryId());
    }

    @RequestMapping(value = "/update")
    Result update(CompoundUpdateVO compUpdateVO) throws XException {
        CompoundDO compInDB = compoundService.tryGetById(compUpdateVO.getId(), compUpdateVO.getLibraryId(), ResultCode.COMPOUND_NOT_EXISTED);
        BeanUtils.copyProperties(compUpdateVO, compInDB);
        return compoundService.update(compInDB, compInDB.getLibraryId());
    }

    @RequestMapping(value = "/list")
    Result list(CompoundQuery query) {
        query.setOrderBy(Sort.Direction.ASC);
        query.setSortColumn("hmdbId");
        Result<List<CompoundDO>> result = compoundService.getList(query, query.getLibraryId());
        return result;
    }

    @RequestMapping(value = "/detail")
    Result detail(@RequestParam(value = "routerId") String libraryId, @RequestParam(value = "id") String compoundId) throws XException {
        CompoundDO compound = compoundService.tryGetById(compoundId, libraryId, ResultCode.COMPOUND_NOT_EXISTED);
        compound.decode();
        return Result.OK(compound);
    }

    @RequestMapping("/remove")
    Result remove(@RequestParam(value = "ids") String[] ids, @RequestParam(value = "routerId") String libraryId) {
        Result<List<String>> result = new Result<List<String>>();
        List<String> errorList = new ArrayList<>();
        List<String> deletedIds = new ArrayList<>();
        for (String id : ids) {
            Result removeResult = compoundService.remove(id, libraryId);
            if (removeResult.isSuccess()) {
                deletedIds.add(id);
            } else {
                errorList.add(removeResult.getMsgInfo());
            }
        }
        if (deletedIds.size() != 0) {
            result.setData(deletedIds);
            result.setSuccess(true);
        }
        if (errorList.size() != 0) {
            result.setErrorList(errorList);
        }
        return result;
    }

    @RequestMapping(value = "/fetchCompoundLabelValues")
    Result fetchCompoundLabelValues(@RequestParam(value = "libraryId", required = true) String libraryId,
                                    @RequestParam(value = "searchName", required = true) String searchName) {
        CompoundQuery query = new CompoundQuery();
        query.setLibraryId(libraryId);
        query.setSearchName(searchName);
        Result<List<IdName>> searchResult = compoundService.getList(query, IdName.class, libraryId);
        if (searchResult.isFailed()) {
            return Result.OK(new ArrayList<>());
        }
        List<LabelValue> lvList = new ArrayList<>();
        for (IdName idName : searchResult.getData()) {
            lvList.add(new LabelValue(idName.name(), idName.id()));
        }
        Result<List<LabelValue>> result = new Result(true);
        result.setData(lvList);
        return result;
    }

    /**
     * 获取一个靶标所有的关联光谱以及对应的主库靶标的所有光谱
     *
     * @param compoundId
     * @param libraryId
     * @return
     */
//    @RequestMapping(value = "/getSpectraAll")
//    Result getSpectraAll(@RequestParam(value = "compoundId") String compoundId,
//                         @RequestParam(value = "libraryId") String libraryId) throws XException {
//        CompoundDO compound = compoundService.tryGetById(compoundId, libraryId, ResultCode.COMPOUND_NOT_EXISTED);
//        List<SpectrumDO> spectraList = spectrumService.getAll(new SpectrumQuery(compound.getId()));
//        List<SpectrumVO> compoundList = new ArrayList<>();
//        spectraList.forEach(spectraDO -> {
//            SpectrumVO spectrumVO = new SpectrumVO();
//            BeanUtils.copyProperties(spectraDO, spectrumVO);
//            compoundList.add(spectrumVO);
//        });
//
//        if (spectraList.isEmpty()) {
//            return Result.Error(ResultCode.SPECTRA_NOT_EXISTED);
//        }
//
//        Map<String, List<SpectrumVO>> specMap = compoundList.stream().collect(Collectors.groupingBy(
//                SpectrumVO::getType));
//        specMap.forEach((key, value) -> {
//            value.sort(Comparator.comparing(SpectrumVO::getCreateDate).reversed());
//        });
//        Result result = new Result(true);
//        result.setData(specMap);
//        result.getFeatureMap().put("compoundId", compoundId);
//        return result;
//    }

//    @RequestMapping(value = "/removeSpectras")
//    Result removeSpectras(@RequestParam(value = "spectraIds") String spectraIds) {
//        String[] spectraIdArray = spectraIds.split(SymbolConst.COMMA);
//        Result result = new Result();
//        List<String> errorList = new ArrayList<>();
//        List<String> deletedIds = new ArrayList<>();
//        for (String spectraId : spectraIdArray) {
//            Result spectraResult = spectrumService.remove(spectraId);
//            if (spectraResult.isSuccess()) {
//                deletedIds.add(spectraId);
//            } else {
//                errorList.add("SpectraId:" + spectraId + "--" + spectraResult.getMsgInfo());
//            }
//        }
//        if (deletedIds.size() != 0) {
//            result.setData(deletedIds);
//            result.setSuccess(true);
//        }
//        if (errorList.size() != 0) {
//            result.setErrorList(errorList);
//        }
//        return result;
//    }
}
