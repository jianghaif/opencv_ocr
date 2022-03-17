package com.yz.edu.answercard.common;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;

public class Test
{
    public static void main2(String[] args)
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Mat source = Imgcodecs.imread("D:\\tmp\\input\\9482d753-8757-438f-9383-9dcaca12cd05.jpg",
                Imgcodecs.IMREAD_GRAYSCALE);

        Mat dst = new Mat();

        Imgproc.equalizeHist(source, dst);

        Imgcodecs.imwrite("D:\\tmp\\input\\9482d753-8757-438f-9383-9dcaca12cd05.jpg.jpg", dst);
    }

    public  static void main(String[] args) throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat imread = Imgcodecs.imread("C:\\Users\\jaxer\\Pictures\\f023.jpg");
        JFrame title = HighGui.createJFrame("title", HighGui.WINDOW_NORMAL);
        title.setVisible(true);
        HighGui.imshow("title", imread);
        System.in.read();


    }
    public  static void main3(String[] args){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Mat src = Imgcodecs.imread("D:\\tmp\\input\\9482d753-8757-438f-9383-9dcaca12cd05.jpg",
                Imgcodecs.IMREAD_GRAYSCALE);

        src.convertTo(src, CvType.CV_32FC1, 1.0 / 255);

        Mat dst3= ReduceBackGroundAlgorithm(src,0);

        Imgproc.GaussianBlur(dst3, dst3, new Size(1, 1), 0, 0, 4); //Size(width, heigth)，width,heigth参数可调,两个参数相同，默认值1，调节范围1-9（需为奇数）;

        //dst3 = ColorGradation(dst3);

        Imgproc.adaptiveThreshold(dst3,dst3,255,0,0,31,30);//去除背景色后再进一步二值化，C参数可调,默认值30，调节范围1-50;

        Imgcodecs.imwrite("D:\\tmp\\input\\9482d753-8757-438f-9383-9dcaca12cd05.jpg.jpg", dst3);


    }

    private static Mat ImageSharp(Mat src, int nAmount)

    {

        Mat dst= new Mat();

        double sigma = 3;

        // int threshold = 1;

        float amount = nAmount / 100.0f;

        Mat imgBlurred=new Mat();

        Imgproc.GaussianBlur(src, imgBlurred, new Size(7,7), sigma, sigma,4);

        Mat temp_sub= new Mat();

        //Mat temp_abs= new Mat();

        Core.subtract(src,imgBlurred,temp_sub);

        // Core.convertScaleAbs(temp_sub,temp_abs);

        // Mat lowContrastMask = new Mat();

        //Imgproc.threshold(temp_abs,lowContrastMask,threshold,255,1);

        //Mat temp_gen= new Mat();

        Core.addWeighted(src,1,temp_sub,amount,0,dst);

        // dst = src+temp_sub*amount;

        //src.copyTo(dst, lowContrastMask);

        return dst;

    }

    private static Mat ReduceBackGroundAlgorithm(Mat src, int flag) {

        Mat gauss = new Mat();
        Mat dst2 = new Mat();
        Mat dst3 = new Mat();

        if (flag==1) {
            Imgproc.GaussianBlur(src, gauss, new Size(31, 31), 0, 0, 4);
        }
        else
        {
            Imgproc.blur(src, gauss, new Size(101,101));
        }

        Core.divide(src,gauss,dst2);

        dst2=ImageSharp(dst2, 101);

        //Imgproc.GaussianBlur(dst2, dst2, new Size(3,3), 0,0,4);//

        dst2.convertTo(dst3, CvType.CV_8UC1,255);

        return dst3;

    }
}
