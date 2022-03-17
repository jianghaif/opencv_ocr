package com.yz.edu.answercard;

import com.yz.edu.answercard.common.R;
import com.yz.edu.answercard.service.CharacterNumberOCR;
import com.yz.edu.answercard.service.CharacterOCR;
import org.opencv.core.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
public class AnswerCardApplication
{
    @Autowired
    private CharacterOCR characterOCR;
    @Autowired
    private CharacterNumberOCR characterNumberOCR;

    public static void main(String[] args)
    {
        SpringApplication.run(AnswerCardApplication.class, args);
    }

//    @Bean
//    public void  loadOCRLibrary(){
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//    }
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    /**
     * 初始化模型
     */
    @Bean
    public void initKN()
    {
        //初始化模型
       characterOCR.resetKN();
       characterNumberOCR.resetKN();
    }
}
