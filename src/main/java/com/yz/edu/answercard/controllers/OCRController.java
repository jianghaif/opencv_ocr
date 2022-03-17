package com.yz.edu.answercard.controllers;

import cn.hutool.core.util.ZipUtil;
import cn.hutool.http.HttpUtil;
import com.yz.edu.answercard.common.R;
import com.yz.edu.answercard.common.beans.LetterInfo;
import com.yz.edu.answercard.service.CharacterNumberOCR;
import com.yz.edu.answercard.service.CharacterOCR;
import com.yz.edu.answercard.service.PathManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/ocr")
@Slf4j
public class OCRController
{
    @Autowired
    private PathManager pathManager;

    @Autowired
    private CharacterOCR characterOCR;
    @Autowired
    private CharacterNumberOCR characterNumberOCR;

    /**
     * 增加纠错图片，到训练库
     * @return
     */
    @ResponseBody
    @RequestMapping("/initKN")
    public R initKN()
    {
        // 重新 初始化模型
        characterOCR.resetKN();

        return R.ok();
    }

    /**
     * 上传文件切图
     * @param file
     * @return
     */
    @ResponseBody
    @RequestMapping("/cut")
    public R cut(@RequestParam("file") MultipartFile file)
    {
        String id = UUID.randomUUID().toString();
        String inputPath = pathManager.getInputDir() + id + ".png";
        String outputDirPath = pathManager.getOutputDir() + id +"_output" + File.separator;
        new File(outputDirPath).mkdir();
        try
        {
            file.transferTo(new File(inputPath));
        }
        catch (IOException e)
        {
            return R.error("文件写入服务器失败");
        }
        characterOCR.cutAbcRct(inputPath,outputDirPath,id);

        ZipUtil.zip(outputDirPath, pathManager.getOutputDir() + id + ".zip");

        return R.ok().add("id",id );
    }

    /**
     * 切图
     * @return
     */
    @ResponseBody
    @RequestMapping("/cutByUrl")
    public R cut(@RequestParam("url") String url)
    {
        String id = UUID.randomUUID().toString();
        String inputPath = pathManager.getInputDir() + id + ".png";
        String outputDirPath = pathManager.getOutputDir() + id +"_output" + File.separator;
        new File(outputDirPath).mkdir();
        try
        {
            File file = new File(inputPath);
            HttpUtil.download(url, new FileOutputStream(file), true);
        }
        catch (IOException e)
        {
            return R.error("文件写入服务器失败");
        }
        characterOCR.cutAbcRct(inputPath,outputDirPath,id);

        ZipUtil.zip(outputDirPath, pathManager.getOutputDir() + id + ".zip");

        return R.ok().add("id",id );
    }


    /**
     * 上传文件识别--字母
     * @param file
     * @return
     */
    @ResponseBody
    @RequestMapping("/ocr")
    public R ocr(@RequestParam("file") MultipartFile file)
    {
        String id = UUID.randomUUID().toString();
        String inputPath = pathManager.getInputDir() + id + ".png";
        String outputDirPath = pathManager.getOutputDir() + id +"_output" + File.separator;
        new File(outputDirPath).mkdir();
        try
        {
            file.transferTo(new File(inputPath));
        }
        catch (IOException e)
        {
            return R.error("文件写入服务器失败");
        }

        List<LetterInfo> letterInfoList = characterOCR.ocrAbc(inputPath, outputDirPath, id);

        return R.ok().add("id",id ).add("result",letterInfoList);
    }
    /**
     * 上传文件识别--字母
     * @return
     */
    @ResponseBody
    @RequestMapping("/ocrByUrl")
    public R ocrByUrl(@RequestParam("url") String url)
    {
        String id = UUID.randomUUID().toString();
        String inputPath = pathManager.getInputDir() + id + ".png";
        String outputDirPath = pathManager.getOutputDir() + id +"_output" + File.separator;
        new File(outputDirPath).mkdir();
        try
        {
            File file = new File(inputPath);
            HttpUtil.download(url, new FileOutputStream(file), true);
        }
        catch (IOException e)
        {
            return R.error("文件写入服务器失败");
        }
        List<LetterInfo> letterInfoList = characterOCR.ocrAbc(inputPath, outputDirPath, id);

        return R.ok().add("id",id ).add("result",letterInfoList);
    }
    /**
     * 上传文件识别--数字
     * @param file
     * @return
     */
    @ResponseBody
    @RequestMapping("/ocrNumber")
    public R ocrNumber(@RequestParam("file") MultipartFile file)
    {
        String id = UUID.randomUUID().toString();
        String inputPath = pathManager.getInputDir() + id + ".png";
        String outputDirPath = pathManager.getOutputDir() + id +"_output" + File.separator;
        log.info("inputPath=="+inputPath);
        log.info("outputDirPath=="+outputDirPath);
        new File(outputDirPath).mkdir();
        try
        {
            file.transferTo(new File(inputPath));
        }
        catch (IOException e)
        {
            return R.error("文件写入服务器失败");
        }

        List<LetterInfo> letterInfoList = characterNumberOCR.ocrNumber(inputPath, outputDirPath, id);

        return R.ok().add("id",id ).add("result",letterInfoList);
    }
    /**
     * 上传文件识别--数字
     * @return
     */
    @ResponseBody
    @RequestMapping("/ocrNumberByUrl")
    public R ocrNumberByUrl(@RequestParam("url") String url)
    {
        String id = UUID.randomUUID().toString();
        String inputPath = pathManager.getInputDir() + id + ".png";
        String outputDirPath = pathManager.getOutputDir() + id +"_output" + File.separator;
        new File(outputDirPath).mkdir();
        try
        {
            File file = new File(inputPath);
            HttpUtil.download(url, new FileOutputStream(file), true);
        }
        catch (IOException e)
        {
            return R.error("文件写入服务器失败");
        }
        //List<LetterInfo> letterInfoList = characterOCR.ocrAbc(inputPath, outputDirPath, id);
        List<LetterInfo> letterInfoList = characterNumberOCR.ocrNumber(inputPath, outputDirPath, id);
        return R.ok().add("id",id ).add("result",letterInfoList);
    }


    /**
     * 增加纠错图片，到训练库
     * @return
     */
    @ResponseBody
    @RequestMapping("/addNewSource")
    public R addNewSource(@RequestParam("value") String value, @RequestParam("filepath") String filepath)
    {
        List<Integer> num = Arrays.asList(0,1,2,3,4,5,6,7,8,9);
        // 使用 图片 转化到训练库
        String outputDirPath = pathManager.getOutputDir();
        value = StringUtils.trim(value);
//        if (value == null  || !value.matches("^[A-Z]$") ||!num.contains(Integer.valueOf(value)))
//        {
//            return R.error("识别结果只能输入A-Z的大写字母和0-9的数字");
//        }
        if(num.contains(Integer.valueOf(value))){
            characterNumberOCR.createSourceImage(outputDirPath + filepath, value);
            // 重新 初始化模型
            characterNumberOCR.resetKN();
        }else {
            characterOCR.createSourceImage(outputDirPath + filepath, value);
            // 重新 初始化模型
            characterOCR.resetKN();
        }
        return R.ok();
    }
    /**
     * 增加纠错图片，到训练库 批量
     * @return
     */
    @ResponseBody
    @RequestMapping("/addNewSourceBatch")
    public R addNewSourceBatch(@RequestParam("value") String value, @RequestParam("filepath") String filepath)
    {
        List<Integer> num = Arrays.asList(0,1,2,3,4,5,6,7,8,9);
        // 使用 图片 转化到训练库
        List<File> files = getFiles(filepath);
        value = StringUtils.trim(value);
        if(num.contains(Integer.valueOf(value))){
            for(File f : files) {
                characterNumberOCR.createSourceImage(f.getPath(), value);
            }
        }else {
            for(File f : files) {
                characterOCR.createSourceImage(f.getPath(), value);
            }
        }
        if(num.contains(Integer.valueOf(value))){
            // 重新 初始化模型
            characterNumberOCR.resetKN();
        }else {
            // 重新 初始化模型
            characterOCR.resetKN();
        }
        return R.ok();
    }

    public static List<File> getFiles(String path){
        File root = new File(path);
        List<File> files = new ArrayList<File>();
        if(!root.isDirectory()){
            files.add(root);
        }else{
            File[] subFiles = root.listFiles();
            for(File f : subFiles){
                files.addAll(getFiles(f.getAbsolutePath()));
            }
        }
        return files;
    }

    /**
     * 上传jpg文件识别
     * @param file
     * @return
     */
    @ResponseBody
    @RequestMapping("/ocrJpg")
    public R ocrJpg(@RequestParam("file") MultipartFile file)
    {
        String id = UUID.randomUUID().toString();
        String inputPath = pathManager.getInputDir() + id + ".png";
        String outputDirPath = pathManager.getOutputDir() + id +"_output" + File.separator;
        new File(outputDirPath).mkdir();
        try
        {
            file.transferTo(new File(inputPath));
        }
        catch (IOException e)
        {
            return R.error("文件写入服务器失败");
        }

        List<LetterInfo> letterInfoList = characterOCR.ocrJPGAbc(inputPath, outputDirPath, id);

        return R.ok().add("id",id ).add("result",letterInfoList);
    }
    /**
     * 服务确认 是否关闭
     * @return
     */
    @RequestMapping("/serviceConfirm.json")
    public R serviceConfirm()
    {
        return R.ok();
    }
}
