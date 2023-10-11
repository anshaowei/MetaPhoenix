package net.csibio.mslibrary.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.csibio.aird.bean.common.Spectrum;
import net.csibio.mslibrary.client.algorithm.similarity.Similarity;
import net.csibio.mslibrary.client.constants.Constants;
import net.csibio.mslibrary.client.constants.enums.SpectrumMatchMethod;
import net.csibio.mslibrary.client.domain.bean.identification.LibraryHit;
import net.csibio.mslibrary.client.domain.db.MethodDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;
import net.csibio.mslibrary.client.service.LibraryHitService;
import net.csibio.mslibrary.client.service.LibraryService;
import net.csibio.mslibrary.client.service.SpectrumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service("libraryHitService")
public class LibraryHitServiceImpl implements LibraryHitService {

    @Autowired
    SpectrumService spectrumService;
    @Autowired
    LibraryService libraryService;

    @Override
    public List<LibraryHit> getAllHits(SpectrumDO querySpectrumDO, String libraryId, Double mzTolerance, boolean isDecoy, SpectrumMatchMethod spectrumMatchMethod, boolean precursorRemoval) {
        List<SpectrumDO> libSpectrumDOS = spectrumService.getByPrecursorMz(querySpectrumDO.getPrecursorMz(), mzTolerance, libraryId);
        List<LibraryHit> libraryHits = new ArrayList<>();
        for (SpectrumDO libSpectrumDO : libSpectrumDOS) {
            LibraryHit libraryHit = init(querySpectrumDO, libSpectrumDO, isDecoy);
            libraryHit.setScore(getScore(querySpectrumDO, libSpectrumDO, mzTolerance, spectrumMatchMethod, precursorRemoval));
            libraryHits.add(libraryHit);
        }
        return libraryHits;
    }

    @Override
    public LibraryHit getBestHit(SpectrumDO querySpectrumDO, String libraryId, Double mzTolerance, boolean isDecoy, SpectrumMatchMethod spectrumMatchMethod, boolean precursorRemoval) {
        List<SpectrumDO> libSpectrumDOS = spectrumService.getByPrecursorMz(querySpectrumDO.getPrecursorMz(), mzTolerance, libraryId);
        Double maxScore = Double.MIN_VALUE;
        int index = -1;
        for (int i = 0; i < libSpectrumDOS.size(); i++) {
            double score = getScore(querySpectrumDO, libSpectrumDOS.get(i), mzTolerance, spectrumMatchMethod, precursorRemoval);
            if (score > maxScore) {
                maxScore = score;
                index = i;
            }
        }
        if (index != -1) {
            LibraryHit libraryHit = init(querySpectrumDO, libSpectrumDOS.get(index), isDecoy);
            libraryHit.setScore(maxScore);
            return libraryHit;
        } else {
            return null;
        }
    }

    @Override
    public ConcurrentHashMap<SpectrumDO, List<LibraryHit>> getAllHitsMap(List<SpectrumDO> querySpectrumDOS, String libraryId, Double mzTolerance, boolean isDecoy, SpectrumMatchMethod spectrumMatchMethod, boolean precursorRemoval) {
        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap = new ConcurrentHashMap<>();
        querySpectrumDOS.parallelStream().forEach(spectrumDO -> {
            List<LibraryHit> libraryHits = getAllHits(spectrumDO, libraryId, mzTolerance, isDecoy, spectrumMatchMethod, precursorRemoval);
            hitsMap.put(spectrumDO, libraryHits);
        });
        return hitsMap;
    }

    @Override
    public ConcurrentHashMap<SpectrumDO, List<LibraryHit>> getAllHitsMap(String queryLibraryId, String libraryId, Double mzTolerance, boolean isDecoy, SpectrumMatchMethod spectrumMatchMethod, boolean precursorRemoval) {
        return getAllHitsMap(spectrumService.getAllByLibraryId(queryLibraryId), libraryId, mzTolerance, isDecoy, spectrumMatchMethod, precursorRemoval);
    }

    @Override
    public ConcurrentHashMap<SpectrumDO, LibraryHit> getBestHitsMap(List<SpectrumDO> querySpectrumDOS, String libraryId, Double mzTolerance, boolean isDecoy, SpectrumMatchMethod spectrumMatchMethod, boolean precursorRemoval) {
        ConcurrentHashMap<SpectrumDO, LibraryHit> hitsMap = new ConcurrentHashMap<>();
        querySpectrumDOS.parallelStream().forEach(spectrumDO -> {
            LibraryHit libraryHit = getBestHit(spectrumDO, libraryId, mzTolerance, isDecoy, spectrumMatchMethod, precursorRemoval);
            hitsMap.put(spectrumDO, libraryHit);
        });
        return hitsMap;
    }

    @Override
    public ConcurrentHashMap<SpectrumDO, LibraryHit> getBestHitsMap(String queryLibraryId, String libraryId, Double mzTolerance, boolean isDecoy, SpectrumMatchMethod spectrumMatchMethod, boolean precursorRemoval) {
        return getBestHitsMap(spectrumService.getAllByLibraryId(queryLibraryId), libraryId, mzTolerance, isDecoy, spectrumMatchMethod, precursorRemoval);
    }

    @Override
    public ConcurrentHashMap<SpectrumDO, List<LibraryHit>> getTargetDecoyHitsMap(List<SpectrumDO> querySpectrumDOS, String targetLibraryId, String decoyLibraryId, MethodDO methodDO) {
        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap = new ConcurrentHashMap<>();
        querySpectrumDOS.parallelStream().forEach(spectrumDO -> {
            List<LibraryHit> targetDecoyHits = new ArrayList<>();
            double mzTolerance = methodDO.getPpmForMzTolerance() ? methodDO.getPpm() * spectrumDO.getPrecursorMz() * Constants.PPM : methodDO.getMzTolerance();
            List<LibraryHit> targetHits = getAllHits(spectrumDO, targetLibraryId, mzTolerance, false, methodDO.getSpectrumMatchMethod(), methodDO.isPrecursorRemoval());
            List<LibraryHit> decoyHits = getAllHits(spectrumDO, decoyLibraryId, mzTolerance, true, methodDO.getSpectrumMatchMethod(), methodDO.isPrecursorRemoval());
            targetDecoyHits.addAll(targetHits);
            targetDecoyHits.addAll(decoyHits);
            hitsMap.put(spectrumDO, targetDecoyHits);
        });
        return hitsMap;
    }

    @Override
    public ConcurrentHashMap<SpectrumDO, List<LibraryHit>> getTargetDecoyHitsMap(String queryLibraryId, String targetLibraryId, String decoyLibraryId, MethodDO methodDO) {
        return getTargetDecoyHitsMap(spectrumService.getAllByLibraryId(queryLibraryId), targetLibraryId, decoyLibraryId, methodDO);
    }

    @Override
    public ConcurrentHashMap<SpectrumDO, List<LibraryHit>> getTargetDecoyBestHitsMap(List<SpectrumDO> querySpectrumDOS, String targetLibraryId, String decoyLibraryId, MethodDO methodDO) {
        ConcurrentHashMap<SpectrumDO, List<LibraryHit>> hitsMap = new ConcurrentHashMap<>();
        querySpectrumDOS.parallelStream().forEach(spectrumDO -> {
            List<LibraryHit> bestTargetDecoyHits = new ArrayList<>();
            double mzTolerance = methodDO.getPpmForMzTolerance() ? methodDO.getPpm() * spectrumDO.getPrecursorMz() * Constants.PPM : methodDO.getMzTolerance();
            LibraryHit bestTargetHit = getBestHit(spectrumDO, targetLibraryId, mzTolerance, false, methodDO.getSpectrumMatchMethod(), methodDO.isPrecursorRemoval());
            LibraryHit bestDecoyHit = getBestHit(spectrumDO, decoyLibraryId, mzTolerance, true, methodDO.getSpectrumMatchMethod(), methodDO.isPrecursorRemoval());
            bestTargetDecoyHits.add(bestTargetHit);
            bestTargetDecoyHits.add(bestDecoyHit);
            hitsMap.put(spectrumDO, bestTargetDecoyHits);
        });
        return hitsMap;
    }

    @Override
    public ConcurrentHashMap<SpectrumDO, List<LibraryHit>> getTargetDecoyBestHitsMap(String queryLibraryId, String targetLibraryId, String decoyLibraryId, MethodDO methodDO) {
        return getTargetDecoyBestHitsMap(spectrumService.getAllByLibraryId(queryLibraryId), targetLibraryId, decoyLibraryId, methodDO);
    }

    private LibraryHit init(SpectrumDO querySpectrumDO, SpectrumDO libSpectrumDO, boolean isDecoy) {
        LibraryHit libraryHit = new LibraryHit();
        libraryHit.setQuerySpectrumId(querySpectrumDO.getId());
        libraryHit.setLibSpectrumId(libSpectrumDO.getId());
        libraryHit.setCompoundName(libSpectrumDO.getCompoundName());
        libraryHit.setLibraryId(libSpectrumDO.getLibraryId());
        libraryHit.setDecoy(isDecoy);
        libraryHit.setPrecursorAdduct(libSpectrumDO.getPrecursorAdduct());
        libraryHit.setPrecursorMz(libSpectrumDO.getPrecursorMz());
        libraryHit.setSmiles(libSpectrumDO.getSmiles());
        libraryHit.setInChIKey(libSpectrumDO.getInChIKey());
        return libraryHit;
    }

    private Double getScore(SpectrumDO querySpectrumDO, SpectrumDO libSpectrumDO, Double mzTolerance, SpectrumMatchMethod spectrumMatchMethod, Boolean precursorRemoval) {
        Spectrum querySpectrum = precursorRemoval ? querySpectrumDO.getPrecursorRemovedSpectrum() : querySpectrumDO.getSpectrum();
        Spectrum libSpectrum = precursorRemoval ? libSpectrumDO.getPrecursorRemovedSpectrum() : libSpectrumDO.getSpectrum();
        return switch (spectrumMatchMethod) {
            case Entropy -> Similarity.getScore(querySpectrum, libSpectrum, SpectrumMatchMethod.Entropy, mzTolerance);
            case Cosine -> Similarity.getScore(querySpectrum, libSpectrum, SpectrumMatchMethod.Cosine, mzTolerance);
            case Cosine_SquareRoot ->
                    Similarity.getScore(querySpectrum, libSpectrum, SpectrumMatchMethod.Cosine_SquareRoot, mzTolerance);
            case Unweighted_Entropy ->
                    Similarity.getScore(querySpectrum, libSpectrum, SpectrumMatchMethod.Unweighted_Entropy, mzTolerance);
            case MetaPro -> Similarity.getScore(querySpectrum, libSpectrum, SpectrumMatchMethod.MetaPro, mzTolerance);
            case Weighted_Cosine ->
                    Similarity.getScore(querySpectrum, libSpectrum, SpectrumMatchMethod.Weighted_Cosine, mzTolerance);
        };
    }

}
