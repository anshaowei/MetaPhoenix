package net.csibio.mslibrary.core.controller;


import net.csibio.mslibrary.client.parser.gnps.GnpsParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {

    @Autowired
    GnpsParser gnpsParser;

    @RequestMapping("/1")
    public void test1() {
        gnpsParser.parse("/Users/anshaowei/Downloads/database/ALL_GNPS.json");
    }
}
