package com.yz.edu.answercard.controllers;

import com.yz.edu.answercard.common.R;
import com.yz.edu.answercard.common.beans.LetterInfo;
import com.yz.edu.answercard.service.CharacterOCR;
import com.yz.edu.answercard.service.PathManager;
import com.yz.edu.answercard.service.PictrueRectService;
import com.yz.edu.answercard.service.YzCardService;
import org.opencv.core.Rect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/")
public class AnswerCardController
{
    @Autowired
    private PathManager pathManager;

    @Autowired
    private YzCardService yzCardService;
    @Autowired
    private PictrueRectService pictrueRectService;
    @ResponseBody
    @RequestMapping("recognition")
    public R regAnswerCardPic(@RequestParam("file") MultipartFile file)
    {
        String id = UUID.randomUUID().toString();
        String inputPath = pathManager.getInputDir() + id + ".jpg";
        String outputPath = pathManager.getOutputDir() + id + ".jpg";
        try
        {
            file.transferTo(new File(inputPath));
        }
        catch (IOException e)
        {
            return R.error("文件写入服务器失败");
        }
        List<String> recognition = yzCardService.recognition(inputPath,outputPath,id);

        return R.ok()
                .add("id",id )
                .add("answerList",recognition);
    }


    /**
     * 上传文件识别图片字符区域
     * @param file
     * @return
     */
    @ResponseBody
    @RequestMapping("/getPicInfo.json")
    public R ocr(@RequestParam("file") MultipartFile file)
    {
        String id = UUID.randomUUID().toString();
        String inputPath = pathManager.getInputDir() + id + ".png";
        try
        {
            file.transferTo(new File(inputPath));
        }
        catch (IOException e)
        {
            return R.error("文件写入服务器失败");
        }
        List<Rect> letterInfoList = pictrueRectService.getPicInfo(inputPath);
        return R.ok().add("result",letterInfoList);
    }
}
