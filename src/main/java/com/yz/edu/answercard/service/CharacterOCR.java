package com.yz.edu.answercard.service;

import cn.hutool.core.io.FileUtil;
import com.yz.edu.answercard.common.OpenCVUtil;
import com.yz.edu.answercard.common.beans.LetterInfo;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.ml.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CharacterOCR
{
    @Value("${output.process.img}")
    private boolean outputProcessImg = true;
    //字母训练素材库
    @Value("${resource.dataset.abcd}")
    private String datasetPath;
    //字母训练模型
    private KNearest kNearest ;
    @Value("${model.abcpath}")
    private String modelPath;


    public static void main(String[] args)
    {
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        new CharacterOCR().kNearestTrain();
    }

    /**
     * 用k值近似法 猜字符
     *
     * @param checkMat  要求 输入的mat为单字图片，20*20宽度
     * @return
     */
    public LetterInfo guessSingleLetter(Mat checkMat, int index,boolean inv){

        //KNearest //trainKNearest();
        if(kNearest==null){
            kNearest = KNearest.load(modelPath);
        }
        Mat mat = getRGBAMatArray(checkMat,index,inv);

        Mat result = new Mat();

        Mat dists = new Mat();

        Mat neighborResponses = new Mat();

        kNearest.findNearest(mat, 3, result, neighborResponses, dists);

        double[] doubles = result.get(0, 0);

        char aDouble = (char) doubles[0];

        log.info("dists ->"+OpenCVUtil.toStringMat(dists));
        log.info("neighborResponses ->"+OpenCVUtil.toStringMat(neighborResponses));

        String letter = String.valueOf(aDouble);
        LetterInfo letterInfo = new LetterInfo();
        letterInfo.setLetter(letter);

        return letterInfo;
    }

    /**
     * 获取 ，或初始化训练模型
     * @return
     */
    private KNearest trainKNearest()
    {
        if(kNearest!=null){
            return kNearest;
        }
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        File datasetDir = new File(datasetPath);
        String[] list = datasetDir.list();
        log.info(Arrays.toString(list));
        int size = 0;
        for (String s : list)
        {
            File file = new File(datasetPath + File.separator + s);
            String[] list1 = file.list();
            size += list1.length;
        }

        Mat trainData = null;
        Mat response = new Mat(size, 1, CvType.CV_32SC1);

        int idx = 0;
        for (String s : list)
        {
            File file = new File(datasetPath + File.separator + s);
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
        this.kNearest = kNearest;
        return kNearest;
    }

    private Mat getMatMomentArray(Mat rgbaImage){
        Mat result = new Mat(1, 31, CvType.CV_32FC1);

        List<Mat> list = new ArrayList<>();
        Core.split(rgbaImage,list);
        Mat mat = list.get(3);
        Moments moments = Imgproc.moments(mat,true);

        result.put(0, 0, moments.get_m30());
        result.put(0, 1, moments.get_m00());
        result.put(0, 2, moments.get_m01());
        result.put(0, 3, moments.get_m02());
        result.put(0, 4, moments.get_m03());
        result.put(0, 5, moments.get_m10());
        result.put(0, 6, moments.get_m11());
        result.put(0, 7, moments.get_m12());
        result.put(0, 8, moments.get_m20());
        result.put(0, 9, moments.get_m21());

        int idx = 10;
        int totalCount = 0;
        for (int i = 0; i < 20; i++)
        {
            int count = 0;
            for (int j = 0; j < 20; j++)
            {
                double v = rgbaImage.get(i, j)[3];
                if(v > 10){
                    count ++;
                }
            }
            mat.put(0, 10+i, count);
            totalCount+=count;
        }
        for (int i = 0; i < 20; i++)
        {
            int count = 0;
            for (int j = 0; j < 20; j++)
            {
                double v = rgbaImage.get(j, i)[3];
                if(v > 10){
                    count ++;
                }
            }
            mat.put(0, 20 + i, count);
            totalCount+=count;

        }
        mat.put(idx, 30, totalCount);

        return result;
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

//        int totalCount = 0;
//
//        int leftCount =0;
//        int rightCount =0;
//        for (int i = 0; i < 20; i++)
//        {
//            int count = 0;
//            for (int j = 0; j < 20; j++)
//            {
//                double v = rgbaImage.get(i, j)[3];
//                if(v > 10){
//                    count ++;
//                }
//                if(j < 10){
//                    leftCount ++;
//                }else{
//                    rightCount ++;
//                }
//            }
//            tmp.add((double) count);
//            totalCount+=count;
//        }
//
//
//        int topCount =0;
//        int bottomCount =0;
//        for (int i = 0; i < 20; i++)
//        {
//            int count = 0;
//            for (int j = 0; j < 20; j++)
//            {
//                double v = rgbaImage.get(j, i)[3];
//                if(v > 10){
//                    count ++;
//                }
//                if(j < 10){
//                    topCount ++;
//                }else{
//                    bottomCount ++;
//                }
//            }
//            tmp.add((double) count);
//
//            totalCount+=count;
//        }
//        tmp.add((double) totalCount);
//        tmp.add((double) leftCount);
//        tmp.add((double) rightCount);
//        tmp.add((double) topCount);
//        tmp.add((double) bottomCount);
//        tmp.add((double) leftCount - rightCount);
//        tmp.add((double) topCount-bottomCount);

        int size = tmp.size();
        Mat mat = new Mat(1, size, CvType.CV_32FC1);
        for (int i = 0; i < size; i++)
        {
            mat.put(0, i, tmp.get(i));
        }
        return mat;
    }


    /**
     * 在透明底的 图片中 找到单个字符，并截图输出
     *
     * @param inputFile
     */
    public void cutAbcRct(String inputFile, String outputDir,String id)
    {
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Mat rgbaImage = Imgcodecs.imread(inputFile, Imgcodecs.IMREAD_UNCHANGED);

        Mat grayImage = OpenCVUtil.convertRgba2Gray(rgbaImage);

        if (outputProcessImg)
        {
            Imgcodecs.imwrite(outputDir + "grayImage.png", grayImage);
        }

        ArrayList<Rect> list = getSingleLetterArea(grayImage, outputDir);
        int i = 0;

        for (Rect rect : list)
        {
            Imgcodecs.imwrite(outputDir  + i + ".png", rgbaImage.submat(rect));
            Imgcodecs.imwrite(outputDir  +"20_"+ i + ".png", convertToUniArea(rgbaImage.submat(rect)));

            Imgproc.rectangle(grayImage, rect.tl(), rect.br(), new Scalar(0, 0, 0), 1);

            i++;
        }
        Imgcodecs.imwrite(outputDir +id+".png", grayImage);
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
            if(rect.size().area() < 20*20){
                // 忽略面积小于 15*15的区域
                continue;
            }
            rectList.add(rect);

        }

        return rectList;
    }

    /**
     * 在透明底的 图片中 找到所有单个字符，并识别输出
     *
     * @param inputFile
     */
    public List<LetterInfo> ocrAbc(String inputFile, String outputDir, String id)
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
     * 非透明底的jpg手写字母识别
     */
    public List<LetterInfo> ocrJPGAbc(String inputFile, String outputDir, String id)
    {
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // 原始图片
        Mat jpgImg = Imgcodecs.imread(inputFile, Imgcodecs.IMREAD_UNCHANGED);

        Mat source = new Mat();
        Imgproc.cvtColor(jpgImg,source,Imgproc.COLOR_RGBA2GRAY);

        source = OpenCVUtil.removeShadow(source);

        // 膨胀
        Mat dilation = OpenCVUtil.dilation(source,1);

        // 腐蚀
        Mat eroding = OpenCVUtil.eroding(dilation, 1);

        Mat gaussianBlur = new Mat();
        Imgproc.GaussianBlur(eroding, gaussianBlur, new Size(3, 3), 0);

        Mat threshold = new Mat();
        Imgproc.threshold(gaussianBlur, threshold, 180, 255, Imgproc.THRESH_BINARY);

        ArrayList<Rect> rectArrayList = getSingleLetterArea(threshold, outputDir);

        List<LetterInfo> letterInfoList = new ArrayList<>();

        for (Rect rect : rectArrayList)
        {
            UUID uuid = UUID.randomUUID();
            String filename = outputDir + uuid + ".png";
            Imgcodecs.imwrite(filename, jpgImg.submat(rect));

            Mat checkMat = convertToUniArea(threshold.submat(rect));
            LetterInfo letter = guessSingleLetter(checkMat,0,true);
            letter.setX(rect.tl().x);
            letter.setY(rect.tl().y);
            letter.setUrl(uuid + ".png");
            letterInfoList.add(letter);
            Imgproc.rectangle(jpgImg, rect.tl(), rect.br(), new Scalar(0, 0, 0), 1);
            Imgproc.putText(jpgImg, letter.getLetter(), rect.br(),Imgproc.FONT_HERSHEY_COMPLEX, 1, new Scalar(0, 0, 0));
        }

        Imgcodecs.imwrite(outputDir +id+".png", jpgImg);

        return letterInfoList;
    }

    public Mat convertToUniArea(Mat mat){
        Mat dst = new Mat();
        Imgproc.resize(mat,dst,new Size(20,20),0,0,Imgproc.INTER_AREA);
        return dst;
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

        String outputDir = datasetPath + File.separator + dir;
        File file = new File(outputDir);
        if(!file.exists()){
            file.mkdir();
        }
        Imgcodecs.imwrite(outputDir + File.separator + System.currentTimeMillis() + "1.png", dst);

        Point center = new Point(5,5);
        Mat left10 = Imgproc.getRotationMatrix2D(center, 5, 1);
        Mat right10 = Imgproc.getRotationMatrix2D(center, -5, 1);

        // 图片左右倾斜 10度
        Imgproc.warpAffine(dst,dst10,left10,new Size(20,20));
        Imgcodecs.imwrite(outputDir + File.separator + System.currentTimeMillis() +"2.png", dst10);

        Imgproc.warpAffine(dst,dst10,right10,new Size(20,20));
        Imgcodecs.imwrite(outputDir + File.separator + System.currentTimeMillis() +"3.png", dst10);

    }



    /**
     * 使用公共数据练习用法
     */
    public void kNearestTrain()
    {

        List<String> utf8 = FileUtil.readLines("C:\\Users\\jaxer\\Downloads\\letter-recognition.data", "utf8");
        System.out.println(utf8.size());
        List<String> trainList = utf8.subList(0, utf8.size() / 2);
        System.out.println(trainList.size());

        Mat trainData = new Mat(trainList.size(), 16, CvType.CV_32FC1);
        Mat response = new Mat(trainList.size(), 1, CvType.CV_32SC1);

        for (int i = 0; i < trainList.size(); i++)
        {
            String[] split = trainList.get(i).split(",");

            String abc = split[0];
            char c = abc.charAt(0);
            response.put(i, 0, c);


            for (int i1 = 1; i1 < split.length; i1++)
            {
                int i2 = Integer.parseInt(split[i1]);
                trainData.put(i, i1 - 1, i2);
            }
        }


        KNearest train = KNearest.create();
        train.train(trainData,0, response);
        train.save("letter.xml");

        KNearest kNearest = KNearest.load("letter.xml");

        for (int i = 12000; i < utf8.size(); i++)
        {
            String testData = utf8.get(i);
            Mat mat = new Mat(1, 16, CvType.CV_32FC1);
            String[] split = testData.split(",");
            for (int i1 = 1; i1 < split.length; i1++)
            {
                int num = Integer.parseInt(split[i1]);
                mat.put(0, i1 - 1, num);
            }
            Mat result = new Mat();
            kNearest.findNearest(mat, 5, result, new Mat(), new Mat());

            double[] doubles = result.get(0, 0);

            System.out.println(split[0].charAt(0) + "<--  -->" + new Character((char) doubles[0]));
        }


    }

    public void resetKN()
    {
        kNearest = null;
        trainKNearest();
    }
    // 训练模型
    private KNearest savetrainKNearest(String path,String letter)
    {
        if(kNearest!=null){
            return kNearest;
        }
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        File datasetDir = new File(path);
        String[] list = datasetDir.list();
        log.info(Arrays.toString(list));
        int size = 0;
        for (String s : list)
        {
            File file = new File(datasetPath + File.separator + s);
            String[] list1 = file.list();
            size += list1.length;
        }

        Mat trainData = null;
        Mat response = new Mat(size, 1, CvType.CV_32SC1);

        int idx = 0;
        for (String s : list)
        {
            File file = new File(datasetPath + File.separator + s);
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
        this.kNearest = kNearest;
        return kNearest;
    }
}
