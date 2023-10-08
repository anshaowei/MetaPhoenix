package net.csibio.mslibrary.client.service;

import net.csibio.mslibrary.client.constants.enums.SpectrumMatchMethod;
import net.csibio.mslibrary.client.domain.bean.identification.LibraryHit;
import net.csibio.mslibrary.client.domain.db.MethodDO;
import net.csibio.mslibrary.client.domain.db.SpectrumDO;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public interface LibraryHitService {

    List<LibraryHit> getAllHits(SpectrumDO querySpectrumDO, String libraryId, Double mzTolerance, boolean isDecoy, SpectrumMatchMethod spectrumMatchMethod, boolean precursorRemoval);

    LibraryHit getBestHit(SpectrumDO querySpectrumDO, String libraryId, Double mzTolerance, boolean isDecoy, SpectrumMatchMethod spectrumMatchMethod, boolean precursorRemoval);

    ConcurrentHashMap<SpectrumDO, List<LibraryHit>> getAllHitsMap(List<SpectrumDO> querySpectrumDOS, String libraryId, Double mzTolerance, boolean isDecoy, SpectrumMatchMethod spectrumMatchMethod, boolean precursorRemoval);

    ConcurrentHashMap<SpectrumDO, List<LibraryHit>> getAllHitsMap(String queryLibraryId, String libraryId, Double mzTolerance, boolean isDecoy, SpectrumMatchMethod spectrumMatchMethod, boolean precursorRemoval);

    ConcurrentHashMap<SpectrumDO, LibraryHit> getBestHitsMap(List<SpectrumDO> querySpectrumDOS, String libraryId, Double mzTolerance, boolean isDecoy, SpectrumMatchMethod spectrumMatchMethod, boolean precursorRemoval);

    ConcurrentHashMap<SpectrumDO, LibraryHit> getBestHitsMap(String queryLibraryId, String libraryId, Double mzTolerance, boolean isDecoy, SpectrumMatchMethod spectrumMatchMethod, boolean precursorRemoval);

    ConcurrentHashMap<SpectrumDO, List<LibraryHit>> getTargetDecoyHitsMap(List<SpectrumDO> querySpectrumDOS, String targetLibraryId, String decoyLibraryId, MethodDO methodDO);

    ConcurrentHashMap<SpectrumDO, List<LibraryHit>> getTargetDecoyHitsMap(String queryLibraryId, String targetLibraryId, String decoyLibraryId, MethodDO methodDO);

    ConcurrentHashMap<SpectrumDO, List<LibraryHit>> getTargetDecoyBestHitsMap(List<SpectrumDO> querySpectrumDOS, String targetLibraryId, String decoyLibraryId, MethodDO methodDO);

    ConcurrentHashMap<SpectrumDO, List<LibraryHit>> getTargetDecoyBestHitsMap(String queryLibraryId, String targetLibraryId, String decoyLibraryId, MethodDO methodDO);
}
