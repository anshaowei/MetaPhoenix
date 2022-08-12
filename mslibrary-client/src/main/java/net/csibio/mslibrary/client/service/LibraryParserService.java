package net.csibio.mslibrary.client.service;

import net.csibio.mslibrary.client.domain.Result;
import net.csibio.mslibrary.client.domain.db.LibraryDO;
import net.csibio.mslibrary.client.exceptions.XException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

public interface LibraryParserService {

    /**
     * @param in
     * @param library
     * @param fileFormat 读取的格式, 1代表excel, 2代表csv
     * @return
     */
    Result parse(InputStream in, LibraryDO library, int fileFormat);


}
