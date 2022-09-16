package net.csibio.mslibrary.core.controller;

import net.csibio.mslibrary.client.constants.enums.ResultCode;
import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.bean.spectrum.SimpleSpectrum;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.domain.query.SpectrumQuery;
import net.csibio.mslibrary.client.exceptions.XException;
import net.csibio.mslibrary.client.service.CompoundService;
import net.csibio.mslibrary.client.service.SpectrumService;
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
    Result<SimpleSpectrum> getBaseSpectrum(@RequestParam(value = "spectrumId", required = true) String spectrumId, String libraryId) throws XException {
        SpectrumDO spectrumDO = spectrumService.tryGetById(spectrumId, libraryId, ResultCode.SPECTRA_NOT_EXISTED);
        SimpleSpectrum spectrum = new SimpleSpectrum(spectrumDO);
        return Result.build(spectrum);
    }

    /**
     * 根据给定的spectraId, 返回SpectraDO的信息
     *
     * @param spectraId SpectraDO的数据库id
     * @return SpectraDO对象
     */
    @RequestMapping("/detail")
    Result detail(@RequestParam(value = "spectraId", required = true) String spectraId, String libraryId) throws XException {
        SpectrumDO spectra = spectrumService.tryGetById(spectraId, libraryId, ResultCode.SPECTRUM_NOT_EXISTED);
        return Result.build(spectra);
    }

    @RequestMapping("/list")
    Result list(SpectrumQuery query) {
        Result<List<SpectrumDO>> res = spectrumService.getList(query, "HMDB");
        return res;
    }

}
