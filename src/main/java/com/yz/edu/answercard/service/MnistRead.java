package com.yz.edu.answercard.service;

/**
 * @author jhf
 * @date 2021/11/25 14:49
 */
import com.sun.imageio.plugins.common.ImageUtil;
import com.yz.edu.answercard.common.OpenCVUtil;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.util.CollectionUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MnistRead {

    public static final String TRAIN_IMAGES_FILE = "F:/TestDoc/train-images.idx3-ubyte";
    public static final String TRAIN_LABELS_FILE = "F:/TestDoc/train-labels.idx1-ubyte";
    public static final String TEST_IMAGES_FILE = "data/mnist/t10k-images.idx3-ubyte";
    public static final String TEST_LABELS_FILE = "data/mnist/t10k-labels.idx1-ubyte";

    /**
     * 将字节更改为十六进制字符串
     *
     * @param bytes bytes
     * @return the returned hex string
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 获取图片
     *
     * @param fileName 数据集路径
     * @return 一行一张图片
     */
    public static double[][] getImages(String fileName) {
        double[][] x = null;
        try (BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fileName))) {
            byte[] bytes = new byte[4];
            bin.read(bytes, 0, 4);
            if (!"00000803".equals(bytesToHex(bytes))) {                        // 读取魔数
                throw new RuntimeException("请选择正确的文件!");
            } else {
                bin.read(bytes, 0, 4);
                int number = Integer.parseInt(bytesToHex(bytes), 16);           // 读取样本总数
                bin.read(bytes, 0, 4);
                int xPixel = Integer.parseInt(bytesToHex(bytes), 16);           // 读取每行所含像素点数
                bin.read(bytes, 0, 4);
                int yPixel = Integer.parseInt(bytesToHex(bytes), 16);           // 读取每列所含像素点数
                x = new double[number][xPixel * yPixel];
                for (int i = 0; i < number; i++) {
                    double[] element = new double[xPixel * yPixel];
                    for (int j = 0; j < xPixel * yPixel; j++) {
                        element[j] = bin.read();                                // 逐一读取像素值
                        // normalization
//                        element[j] = bin.read() / 255.0;
                    }
                    x[i] = element;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return x;
    }

    /**
     * 获取“train”或“test”的标签
     *
     * @param fileName “train”或“test”相关的文件
     * @return
     */
    public static double[] getLabels(String fileName) {
        double[] y = null;
        try (BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fileName))) {
            byte[] bytes = new byte[4];
            bin.read(bytes, 0, 4);
            if (!"00000801".equals(bytesToHex(bytes))) {
                throw new RuntimeException("请选择正确的文件!");
            } else {
                bin.read(bytes, 0, 4);
                int number = Integer.parseInt(bytesToHex(bytes), 16);
                y = new double[number];
                for (int i = 0; i < number; i++) {
                    y[i] = bin.read();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return y;
    }
    /**
     * 绘制一张灰色图片，图片格式为png
     *  @param pixelValues 图片像素信息
     * @param width       width
     * @param high        high
     * @param fileName    图片保存path
     */
    public static void drawGrayPicture(double[] pixelValues, int width, int high, String fileName) throws IOException {

        BufferedImage bufferedImage = ImageIO.read(new File(fileName));
        int alpha = 0; // 图片透明度
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < high; j++) {
                int pixel = (int) (255 - pixelValues[i * high + j]);
                if (pixel!=0) {
                    // 设置为不透明
                    int value = pixel + (pixel << 8) + (pixel << 16);   // r = g = b 时，正好为灰度
                    bufferedImage.setRGB(j, i, value);
                }
//                else {
//                    // #AARRGGBB 最前两位为透明度
//                    int rgb = bufferedImage.getRGB(j, i);
//                    bufferedImage.setRGB(j, i, rgb);
//                }
                // #AARRGGBB 最前两位为透明度

            }
        }
        // 生成图片为PNG
        ImageIO.write(bufferedImage, "png", new File(fileName));
    }

    public static void main(String[] args) throws IOException {
        /**
         * 图片处理为透明背景
         */
//        String path = "C:/Users/jhf/Desktop/sssss/black";
//        //pngBlack(new File("F:/123.png"),path+321+".png");
        List<File> files = getFiles("F:/智能作答本/作答本通用内页-云蝶-20210220372");
//        int name =1;
//        for(File f : files){
//            pngBlack(f,path+name+".png");
//            name++;
//        }
        for(File f : files){
            resizeImage(f.getPath(),"F/智能作答本/作答本通用/"+File.separator+f.getName(),2149,3023,true);
        }
        /**
         * 图片处理切割为20x20
         */
       // pngcut("F:/TestDoc/number/20/0","F:/TestDoc/number/20/other");
        //resizeImage("F:/智能作答本/点阵/4.png","F:/智能作答本/作答本通用/"+bz4.png",2255,3141,true);
//        List<File> files = getFiles("F:/TestDoc/number/9");
//        int name =1;
//        for(File f : files){
//            resizeImage(f.getPath(),"F:/TestDoc/number/20/9/"+20+"_"+9+"_"+name+".png",20,20,false);
//            name++;
//        }

        /**
         *
         */
//        String filePath = "F:/TestDoc/image/"+(int)labels[0];
//        if(!(new File(filePath)).exists()){//如果文件夹不存在
//            (new File(filePath)).mkdir();//创建文件夹
//        }
        //drawGrayPicture(images[0],28,28, filePath+"/"+UUID.randomUUID().toString()+".png");
        /**
         * 处理MNIST数据集
         */
//        double[][] images = getImages(TRAIN_IMAGES_FILE);
//        double[] labels = getLabels(TRAIN_LABELS_FILE);
//        int white= new Color(255,255,255).getRGB();
//        for (int i=0;i< images.length;i++) {
//            String filePath = "F:/TestDoc/number/"+(int)labels[i];
//            if(!(new File(filePath)).exists()){//如果文件夹不存在
//                (new File(filePath)).mkdir();//创建文件夹
//            }
//            createPng(images[i],28,28, filePath+"/28_"+(i+1)+".png",null);
//        }
    }
    public static void createPng(double[] pixelValues,int width, int high,String path,Integer balckRgb) throws IOException {
        BufferedImage image = new BufferedImage(width, high, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", new File(path));
        int alpha = 0; // 图片透明度
        // 生产背景透明和内容透明的图片
        ImageIcon imageIcon = new ImageIcon(image);
        BufferedImage bufferedImage = new BufferedImage(width, high, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2D = (Graphics2D) bufferedImage.getGraphics(); // 获取画笔
        g2D.drawImage(imageIcon.getImage(), 0, 0, null); // 绘制Image的图片
        int black= new Color(39, 39, 39, 198).getRGB();
        for (int i = 0; i < high; i++) {
            for (int j = 0; j < width; j++) {
                int pixel = 0;
                if((i * 28 + j)<pixelValues.length){
                    pixel =(int)pixelValues[i * 28 + j];
                }
                int rgb = bufferedImage.getRGB(j, i);
                if (pixel!=0) {
                    // 设置为不透明
                   // int value = pixel + (pixel << 8) + (pixel << 16);   // r = g = b 时，正好为灰度
                    bufferedImage.setRGB(j, i, black);
                }else {
                    if(balckRgb!=null){
                        bufferedImage.setRGB(j, i, balckRgb);
                    }else {
                        pixel = ((alpha * 255 / 10) << 24) | (rgb & 0x00ffffff);
                        bufferedImage.setRGB(j, i, pixel);
                    }
                }
                // #AARRGGBB 最前两位为透明度
            }
        }
        // 绘制设置了RGB的新图片
        g2D.drawImage(bufferedImage, 0, 0, null);
        // 生成图片为PNG
        ImageIO.write(bufferedImage, "png", new File(path));
    }
    public static void pngBlack(File file,String path) throws IOException {
        BufferedImage bufImg = ImageIO.read(file);
        int width = bufImg.getWidth();//获取图片的宽度
        int height = bufImg.getHeight();//获取图片的长度
        int alpha = 0; // 图片透明度
        // 生产背景透明和内容透明的图片
        ImageIcon imageIcon = new ImageIcon(bufImg);
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2D = (Graphics2D) bufferedImage.getGraphics(); // 获取画笔
        g2D.drawImage(imageIcon.getImage(), 0, 0, null); // 绘制Image的图片
        int black= new Color(39, 39, 39, 198).getRGB();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = bufferedImage.getRGB(j, i);
                //System.out.println(rgb);
                if (rgb!=-1) {
                    // 设置为不透明
                    bufferedImage.setRGB(j, i, black);
                }else {
                    rgb = ((alpha * 255 / 10) << 24) | (rgb & 0x00ffffff);
                    bufferedImage.setRGB(j, i, rgb);
                }
                // #AARRGGBB 最前两位为透明度
            }
        }
        // 绘制设置了RGB的新图片
        g2D.drawImage(bufferedImage, 0, 0, null);
        // 生成图片为PNG
        ImageIO.write(bufferedImage, "png", new File(path));
    }

    /**
     * 透明图片 切割为 对应大小
     */
    public static void pngcut(String inputPath,String outPath) throws IOException {
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        List<File> files = getFiles(inputPath);
        int name =1;
        for(File f : files){
            String path =f.getPath();
           // System.out.println(path);
            Mat rgbaImage = Imgcodecs.imread(path, Imgcodecs.IMREAD_UNCHANGED);
//            Mat grayImage = convertRgba2Gray(rgbaImage);
            ArrayList<Rect> list = getSingleLetterArea(rgbaImage, outPath);
            int i = 0;

            for (Rect rect : list)
            {
                try {
                    Imgcodecs.imwrite(outPath  +name+".png", convertToUniArea(rgbaImage.submat(rect)));
                }catch (Exception e){
                    e.printStackTrace();
                    continue;
                }
//                Imgproc.rectangle(grayImage, rect.tl(), rect.br(), new Scalar(0, 0, 0), 1);

                i++;
            }
            //Imgcodecs.imwrite(outPath +name+".png", grayImage);
            name++;
           // Imgcodecs.imwrite(outPath + "grayImage.png", grayImage);
        }

    }
    public static Mat convertRgba2Gray(Mat rgbaImage)
    {
        ArrayList<Mat> rgbaMatList = new ArrayList<>();
        Core.split(rgbaImage, rgbaMatList);

        Mat white = rgbaMatList.get(3);

        Mat black = new Mat();
        Imgproc.threshold(white, black, 20, 255, Imgproc.THRESH_BINARY_INV);
        return black;
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

    private static ArrayList<Rect> getSingleLetterArea(Mat source, String outputDir)
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
            Imgproc.rectangle(sourceClone, rect.tl(), rect.br(), new Scalar(0, 0, 0), -1);
        }
        contours = new ArrayList<>();
        Mat blockCanny = new Mat();
        Imgproc.Canny(sourceClone, blockCanny, 100, 200, 3, false);
        Imgproc.findContours(blockCanny, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        ArrayList<Rect> rectList = new ArrayList<>();
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

    public static Mat convertToUniArea(Mat mat){
        Mat dst = new Mat();
        Imgproc.resize(mat,dst,new Size(20,20),0,0,Imgproc.INTER_AREA);
        return dst;
    }

    /**
     * 重新生成图片宽、高
     * @param srcPath 图片路径
     * @param destPath 新生成的图片路径
     * @param newWith 新的宽度
     * @param newHeight 新的高度
     * @param forceSize 是否强制使用指定宽、高,false:会保持原图片宽高比例约束
     * @return
     * @throws IOException
     */
    public static boolean resizeImage (String srcPath, String destPath, int newWith, int newHeight, boolean forceSize) throws IOException {
        if (forceSize) {
            Thumbnails.of(srcPath).forceSize(newWith, newHeight).toFile(destPath);
        } else {
            Thumbnails.of(srcPath).width(newWith).height(newHeight).toFile(destPath);
        }
        return true;
    }
}

