package com.yz.edu.answercard.service;

import com.yz.edu.answercard.common.OpenCVUtil;
import org.apache.commons.lang3.StringUtils;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 猿纵 答题卡识别
 */
@Service
public class YzCardService
{

    private static final Logger logger = LoggerFactory.getLogger(YzCardService.class);

    @Value("${output.process.img}")
    private boolean outputProcessImg;

    @Autowired
    private PathManager pathManager;

    /**
     * 返回答案列表，生成结果图片
     */
    public List<String> recognition(String inputPicPath, String outputPicPath, String uuid)
    {
        // 模板匹配 黑矩形
       // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String afterFourPointPath = fourPointTran(inputPicPath,uuid);

        // 二值化的 阈值
        double binary_thresh = 185;

        // 是否涂的 范围 可信度
        int redvalue = 30;
        int bluevalue = 45;
        // 切割左侧
        int leftSubSkipWidth = 78;
        int leftSubSkipHeight = 20;
        int leftSubWidth = 30;

        // 切割顶部
        int topSubSkipWidth = 50;
        int topSubSkipHeight = 30;
        int topSubHeight = 35;


        logger.info("原答题卡图片======" + afterFourPointPath);

        // 初始图片灰度图
        Mat sourceGrayMat = Imgcodecs.imread(afterFourPointPath, Imgcodecs.IMREAD_GRAYSCALE);
        if (outputProcessImg)
        {
            String destPath = pathManager.getProcessDir(uuid) + "3_gray.png";
            Imgcodecs.imwrite(destPath, sourceGrayMat);
            logger.info("生成灰度图======" + destPath);
        }


        // 先膨胀 后腐蚀算法，开运算消除细小杂点
        //        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * 1 + 1, 2 * 1 + 1));
        //
        //        Imgproc.morphologyEx(sourceGrayMat, sourceGrayMat, Imgproc.MORPH_OPEN, element);
        //
        //        destPath = Constants.PATH + Constants.DEST_IMAGE_PATH + "dtk2_pengzhang.png";
        //        Imgcodecs.imwrite(destPath, sourceGrayMat);
        //        logger.info("生成膨胀腐蚀后的图======" + destPath);


        Mat leftMark = new Mat(sourceGrayMat, new Rect(leftSubSkipWidth, leftSubSkipHeight,
                leftSubWidth, sourceGrayMat.rows() - leftSubSkipHeight));


        if (outputProcessImg)
        {
            Imgcodecs.imwrite(pathManager.getProcessDir(uuid) + "4_left.png", leftMark);
        }
        // 平滑处理消除噪点毛刺等等
        Imgproc.GaussianBlur(leftMark, leftMark, new Size(3, 3), 0);
        if (outputProcessImg)
        {
            logger.info("平滑处理后的右侧定位点图");
            Imgcodecs.imwrite(pathManager.getProcessDir(uuid) + "4_left_gaussian_blur.png", leftMark);
        }

        // 根据右侧定位获取水平投影，并获取纵向坐标
        Mat leftMarkMat = OpenCVUtil.horizontalProjection(leftMark);
        if (outputProcessImg)
        {
            logger.info("右侧水平投影图");
            Imgcodecs.imwrite(pathManager.getProcessDir(uuid) + "5_left_projection.png", leftMarkMat);
        }

        // 获取y坐标点，返回的是横向条状图集合
        List<Rect> leftMarkRectList = OpenCVUtil.getBlockRect(leftMarkMat, 1, leftSubSkipWidth, leftSubSkipHeight);

        // 顶部切割
        Mat topMarkMat = new Mat(sourceGrayMat, new Rect(topSubSkipWidth, topSubSkipHeight,
                sourceGrayMat.cols() - topSubSkipWidth, topSubHeight));
        if (outputProcessImg)
        {
            logger.info("截取底部定位点图" );
            Imgcodecs.imwrite(pathManager.getProcessDir(uuid) + "6_top.png", topMarkMat);
        }

        Imgproc.GaussianBlur(topMarkMat, topMarkMat, new Size(3, 3), 0);
        if (outputProcessImg)
        {
            logger.info("平滑处理后的底部定位点图");
            Imgcodecs.imwrite(pathManager.getProcessDir(uuid) + "6_top_gaussian_blur.png", topMarkMat);
        }

        // 根据底部定位获取垂直投影，并获取横向坐标
        Mat matbootom = OpenCVUtil.verticalProjection(topMarkMat);

        if (outputProcessImg)
        {
            logger.info("底部垂直投影图");
            Imgcodecs.imwrite(pathManager.getProcessDir(uuid) + "7_top_projection.png", matbootom);
        }

        // 获取x坐标点，返回的是竖向的柱状图集合
        List<Rect> topMarkRectList = OpenCVUtil.getBlockRect(matbootom, 0, topSubSkipWidth, topSubSkipHeight);

        // 高阶处理：增加HSV颜色查找，查找红色像素点
        Mat sourceMat = Imgcodecs.imread(afterFourPointPath, Imgcodecs.IMREAD_COLOR);
        Mat matRed = OpenCVUtil.findColorbyHSV(sourceMat, 156, 180);
        if (outputProcessImg)
        {
            logger.info("HSV找出红色像素点" );
            Imgcodecs.imwrite(pathManager.getProcessDir(uuid) + "9_rm_red.png", matRed);
        }

        Mat dstNoRed = OpenCVUtil.dilation(sourceGrayMat);
        // Imgproc.threshold(sourceGrayMat, dstNoRed, 190, 255, Imgproc.THRESH_BINARY);
        if (outputProcessImg)
        {
            logger.info("白色膨胀后的图片" );
            Imgcodecs.imwrite(pathManager.getProcessDir(uuid) + "10_white_pengzhang.png", dstNoRed);
        }

        Photo.inpaint(dstNoRed, matRed, dstNoRed, 1, Photo.INPAINT_NS);

        if (outputProcessImg)
        {
            logger.info("去除红颜色后的图片" );
            Imgcodecs.imwrite(pathManager.getProcessDir(uuid) + "11_nored.png", dstNoRed);
        }

        // 全图直方图
        Mat grayHistogram1 = OpenCVUtil.getGrayHistogram(dstNoRed);
        if (outputProcessImg)
        {
            logger.info("灰度直方图图片1" );
            Imgcodecs.imwrite(pathManager.getProcessDir(uuid)  + "12_1.png", grayHistogram1);
        }

        // 只看答案区域的直方图
        Mat answerMat = dstNoRed.submat(new Rect(115, 385, 500, 100));
        Mat grayHistogram2 = OpenCVUtil.getGrayHistogram(answerMat);
        if (outputProcessImg)
        {
            logger.info("灰度直方图图片2");
            Imgcodecs.imwrite(pathManager.getProcessDir(uuid)  + "12_2.png", grayHistogram2);
        }

        // 答题区域二值化
        Imgproc.threshold(answerMat, answerMat, binary_thresh, 255, Imgproc.THRESH_BINARY_INV);
        OpenCVUtil.cannyFillRect(answerMat);
        if (outputProcessImg)
        {
            Imgcodecs.imwrite(pathManager.getProcessDir(uuid) + "13_answerMat.png", dstNoRed);
        }

        // 二值化
        //        destPath = Constants.PATH + Constants.DEST_IMAGE_PATH + "dtk14_2.png";
        //        Imgproc.threshold(dstNoRed, dstNoRed, binary_thresh, 255, Imgproc.THRESH_BINARY_INV);
        //        Imgproc.adaptiveThreshold(dstNoRed,dstNoRed,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc
        //        .THRESH_BINARY,5,5);
        //        Imgcodecs.imwrite(destPath, dstNoRed);
//        logger.info("去除红色基础上进行二值化======" + destPath);


        String[] ans = new String[]{"A", "B", "C", "D"};
        List<String> answerList = new ArrayList<>();

        for (int i = 0; i < topMarkRectList.size(); i++)
        {
            Rect topRect = topMarkRectList.get(i);
            if (i < 1 || i > 5)
            {
                continue;
            }

            for (int j = 0; j < leftMarkRectList.size(); j++)
            {
                if (j < 11 || j > 15)
                {
                    continue;
                }
                Rect leftRect = leftMarkRectList.get(j);
                StringBuilder answer = new StringBuilder();
                // 顶部一个黑块，对应下面四个选项
                for (int i1 = 0; i1 < 4; i1++)
                {
                    // 16写死识别的宽度
                    int x = topRect.x + (16 * i1) -1;
                    int y = leftRect.y -1;
                    int width = 11 + 1;// topRect.width * 10 / 16;
                    int height = 7 + 1;//leftRect.height;

                    Mat selectdst = new Mat(dstNoRed, new Range(y, y + height), new Range(x, x + width));
                    // 本来是在每个区域内进行二值化，后来挪至了14步，整体进行二值化，因此注释掉此处2行
                    // Mat selectdst = new Mat(select.rows(), select.cols(), select.type());
                    // Imgproc.threshold(select, selectdst, 170, 255, Imgproc.THRESH_BINARY);

                    // System.out.println("x, y=="+x+","+y+"width,height=="+width+","+height);
                    double p100 = Core.countNonZero(selectdst) * 100 / (selectdst.size().area());

                    //                String que_answer =getQA(i, j);
                    //
                    //                Integer que = Integer.valueOf(que_answer.split("_")[0]);
                    //                String answer = que_answer.split("_")[1];
                    // System.out.println(Core.countNonZero(selectdst) + "/" + selectdst.size().area());
                    //                System.out.println(que_answer + ":			" + p100);

                    if (p100 >= bluevalue)
                    {// 蓝色
                        Imgproc.putText(sourceMat, StringUtils.substring(p100 + "", 0, 4), new Point(x, y), 1, 0.7, new Scalar(255, 0, 0));
                        Imgproc.rectangle(sourceMat, new Point(x, y), new Point(x + width, y + height), new Scalar(255, 0, 0), 1);
                        answer.append(ans[i1]);

                    }
                    else if (p100 > redvalue && p100 < bluevalue)
                    {// 红色
                        Imgproc.putText(sourceMat, StringUtils.substring(p100 + "", 0, 4), new Point(x, y), 1, 0.7,
                                new Scalar(0, 0, 255));

                        Imgproc.rectangle(sourceMat, new Point(x, y), new Point(x + width, y + height), new Scalar(0, 0, 255), 1);

                    }
                    Imgproc.putText(dstNoRed, StringUtils.substring(p100 + "", 0, 4), new Point(x, y), 1, 0.7, new Scalar(255, 0, 0));
                    Imgproc.rectangle(dstNoRed, new Point(x, y), new Point(x + width, y + height), new Scalar(255, 0, 0), 1);

                    //else {// 绿色
                    //                        Imgproc.rectangle(sourceMat, new Point(x, y),
                    //                                new Point(x + width, y + height),
                    //                                new Scalar(0, 255, 0), 1);
                    //
                    //                    }


                }
                answerList.add(answer.toString());
            }
        }
        if (outputProcessImg)
        {
            Imgcodecs.imwrite(pathManager.getProcessDir(uuid) + "13_answerMat.png", dstNoRed);
        }
        logger.info("框选填图区域，绿色为选项，蓝色为填图，红色为临界" );
        Imgcodecs.imwrite(outputPicPath, sourceMat);

        return answerList;
    }

    /**
     * 1. 根据模板，找到所有匹配的 黑方块
     * 2. 找到最四边的四点
     * 3. 根据预设的位置，进行图像转化拉直
     *
     * @param sourcePath
     * @param uuid
     * @return
     */
    private String fourPointTran(String sourcePath, String uuid)
    {
        Mat source = Imgcodecs.imread(sourcePath);

        // 缩放到预设比例
        double ratio = 885d/ source.height();

        logger.info("图片缩放{}", ratio);
        double width = ratio * source.width();

        Mat standSizeSource = new Mat();
        Imgproc.resize(source, standSizeSource, new Size(width, 885));

        // 移除阴影
        standSizeSource = OpenCVUtil.removeShadow(standSizeSource);


        Mat blackRectMat = Imgcodecs.imread(pathManager.getBaseDir() + "box.png", Imgcodecs.IMREAD_COLOR);

        Mat destination = new Mat();
        // 模板匹配
        Imgproc.matchTemplate(standSizeSource, blackRectMat, destination, Imgproc.TM_SQDIFF);

        Core.MinMaxLocResult minmaxLoc1 = Core.minMaxLoc(destination);

        // 最相识的点 乘以 系数，获取比较多的位置，以能涵盖到四角的点为优
        double tholesScore = minmaxLoc1.minVal * 1.7;


        int rows = destination.rows();
        int cols = destination.cols();
        Point leftTop = new Point(cols / 2, rows / 2);
        Point rightTop = new Point(cols / 2, rows / 2);
        Point leftBottom = new Point(cols / 2, rows / 2);
        Point rightBottom = new Point(cols / 2, rows / 2);

        // 用来标注 匹配到的位置 图片，便于过程定位
        Mat markRectMat = standSizeSource.clone();

        int blackRectWidth = blackRectMat.cols();
        int blackRectHeight = blackRectMat.rows();

        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < cols; j++)
            {
                double[] doubles = destination.get(i, j);

                if (doubles[0] <= tholesScore)
                {
                    if(outputProcessImg){
                        Imgproc.rectangle(markRectMat,
                                new Point(j, i),
                                new Point(j + blackRectWidth, i + blackRectHeight),
                                new Scalar(0, 255, 0), 1);
                    }


                    if (i * j > rightBottom.x * rightBottom.y)
                    {
                        rightBottom.x = j;
                        rightBottom.y = i;
                    }
                    if (i * j < leftTop.x * leftTop.y)
                    {
                        leftTop.x = j;
                        leftTop.y = i;
                    }
                    if (i - j < leftBottom.y - leftBottom.x)
                    {
                        leftBottom.x = j;
                        leftBottom.y = i;
                    }
                    if (i - j > rightTop.y - rightTop.x)
                    {
                        rightTop.x = j;
                        rightTop.y = i;
                    }
                }
            }
        }

        Imgproc.rectangle(markRectMat, leftTop, new Point(leftTop.x + blackRectWidth, leftTop.y + blackRectHeight),
                new Scalar(0, 0, 255), 2);
        Imgproc.rectangle(markRectMat, rightTop, new Point(rightTop.x + blackRectWidth, rightTop.y + blackRectHeight),
                new Scalar(0, 0, 255), 2);
        Imgproc.rectangle(markRectMat, leftBottom, new Point(leftBottom.x + blackRectWidth, leftBottom.y + blackRectHeight),
                new Scalar(0, 0, 255), 2);
        Imgproc.rectangle(markRectMat, rightBottom, new Point(rightBottom.x + blackRectWidth, rightBottom.y + blackRectHeight),
                new Scalar(0, 0, 255), 2);

        Imgcodecs.imwrite(pathManager.getProcessDir(uuid)+"1markRect.jpg", markRectMat);

        MatOfPoint mop = new MatOfPoint(leftTop, rightTop, leftBottom, rightBottom);
        MatOfPoint2f mat2f = new MatOfPoint2f();
        mop.convertTo(mat2f, CvType.CV_32FC1);

        Point point11 = new Point(75, 33);
        Point point12 = new Point(623, 33);
        Point point13 = new Point(75, 844);
        Point point14 = new Point(623, 844);

        Mat dst_vertices = new MatOfPoint(point11, point13, point12, point14);

        MatOfPoint2f refmat2f = new MatOfPoint2f();
        dst_vertices.convertTo(refmat2f, CvType.CV_32FC1);

        Mat warpMatrix = Imgproc.getPerspectiveTransform(mat2f, refmat2f);

        Mat dst = new Mat(standSizeSource.rows(), standSizeSource.cols(), standSizeSource.type());
        Imgproc.warpPerspective(standSizeSource, dst, warpMatrix, dst.size(), Imgproc.INTER_LINEAR, 0, new Scalar(255, 255, 255));

        String filename = pathManager.getProcessDir(uuid) + "2Tran.jpg";
        Imgcodecs.imwrite(filename, dst);
        return filename;
    }
}
