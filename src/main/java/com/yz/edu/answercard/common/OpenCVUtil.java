package com.yz.edu.answercard.common;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class OpenCVUtil
{
	public static BufferedImage covertMat2Buffer(Mat mat) throws IOException {
		long time1 = new Date().getTime();
		// Mat 转byte数组
		BufferedImage originalB = toBufferedImage(mat);
		long time3 = new Date().getTime();
		System.out.println("保存读取方法2转=" + (time3 - time1));
		return originalB;
		// ImageIO.write(originalB, "jpg", new File("D:\\test\\testImge\\ws2.jpg"));
	}

	public static byte[] covertMat2Byte(Mat mat) throws IOException {
		long time1 = new Date().getTime();
		// Mat 转byte数组
		byte[] return_buff = new byte[(int) (mat.total() * mat.channels())];
		Mat mat1 = new Mat();
		mat1.get(0, 0, return_buff);
		long time3 = new Date().getTime();
		System.out.println(mat.total() * mat.channels());
		System.out.println("保存读取方法2转=" + (time3 - time1));
		return return_buff;
	}



	public static BufferedImage toBufferedImage(Mat m) {
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (m.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = m.channels() * m.cols() * m.rows();
		byte[] b = new byte[bufferSize];
		m.get(0, 0, b); // get all the pixels
		BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		return image;
	}

	/**
	 * 腐蚀膨胀是针对于白色区域来说的，腐蚀即腐蚀白色区域
	 * 腐蚀算法（黑色区域变大）
	 * @param source
	 * @return
	 */
	public static Mat eroding(Mat source) {
		return eroding(source, 1);
	}

	public static Mat eroding(Mat source, double erosion_size) {
		Mat resultMat = new Mat(source.rows(), source.cols(), source.type());
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * erosion_size + 1,
				2 * erosion_size + 1));
		Imgproc.erode(source, resultMat, element);
		return resultMat;
	}

	/**
	 * 腐蚀膨胀是针对于白色区域来说的，膨胀是膨胀白色区域
	 * 膨胀算法（白色区域变大）
	 * @param source
	 * @return
	 */
	public static Mat dilation(Mat source) {
		return dilation(source, 1);
	}

	/**
	 * 腐蚀膨胀是针对于白色区域来说的，膨胀是膨胀白色区域
	 * @Author 王嵩
	 * @param source
	 * @param dilationSize 膨胀因子2*x+1 里的x
	 * @return Mat
	 * @Date 2018年2月5日
	 * 更新日志
	 * 2018年2月5日 王嵩  首次创建
	 *
	 */
	public static Mat dilation(Mat source, double dilation_size) {
		Mat resultMat = new Mat(source.rows(), source.cols(), source.type());
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * dilation_size + 1,
				2 * dilation_size + 1));
		Imgproc.dilate(source, resultMat, element);
		return resultMat;
	}

	/**
	 * 轮廓识别，使用最外轮廓发抽取轮廓RETR_EXTERNAL，轮廓识别方法为CHAIN_APPROX_SIMPLE
	 * @param source 传入进来的图片Mat对象
	 * @return 返回轮廓结果集
	 */
	public static Vector<MatOfPoint> findContours(Mat source) {
		Mat rs = new Mat();
		/**
		 * 定义轮廓抽取模式
		 *RETR_EXTERNAL:只检索最外面的轮廓;
		 *RETR_LIST:检索所有的轮廓，并将其放入list中;
		 *RETR_CCOMP:检索所有的轮廓，并将他们组织为两层:顶层是各部分的外部边界，第二层是空洞的边界;
		 *RETR_TREE:检索所有的轮廓，并重构嵌套轮廓的整个层次。
		 */
		int mode = Imgproc.RETR_EXTERNAL;
		// int mode = Imgproc.RETR_TREE;
		/**
		 * 定义轮廓识别方法
		 * 边缘近似方法(除了RETR_RUNS使用内置的近似，其他模式均使用此设定的近似算法)。可取值如下:
		 *CV_CHAIN_CODE:以Freeman链码的方式输出轮廓，所有其他方法输出多边形(顶点的序列)。
		 *CHAIN_APPROX_NONE:将所有的连码点，转换成点。
		 *CHAIN_APPROX_SIMPLE:压缩水平的、垂直的和斜的部分，也就是，函数只保留他们的终点部分。
		 *CHAIN_APPROX_TC89_L1，CV_CHAIN_APPROX_TC89_KCOS:使用the flavors of Teh-Chin chain近似算法的一种。
		 *LINK_RUNS:通过连接水平段的1，使用完全不同的边缘提取算法。使用CV_RETR_LIST检索模式能使用此方法。
		 */
		int method = Imgproc.CHAIN_APPROX_SIMPLE;
		Vector<MatOfPoint> contours = new Vector<MatOfPoint>();
		Imgproc.findContours(source, contours, rs, mode, method, new Point());
		return contours;
	}

    /**
     * 绘制灰度直方图用于调整识别区域阈值判断
     * @Author 王嵩
     * @param 输入Mat对象img
     * @return Mat
     * @Date 2018年3月28日
     * 更新日志
     * 2018年3月28日 王嵩  首次创建
     *
     */
    public static Mat getGrayHistogram(Mat img) {
        List<Mat> images = new ArrayList<Mat>();
        images.add(img);
        MatOfInt channels = new MatOfInt(0); // 图像通道数，0表示只有一个通道
        MatOfInt histSize = new MatOfInt(256); // CV_8U类型的图片范围是0~255，共有256个灰度级
        Mat histogramOfGray = new Mat(); // 输出直方图结果，共有256行，行数的相当于对应灰度值，每一行的值相当于该灰度值所占比例
        MatOfFloat histRange = new MatOfFloat(0, 255);
        Imgproc.calcHist(images, channels, new Mat(), histogramOfGray, histSize, histRange, false); // 计算直方图
        Core.MinMaxLocResult minmaxLoc = Core.minMaxLoc(histogramOfGray);
        // 按行归一化
        // Core.normalize(histogramOfGray, histogramOfGray, 0, histogramOfGray.rows(), Core.NORM_MINMAX, -1, new Mat());

        // 创建画布
        int histImgRows = 600;
        int histImgCols = 1300;
        System.out.println("---------" + histSize.get(0, 0)[0]);
        int colStep = (int) Math.floor(histImgCols / histSize.get(0, 0)[0]);// 舍去小数，不能四舍五入，有可能列宽不够
        Mat histImg = new Mat(histImgRows, histImgCols, CvType.CV_8UC3, new Scalar(255, 255, 255)); // 重新建一张图片，绘制直方图


        int max = (int) minmaxLoc.maxVal;
        System.out.println("--------" + max);
        double bin_u = (double) (histImgRows - 20) / max; // max: 最高条的像素个数，则 bin_u 为单个像素的高度，因为画直方图的时候上移了20像素，要减去
        int kedu = 0;
        for (int i = 1; kedu <= minmaxLoc.maxVal; i++) {
            kedu = i * max / 10;
            // 在图像中显示文本字符串
            Imgproc.putText(histImg, kedu + "", new Point(0, histImgRows - kedu * bin_u), 1, 1, new Scalar(0, 0, 0));
        }


        for (int i = 0; i < histSize.get(0, 0)[0]; i++) { // 画出每一个灰度级分量的比例，注意OpenCV将Mat最左上角的点作为坐标原点
            // System.out.println(i + ":=====" + histogramOfGray.get(i, 0)[0]);
            Imgproc.rectangle(histImg, new Point(colStep * i, histImgRows - 20), new Point(colStep * (i + 1), histImgRows
                            - bin_u * Math.round(histogramOfGray.get(i, 0)[0]) - 20),
                    new Scalar(0, 0, 0), 1, 8, 0);
            kedu = i * 10;
            // 每隔10画一下刻度
            Imgproc.rectangle(histImg, new Point(colStep * kedu, histImgRows - 20), new Point(colStep * (kedu + 1),
                    histImgRows - 20), new Scalar(255, 0, 0), 2, 8, 0);
            Imgproc.putText(histImg, kedu + "", new Point(colStep * kedu, histImgRows - 5), 1, 1, new Scalar(255, 0, 0)); // 附上x轴刻度
        }

        return histImg;

    }


    /**
     * 垂直投影
     * @param source 传入灰度图片Mat
     * @return
     */
    public static Mat verticalProjection(Mat source) {
        // 先进行反转二值化
        Mat dst = new Mat(source.rows(), source.cols(), source.type());
        Imgproc.threshold(source, dst, 150, 255, Imgproc.THRESH_BINARY_INV);
        // 垂直积分投影
        // 每一列的白色像素的个数
        int[] colswidth = new int[dst.cols()];
        for (int j = 0; j < dst.cols(); j++) {
            for (int i = 0; i < dst.rows(); i++) {
                if (dst.get(i, j)[0] == 255) {
                    colswidth[j]++;
                }
            }
        }
        Mat matResult = new Mat(dst.rows(), dst.cols(), CvType.CV_8UC1, new Scalar(255, 255, 255));
        // 将每一列按照列像素值大小填充像素宽度
        for (int j = 0; j < matResult.cols(); j++) {
            for (int i = 0; i < colswidth[j]; i++) {
                matResult.put(matResult.rows() - 1 - i, j, 0);
            }
        }
        return matResult;
    }

    /**
     * 图片切块
     * @param srcImg 传入水平或垂直投影的图片对象Mat
     * @param proType 传入投影Mat对象的 投影方式
     *                   0：垂直投影图片,竖向切割；
     *                   1：水平投影图片，横向切割
     * @param rowXY 由于传来的是可能是原始图片的部分切片，要计算切块的实际坐标位置需要给出切片时所在的坐标，所以需要传递横向切片的y坐标或者纵向切片的横坐标
     * 如当proType==0时，传入的是切片的垂直投影，那么切成块后能得出x坐标及块宽高度，但是实际y坐标需要加上原切片的y坐标值，所以rowXY为切片的y坐标点，
     * 同理当proType==1时，rowXY应该为x坐标
     * @return
     */
    public static List<Rect> getBlockRect(Mat srcImg, Integer proType, int skipWidth, int skipHeight) {
        Imgproc.threshold(srcImg, srcImg, 150, 255, Imgproc.THRESH_BINARY_INV);
        // 注意 countNonZero 方法是获取非0像素（白色像素）数量，所以一般要对图像进行二值化反转
        List<Rect> rectList = new ArrayList<Rect>();
        int size = proType == 0 ? srcImg.cols() : srcImg.rows();
        int[] pixNum = new int[size];
        if (proType == 0) {
            for (int i = 0; i < srcImg.cols(); i++) {
                Mat col = srcImg.col(i);
                pixNum[i] = Core.countNonZero(col) > 1 ? Core.countNonZero(col) : 0;
            }
        } else {// 水平投影只关注行
            for (int i = 0; i < srcImg.rows(); i++) {
                Mat row = srcImg.row(i);
                pixNum[i] = Core.countNonZero(row) > 1 ? Core.countNonZero(row) : 0;
            }
        }
        int startIndex = 0;// 记录进入字符区的索引
        int endIndex = 0;// 记录进入空白区域的索引
        boolean inBlock = false;// 是否遍历到了字符区内
        for (int i = 0; i < size; i++) {
            if (!inBlock && pixNum[i] != 0) {// 进入字符区，上升跳变沿
                inBlock = true;
                startIndex = i;
            } else if (pixNum[i] == 0 && inBlock) {// 进入空白区，下降跳变沿存储
                endIndex = i;
                inBlock = false;
                Rect rect = null;
                if (proType == 0) {
                    rect = new Rect(startIndex+skipWidth, skipHeight, (endIndex - startIndex), srcImg.rows());
                } else {
                    rect = new Rect(skipWidth, startIndex+skipHeight, srcImg.cols(), (endIndex - startIndex));
                }
                rectList.add(rect);
            }
        }
        return rectList;
    }

    /**
     * 红色色系0-20，160-180
     * 蓝色色系100-120
     * 绿色色系60-80
     * 黄色色系23-38
     * 识别出的颜色会标记为白色，其他的为黑色
     * @param min
     * @param max
     */
    public static Mat findColorbyHSV(Mat source, int min, int max) {
        Mat hsv_image = new Mat();
        Imgproc.GaussianBlur(source, source, new Size(3, 3), 0, 0);
        Imgproc.cvtColor(source, hsv_image, Imgproc.COLOR_BGR2HSV);
        // String imagenameb = "D:\\test\\testImge\\ttbefore.jpg";
        // Imgcodecs.imwrite(imagenameb, hsv_image);

        //
        int iLowH = 220 /2;
        int iHighH = 350 /2;

        int iLowS = 10 *255/100;
        int iHighS = 81 *255/100;

        int iLowV = 15 *255/100;
        int iHighV = 85 *255/100;


        Mat thresholded = new Mat();
        Core.inRange(hsv_image, new Scalar(min, 90, 90), new Scalar(max, 255, 255), thresholded);
        //		Core.inRange(hsv_image, new Scalar(iLowH, iLowS, iLowV), new Scalar(iHighH, iHighS, iHighV), thresholded);
        return thresholded;
    }


    /**
     * 水平投影
     * @param source 传入灰度图片Mat
     * @return
     */
    public static Mat horizontalProjection(Mat source) {
        Mat dst = new Mat(source.rows(), source.cols(), source.type());
        // 先进行反转二值化
        Imgproc.threshold(source, dst, 150, 255, Imgproc.THRESH_BINARY_INV);
        // 水平积分投影
        // 每一行的白色像素的个数
        int[] rowswidth = new int[dst.rows()];
        for (int i = 0; i < dst.rows(); i++) {
            for (int j = 0; j < dst.cols(); j++) {
                if (dst.get(i, j)[0] == 255) {
                    rowswidth[i]++;
                }
            }
        }
        // 定义一个白色跟原图一样大小的画布
        Mat matResult = new Mat(dst.rows(), dst.cols(), CvType.CV_8UC1, new Scalar(255, 255, 255));
        // 将每一行按照行像素值大小填充像素宽度
        for (int i = 0; i < matResult.rows(); i++) {
            for (int j = 0; j < rowswidth[i]; j++) {
                matResult.put(i, j, 0);
            }
        }
        return matResult;
    }

    /**
     * 移除阴影
     * @param src
     * @return
     */
    public static Mat removeShadow(Mat src){

        ArrayList<Mat> channle_list = new ArrayList<>();
        ArrayList<Mat> result_channle_list = new ArrayList<>();
        Core.split(src, channle_list);
        for (int i = 0; i < channle_list.size(); i++)
        {
            //            Mat dst = new Mat();
            Mat s_mat = channle_list.get(i);

            Mat dst = OpenCVUtil.dilation(s_mat,13);
            //            Imgproc.dilate(s_mat, dst, new Mat());
//            Imgcodecs.imwrite("D:\\tmp\\input\\1"+i+".jpg", dst);

            Imgproc.medianBlur(dst,dst,21);

//            Imgcodecs.imwrite("D:\\tmp\\input\\2"+i+".jpg", dst);

            //            Imgproc.threshold(dst,dst,100,255,Imgproc.THRESH_BINARY_INV);

            //            Imgcodecs.imwrite("D:\\tmp\\input\\3"+i+".jpg", dst);


            Core.absdiff(s_mat, dst, dst);

            Core.bitwise_not(dst, dst);
//            Imgcodecs.imwrite("D:\\tmp\\input\\4"+i+".jpg", dst);
            result_channle_list.add(dst);
        }

        Mat dst = new Mat();
        Core.merge(result_channle_list, dst);

//        Imgcodecs.imwrite("D:\\tmp\\input\\result.jpg", dst);

        return dst;
    }

    /**
     * 涂写的边缘检测
     * 轮廓寻找，填充矩形
     * 并外扩1像素
     */
    public static void cannyFillRect(Mat subSource){
        // 边缘检测
        Imgproc.Canny(subSource,subSource,100,200,3,false);

        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        // 轮廓寻找
        Imgproc.findContours(subSource, contours, hierarchy,0,2,new Point());
        // 寻找轮廓的外接矩形,并填充
        for (MatOfPoint contour : contours)
        {
            Rect rect = Imgproc.boundingRect(contour);
            double area = rect.size().area();
            if(area < 10){
                continue;
            }
            Imgproc.rectangle(subSource,rect.tl(),rect.br(), new Scalar(255, 0, 0), -1);
        }
    }

    public static  String toStringMat(Mat mat){
        int rows = mat.rows();
        int cols = mat.cols();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < rows; i++)
        {
            for (int i1 = 0; i1 < cols; i1++)
            {
                double[] doubles = mat.get(i, i1);
                if(i1 != 0){
                    stringBuilder.append(",");
                }
                stringBuilder.append(doubles[0]);
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     * 转化RGBA透明图片 到 灰度格式图片
     * @param rgbaImage
     * @return
     */
    public static Mat convertRgba2Gray(Mat rgbaImage)
    {
        ArrayList<Mat> rgbaMatList = new ArrayList<>();
        Core.split(rgbaImage, rgbaMatList);

        Mat white = rgbaMatList.get(3);

        Mat black = new Mat();
        Imgproc.threshold(white, black, 20, 255, Imgproc.THRESH_BINARY_INV);
        //Imgcodecs.imwrite("F:/TestDoc/ocrCutTmpPath/black.png", black);
        return black;
    }
}
