package com.yz.edu.answercard.service;

import cn.hutool.core.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.Arrays;

/**
 * 原始图片生产
 */
@Slf4j
public class SourceImageCreate
{
    public static void main(String[] args)
    {
        // 原始图片
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        new SourceImageCreate().create();
    }

    private static void sout(Mat rgbaImage)
    {
        int rows = rgbaImage.rows();
        int cols = rgbaImage.cols();
        StringBuilder stringBuilder = new StringBuilder();
        double[] sample = rgbaImage.get(0,0);
        stringBuilder.append(Arrays.toString(sample));
        int number = 0;
        for (int i = 0; i < rows; i++)
        {
            for (int i1 = 0; i1 < cols; i1++)
            {
                double[] doubles = rgbaImage.get(i, i1);
                double max = NumberUtil.max(doubles);
                if(max != 0 ){
                    number++;
                    stringBuilder.append(Arrays.toString(doubles));
                }
            }
            if(number>100){
                break;
            }
        }
        System.out.println(stringBuilder);
    }

    private static void create()
    {
        // 原始图片
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String datasetPath = "D:\\tmp\\bigABCD" ;
        String datasetPathOutput = "D:\\tmp\\bigABCD_output\\" ;

        File datasetDir = new File(datasetPath);
        String[] list = datasetDir.list();
        log.info(Arrays.toString(list));
        int i = 0;

        // 图片左右倾斜 10度
        Point center = new Point(10,10);
        Mat left10 = Imgproc.getRotationMatrix2D(center, 10, 1);
        Mat right10 = Imgproc.getRotationMatrix2D(center, -10, 1);

        MatOfPoint2f topCenter = new MatOfPoint2f(new Point(9, 0), new Point(0, 19), new Point(19, 19));
        MatOfPoint2f topLeft = new MatOfPoint2f(new Point(4, 0), new Point(0, 19), new Point(19, 19));
        MatOfPoint2f topRight = new MatOfPoint2f(new Point(14, 0), new Point(0, 19), new Point(19, 19));
        Mat affineTransform = Imgproc.getAffineTransform(topCenter, topLeft);
        Mat affineTransform2 = Imgproc.getAffineTransform(topCenter, topRight);


        for (String s : list)
        {
            File file = new File(datasetPath + File.separator + s);
            File[] files = file.listFiles();
            for (File pngfile : files)
            {
                Mat rgbaImage = Imgcodecs.imread(pngfile.getAbsolutePath(), Imgcodecs.IMREAD_UNCHANGED);

                Mat dst = new Mat();
                Mat dst10 = new Mat();

                Imgproc.resize(rgbaImage,dst,new Size(20,20),0,0,Imgproc.INTER_AREA);

                Imgcodecs.imwrite(datasetPathOutput + File.separator + s + File.separator +i +".png", dst);
                i++;

                // 图片左右倾斜 10度
                Imgproc.warpAffine(dst,dst10,left10,new Size(20,20));
                Imgcodecs.imwrite(datasetPathOutput + File.separator + s + File.separator +i +".png", dst10);
                i++;
                Imgproc.warpAffine(dst,dst10,right10,new Size(20,20));
                Imgcodecs.imwrite(datasetPathOutput + File.separator + s + File.separator +i +".png", dst10);
                i++;

                // 图片头部倾斜 [9，0] 》 [0,0] ,[19,0]， 底部不变
                Imgproc.warpAffine(dst,dst10,affineTransform,new Size(20,20));
                Imgcodecs.imwrite(datasetPathOutput + File.separator + s + File.separator +i +".png", dst10);
                i++;
                Imgproc.warpAffine(dst,dst10,affineTransform2,new Size(20,20));
                Imgcodecs.imwrite(datasetPathOutput + File.separator + s + File.separator +i +".png", dst10);
                i++;
            }
        }
    }
}
