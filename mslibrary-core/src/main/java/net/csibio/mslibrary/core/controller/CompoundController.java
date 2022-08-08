package net.csibio.mslibrary.core.controller;

import net.csibio.aird.bean.common.IdName;
import net.csibio.aird.constant.SymbolConst;
import net.csibio.mslibrary.client.constants.enums.ResultCode;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.common.LabelValue;
import net.csibio.mslibrary.client.domain.db.CompoundDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.CompoundQuery;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import net.csibio.mslibrary.client.domain.vo.CompoundUpdateVO;
import net.csibio.mslibrary.client.domain.vo.SpectrumVO;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.service.BaseService;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.csibio.mslibrary.client.constants.AdductConst.ESIAdducts;

@RestController
@RequestMapping("compound")
public class CompoundController extends BaseController<CompoundDO, CompoundQuery> {

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
        return compoundService.insert(newCompound);
    }

    @RequestMapping(value = "/update")
    Result update(CompoundUpdateVO targetUpdateVO) throws XException {
        CompoundDO compInDB = compoundService.tryGetById(targetUpdateVO.getId(), ResultCode.COMPOUND_NOT_EXISTED);
        BeanUtils.copyProperties(targetUpdateVO, compInDB);
        return compoundService.update(compInDB);
    }

    @Override
    BaseService<CompoundDO, CompoundQuery> getBaseService() {
        return compoundService;
    }

    @RequestMapping(value = "/list")
    Result list(CompoundQuery query) {
        Result<List<CompoundDO>> result = compoundService.getList(query);
        result.getFeatureMap().put("ESIAdducts", ESIAdducts);
        return result;
    }

    @RequestMapping(value = "/fetchCompoundLabelValues")
    Result fetchCompoundLabelValues(@RequestParam(value = "libraryId", required = true) String libraryId, @RequestParam(value = "searchName", required = true) String searchName) {
        CompoundQuery query = new CompoundQuery();
        query.setLibraryId(libraryId);
        query.setSearchName(searchName);
        Result<List<IdName>> searchResult = compoundService.getList(query, IdName.class);
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
     * @param targetId
     * @return
     */
    @RequestMapping(value = "/getSpectraAll")
    Result getSpectraAll(@RequestParam(value = "targetId") String targetId) throws XException {
        CompoundDO compound = compoundService.tryGetById(targetId, ResultCode.COMPOUND_NOT_EXISTED);
        List<SpectrumDO> spectraList = spectrumService.getAll(new SpectrumQuery(compound.getId()));
        List<SpectrumVO> targetList = new ArrayList<>();
        spectraList.forEach(spectraDO -> {
            SpectrumVO spectrumVO = new SpectrumVO();
            BeanUtils.copyProperties(spectraDO, spectrumVO);
            targetList.add(spectrumVO);
        });

        check(targetList, ResultCode.SPECTRA_NOT_EXISTED);

        Map<String, List<SpectrumVO>> specMap = targetList.stream().collect(Collectors.groupingBy(
                SpectrumVO::getType));
        specMap.forEach((key, value) -> {
            value.sort(Comparator.comparing(SpectrumVO::getCreateDate).reversed());
        });
        Result result = new Result(true);
        result.setData(specMap);
        result.getFeatureMap().put("targetId", targetId);
        return result;
    }

    @RequestMapping(value = "/removeSpectras")
    Result removeSpectras(@RequestParam(value = "spectraIds") String spectraIds) {
        String[] spectraIdArray = spectraIds.split(SymbolConst.COMMA);
        Result result = new Result();
        List<String> errorList = new ArrayList<>();
        List<String> deletedIds = new ArrayList<>();
        for (String spectraId : spectraIdArray) {
            Result spectraResult = spectrumService.remove(spectraId);
            if (spectraResult.isSuccess()) {
                deletedIds.add(spectraId);
            } else {
                errorList.add("SpectraId:" + spectraId + "--" + spectraResult.getMsgInfo());
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
}
