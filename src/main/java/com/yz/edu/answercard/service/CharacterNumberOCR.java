package com.yz.edu.answercard.service;

import com.yz.edu.answercard.common.OpenCVUtil;
import com.yz.edu.answercard.common.beans.LetterInfo;
import javafx.scene.transform.MatrixType;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.KNearest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author jhf
 * @date 2021/12/6 16:08
 */
@Service
@Slf4j
public class CharacterNumberOCR {
    @Value("${output.process.img}")
    private boolean outputProcessImg = true;
    //数字训练素材库
    @Value("${resource.dataset.number}")
    private String datasetNumberPath;
    //数字训练模型
    private KNearest numberKNearest ;
    @Value("${model.numberpath}")
    private String modelPath;
    /**
     * 在透明底的 图片中 找到所有单个字符，并识别输出
     *
     * @param inputFile
     */
    public List<LetterInfo> ocrNumber(String inputFile, String outputDir, String id)
    {
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Mat rgbaImage = Imgcodecs.imread(inputFile, Imgcodecs.IMREAD_UNCHANGED);

        Mat grayImage = OpenCVUtil.convertRgba2Gray(rgbaImage);
        if (outputProcessImg)
        {
            Imgcodecs.imwrite(outputDir + "grayImage.png", grayImage);
        }

        ArrayList<Rect> rectArrayList = getSingleLetterArea(grayImage, outputDir);

        List<LetterInfo> letterInfoList = new ArrayList<>();

        for (Rect rect : rectArrayList)
        {
            UUID uuid = UUID.randomUUID();
            String filename = outputDir + uuid + ".png";
            Imgcodecs.imwrite(filename, rgbaImage.submat(rect));

            Mat checkMat = convertToUniArea(rgbaImage.submat(rect));
            LetterInfo letter = guessSingleLetter(checkMat, 3,false);
            letter.setX(rect.tl().x);
            letter.setY(rect.tl().y);
            letter.setUrl(uuid + ".png");
            letterInfoList.add(letter);

            Imgproc.rectangle(grayImage, rect.tl(), rect.br(), new Scalar(0, 0, 0), 1);
            Imgproc.putText(grayImage, letter.getLetter(), rect.br(), Imgproc.FONT_HERSHEY_COMPLEX, 1, new Scalar(0, 0, 0));
        }
        Imgcodecs.imwrite(outputDir +id+".png", grayImage);

        return letterInfoList;
    }

    /**
     * 识别获取大图中单字母的区域坐标列表
     * @param source 要求为白底黑字的单通道图片，大图可以包含多个字母
     * @return list 矩形坐标，表示大图中每个字母的坐标
     */
    private ArrayList<Rect> getSingleLetterArea(Mat source, String outputDir)
    {
        // 边缘检测
        Mat cannyBlack = new Mat();
        Imgproc.Canny(source, cannyBlack, 100, 200, 3, false);
        if (outputProcessImg)
        {
            Imgcodecs.imwrite(outputDir + "cannyBlack.png", cannyBlack);
        }
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
            Imgproc.rectangle(sourceClone, rect.tl(), rect.br(), new Scalar(0, 0, 0), -1);
        }
        if (outputProcessImg)
        {
            Imgcodecs.imwrite(outputDir + "find.png", sourceClone);
        }
        contours = new ArrayList<>();
        Mat blockCanny = new Mat();
        Imgproc.Canny(sourceClone, blockCanny, 100, 200, 3, false);
        if (outputProcessImg)
        {
            Imgcodecs.imwrite(outputDir + "find_canny.png", blockCanny);
        }
        Imgproc.findContours(blockCanny, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        ArrayList<Rect> rectList = new ArrayList<>();
        for (MatOfPoint contour : contours)
        {
            MatOfPoint2f points = new MatOfPoint2f();
            contour.convertTo(points, CvType.CV_32FC1);
            RotatedRect rotatedRect = Imgproc.minAreaRect(points);
            Rect rect = rotatedRect.boundingRect();
            if(rect.size().area() < 10*10){
                // 忽略面积小于 20*20的区域
                continue;
            }
            rectList.add(rect);

        }

        return rectList;
    }

    public Mat convertToUniArea(Mat mat){
        Mat dst = new Mat();
        Imgproc.resize(mat,dst,new Size(20,20),0,0,Imgproc.INTER_AREA);
        return dst;
    }

    /**
     * 用k值近似法 猜字符
     *
     * @param checkMat  要求 输入的mat为单字图片，20*20宽度
     * @return
     */
    public LetterInfo guessSingleLetter(Mat checkMat, int index,boolean inv){

        //KNearest kNearest =KNearest.load(modelPath);//trainKNearest();
        if(numberKNearest==null){
            numberKNearest=KNearest.load(modelPath);
        }
        Mat mat = getRGBAMatArray(checkMat,index,inv);

        Mat result = new Mat();

        Mat dists = new Mat();

        Mat neighborResponses = new Mat();

        numberKNearest.findNearest(mat, 3, result, neighborResponses, dists);

        double[] doubles = result.get(0, 0);

        char aDouble = (char) doubles[0];

        log.info("dists ->"+OpenCVUtil.toStringMat(dists));
        log.info("neighborResponses ->"+OpenCVUtil.toStringMat(neighborResponses));

        String letter = String.valueOf(aDouble);
        LetterInfo letterInfo = new LetterInfo();
        letterInfo.setLetter(letter);

        return letterInfo;
    }
    public void resetKN()
    {
        numberKNearest = null;
        trainKNearest();
    }
    /**
     * 获取 ，或初始化训练模型
     * @return
     */
    private KNearest trainKNearest()
    {
        if(numberKNearest!=null){
            return numberKNearest;
        }
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        File datasetDir = new File(datasetNumberPath);
        String[] list = datasetDir.list();
        log.info(Arrays.toString(list));
        int size = 0;
        for (String s : list)
        {
            File file = new File(datasetNumberPath + File.separator + s);
            String[] list1 = file.list();
            size += list1.length;
        }

        Mat trainData = null;
        Mat response = new Mat(size, 1, CvType.CV_32SC1);

        int idx = 0;
        for (String s : list)
        {
            File file = new File(datasetNumberPath + File.separator + s);
            File[] files = file.listFiles();
            for (File pngfile : files)
            {
                Mat rgbaImage = Imgcodecs.imread(pngfile.getAbsolutePath(), Imgcodecs.IMREAD_UNCHANGED);
                Mat matArray = getRGBAMatArray(rgbaImage,3,false);
                int cols = matArray.cols();
                if(trainData == null){
                    trainData = new Mat(size, cols, CvType.CV_32FC1);
                }
                for (int i = 0; i < cols; i++)
                {
                    double v = matArray.get(0, i)[0];
                    trainData.put(idx, i, v);
                }
                response.put(idx, 0, s.charAt(0));
                idx++;
            }
        }
        KNearest kNearest = KNearest.create();
        log.debug(OpenCVUtil.toStringMat(trainData));
        log.debug(OpenCVUtil.toStringMat(response));
        kNearest.train(trainData,0, response);
        kNearest.save(modelPath);
        this.numberKNearest = kNearest;
        return kNearest;
    }

    /**
     * 获取图片特征数组，前400个为 20*20的像素值
     * 再 20 个 为 每列 有像素的个数
     * 再 20 个 为 每行 有像素的个数
     * 最后一个为 总像素个数
     * 共 441 个值
     * @param rgbaImage
     * @return
     */
    private Mat getRGBAMatArray(Mat rgbaImage,int index, boolean inv)
    {
        List<Double> tmp = new ArrayList<>();

        for (int i = 0; i < 20; i++)
        {
            for (int j = 0; j < 20; j++)
            {
                double v = rgbaImage.get(i, j)[index];
                boolean b = (inv) ? v < 245 :v > 10;

                tmp.add(b ? 255d : 0d);
            }
        }
        int size = tmp.size();
        Mat mat = new Mat(1, size, CvType.CV_32FC1);
        for (int i = 0; i < size; i++)
        {
            mat.put(0, i, tmp.get(i));
        }
        return mat;
    }

    /**
     * 传入一个切好的 字符图片，通过倾斜，生成3张图片
     * @param filepath 字符图片的绝对路径
     * @param dir 文件夹名称，如 A
     */
    public void createSourceImage(String filepath,String dir){

        Mat rgbaImage = Imgcodecs.imread(filepath, Imgcodecs.IMREAD_UNCHANGED);

        Mat dst = new Mat();
        Mat dst10 = new Mat();

        Imgproc.resize(rgbaImage,dst,new Size(20,20),0,0,Imgproc.INTER_AREA);

        String outputDir = datasetNumberPath + File.separator + dir;
        File file = new File(outputDir);
        if(!file.exists()){
            file.mkdir();
        }
        Imgcodecs.imwrite(outputDir + File.separator + System.currentTimeMillis() + "1.png", dst);

        Point center = new Point(10,10);
        Mat left10 = Imgproc.getRotationMatrix2D(center, 10, 1);
        Mat right10 = Imgproc.getRotationMatrix2D(center, -10, 1);

        // 图片左右倾斜 10度
        Imgproc.warpAffine(dst,dst10,left10,new Size(20,20));
        Imgcodecs.imwrite(outputDir + File.separator + System.currentTimeMillis() +"2.png", dst10);

        Imgproc.warpAffine(dst,dst10,right10,new Size(20,20));
        Imgcodecs.imwrite(outputDir + File.separator + System.currentTimeMillis() +"3.png", dst10);

    }

    public static void main(String[] args) {
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //Mat image = Mat.zeros( 2480, 3484, 8 );
        Mat image = new Mat(new Size(2480, 3484), 0,new Scalar(255, 255, 255));
        Rect rect = new Rect();
        rect.width=620;
        rect.height=350;
        rect.x=0;
        rect.y=0;
        Scalar color=new Scalar(0, 0, 0);
        Imgproc.rectangle(image,rect,color);
        //Imgproc.putText(image,"张三",new Point(0,0),14,14,color);
        Imgproc.putText(image, "", new Point(rect.width / 2,30), 0, 2, color,3,0,false);
        Imgcodecs.imwrite("F:\\spirePdf" + File.separator + System.currentTimeMillis() +"3.png", image);
    }
}
