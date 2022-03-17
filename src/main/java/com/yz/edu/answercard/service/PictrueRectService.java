package com.yz.edu.answercard.service;

import com.yz.edu.answercard.common.OpenCVUtil;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jhf
 * @date 2021/12/8 15:08
 */
@Service
public class PictrueRectService {

    public List<Rect> getPicInfo(String inputFile)
    {
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat rgbaImage = Imgcodecs.imread(inputFile, Imgcodecs.IMREAD_UNCHANGED);
        Mat grayImage = OpenCVUtil.convertRgba2Gray(rgbaImage);
        ArrayList<Rect> rectArrayList = getSingleLetterArea(grayImage);
        return rectArrayList;
    }

    /**
     * 识别获取大图 作答区域坐标列表
     * @param source 要求为白底黑字的单通道图片，大图可以包含多个轨迹
     * @return list 矩形坐标，表示大图中每个轨迹的坐标
     */
    private ArrayList<Rect> getSingleLetterArea(Mat source)
    {
        // 边缘检测
        Mat cannyBlack = new Mat();
        Imgproc.Canny(source, cannyBlack, 100, 200, 3, false);
        // 轮廓检测
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(cannyBlack, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Mat sourceClone = source.clone();
        for (MatOfPoint contour : contours)
        {
            MatOfPoint2f points = new MatOfPoint2f();

            contour.convertTo(points, CvType.CV_32FC1);

            RotatedRect rotatedRect = Imgproc.minAreaRect(points);

            Rect rect = rotatedRect.boundingRect();
            // thickness :-1 填充多边形
//            Point tl=rect.tl();
//            Point br=rect.br();
            Imgproc.rectangle(sourceClone, rect.tl(), rect.br(), new Scalar(0, 0, 0), -1);
        }
        contours = new ArrayList<>();
        Mat blockCanny = new Mat();
        Imgproc.Canny(sourceClone, blockCanny, 100, 200, 3, false);
        Imgproc.findContours(blockCanny, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        ArrayList<Rect> rectList = new ArrayList<>();
        //Imgcodecs.imwrite("F:/TestDoc/ocrCutTmpPath/find_canny.png", blockCanny);
        for (MatOfPoint contour : contours)
        {
            MatOfPoint2f points = new MatOfPoint2f();
            contour.convertTo(points, CvType.CV_32FC1);
            RotatedRect rotatedRect = Imgproc.minAreaRect(points);
            Rect rect = rotatedRect.boundingRect();
            if(rect.size().area() < 20*20){
                // 忽略面积小于 20*20的区域
                continue;
            }
            rectList.add(rect);
        }
        return rectList;
    }
}
