package net.csibio.metaphoenix.core.controller;

import net.csibio.aird.bean.common.Spectrum;
import net.csibio.metaphoenix.client.constants.enums.ResultCode;
import net.csibio.metaphoenix.client.domain.Result;
import net.csibio.metaphoenix.client.domain.db.SpectrumDO;
import net.csibio.metaphoenix.client.domain.query.SpectrumQuery;
import net.csibio.metaphoenix.client.exceptions.XException;
import net.csibio.metaphoenix.client.service.CompoundService;
import net.csibio.metaphoenix.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("spectrum")
public class SpectrumController {

    @Autowired
    SpectrumService spectrumService;
    @Autowired
    CompoundService compoundService;

    @RequestMapping(value = "/getBaseSpectrum")
    Result<Spectrum> getBaseSpectrum(@RequestParam(value = "spectrumId", required = true) String spectrumId, String libraryId) throws XException {
        SpectrumDO spectrumDO = spectrumService.tryGetById(spectrumId, libraryId, ResultCode.SPECTRA_NOT_EXISTED);
        Spectrum spectrum = new Spectrum(spectrumDO.getMzs(), spectrumDO.getInts());
        return Result.build(spectrum);
    }

    /**
     * 根据给定的spectraId, 返回SpectraDO的信息
     *
     * @param id SpectraDO的数据库id
     * @return SpectraDO对象
     */
    @RequestMapping("/detail")
    Result detail(@RequestParam(value = "id") String id, @RequestParam(value = "routerId") String routerId) throws XException {
        SpectrumDO spectra = spectrumService.tryGetById(id, routerId, ResultCode.SPECTRUM_NOT_EXISTED);
        return Result.build(spectra);
    }

    @RequestMapping("/list")
    Result list(SpectrumQuery query) {
        if (query.getLibraryId() != null) {
            query.setLibraryId(query.getLibraryId());
        }
        Result<List<SpectrumDO>> res = spectrumService.getList(query, query.getLibraryId());
        if (res.isSuccess()){
            res.getData().forEach(SpectrumDO::decode);
        }
        return res;
    }

}
