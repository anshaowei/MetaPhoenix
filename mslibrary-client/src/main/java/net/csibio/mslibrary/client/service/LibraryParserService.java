package net.csibio.mslibrary.client.service;

import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.LibraryDO;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

public interface LibraryParserService {

    /**
     *
     * @param in
     * @param library
     * @param fileFormat 读取的格式, 1代表excel, 2代表csv
     * @return
     */
    Result parse(InputStream in, LibraryDO library, int fileFormat);

    Result parseHMDB(String filePath);

    Result parseMassBank(String filePath);

    Result parseGNPS(String filePath) throws IOException, ParseException;

}
