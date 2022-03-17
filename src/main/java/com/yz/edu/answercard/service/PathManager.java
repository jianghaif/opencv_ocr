package com.yz.edu.answercard.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class PathManager
{

    @Value("${resource.dir}")
    private String resourceDir;


    public String getBaseDir(){
        return resourceDir + File.separator + "base"+File.separator;
    }
    public String getInputDir(){
        return resourceDir + File.separator + "input"+File.separator;
    }
    public String getProcessDir(String uuid){
        String s = resourceDir + File.separator + "process" + File.separator + uuid + File.separator;
        File file = new File(s);
        if(!file.exists()){
            file.mkdir();
        }
        return s;
    }
    public String getOutputDir(){
        return resourceDir + File.separator + "output"+File.separator;
    }
}
