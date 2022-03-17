package com.yz.edu.answercard.service;

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 鼠标点击测试
 */
@Slf4j
public class MouseTest
{
    //初始化robot
    public static Robot robot;

    static JFrame frame = new JFrame();

    static JLabel imagePanel = new JLabel();

    static JTextField logText = new JTextField();

    static
    {
        try
        {
            robot = new Robot();
        }
        catch (AWTException e)
        {
            e.printStackTrace();
        }
    }

    //棋子的宽度，从截屏中量取，自行调节
    private final int halmaBodyWidth = 74;

    //游戏截屏里的两个跳板的中点坐标，主要用来计算角度，可依据实际的截屏计算，计算XY的比例
    private final static  double ratio = (1122d - 813d) / (813d - 310d);


    public static void main(String[] args) throws InterruptedException, IOException
    {
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BorderLayout());


        JTextField ratioText = new JTextField(""+ timeRatio);
        ratioText.setSize(200,20);
        ratioText.setEditable(false);
        ratioText.setFont(new Font("serif", Font.BOLD,18));

        JPanel panel = new JPanel();

        JButton comp = new JButton("-10");
        JButton comp2 = new JButton("-5");
        JButton comp22 = new JButton("-2");
        JButton comp23 = new JButton("+2");
        JButton comp3 = new JButton("+5");
        JButton comp4 = new JButton("+10");
        comp.addMouseListener(getL(ratioText, -10));
        comp2.addMouseListener(getL(ratioText, -5));
        comp22.addMouseListener(getL(ratioText, -2));
        comp23.addMouseListener(getL(ratioText, +2));
        comp3.addMouseListener(getL(ratioText, 5));
        comp4.addMouseListener(getL(ratioText, 10));
        panel.add(comp);
        panel.add(comp2);
        panel.add(comp22);
        panel.add(comp23);
        panel.add(comp3);
        panel.add(comp4);

        JPanel panel3 = new JPanel();
        panel3.add(logText);
        logText.setEditable(false);
        logText.setSize(100,200);
        logText.setText("鼠标状态");
        logText.setFont(new Font("serif", Font.BOLD,18));

        JPanel panel4 = new JPanel();
        panel4.setSize(100,100);
        panel4.add(new JLabel("系数："));
        panel4.add(ratioText);


        sliderPanel.add(panel,BorderLayout.CENTER);
        sliderPanel.add(panel3,BorderLayout.SOUTH);
        sliderPanel.add(panel4,BorderLayout.NORTH);



        frame.setTitle("辅助增强");
        frame.setSize(400,400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//设置:关闭窗口，则程序退出
        frame.setLocation(600,300);
        frame.add(sliderPanel, BorderLayout.CENTER);
        frame.add(imagePanel, BorderLayout.WEST);

        frame.setVisible(true);//显示窗口

        MouseTest mouseTest = new MouseTest();

        while (true)
        {
            mouseTest.hackWithCapture();
        }
    }

    private static MouseAdapter getL(JTextField textField, int i)
    {
        return new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                timeRatio += i;
                textField.setText("" + timeRatio);
            }
        };
    }

    public static int timeRatio = 243;

    private void hackWithCapture() throws InterruptedException, IOException
    {
        // 获取起跳鼠标点
        double len = countLengthByCap();

        long time = Math.max(Math.round(len * (timeRatio / 100d)), 200);
        log.info("获取终点结束，长度为 ：{}，按压时长:{}, x: {}" ,len,time,timeRatio);
        if(time < 250){
            time += 10;
            System.out.println("纠正按压时长:" + time);
        }

        logText.setBackground(Color.YELLOW);
        logText.setText("模拟按压鼠标时长："+time);
        //模拟鼠标按下左键
        robot.mousePress(InputEvent.BUTTON1_MASK);
        Thread.sleep(time);
        //模拟鼠标松开左键
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        log.info("释放鼠标");
        logText.setBackground(Color.white);
        logText.setText("释放鼠标");

        // 等待动画
        Thread.sleep(30 * 100 + RandomUtil.randomInt(-500,500));
    }


    private static void hackWithMouse() throws InterruptedException
    {
        // 获取起跳鼠标点
        System.out.println("1秒后获取起点");
        Thread.sleep(10 * 100);
        Point point = MouseInfo.getPointerInfo().getLocation();
        System.out.println("起点结束，快移动到终点，你有2.5秒");
        Thread.sleep(25 * 100);

        // 获取终点
        Point point2 = MouseInfo.getPointerInfo().getLocation();

        // 计算距离
        double absx = Math.abs(point.getX() - point2.getX());
        double absy = Math.abs(point.getY() - point2.getY());
        double len = Math.pow(Math.pow(absx, 2) + Math.pow(absy, 2), 0.5);


        long time = Math.max(Math.round(len * 2.42), 200);

        System.out.println("获取终点结束，长度为 ：" + len + "，按压时长:" + time);
        //模拟鼠标按下左键
        robot.mousePress(InputEvent.BUTTON1_MASK);
        Thread.sleep(time);
        //模拟鼠标松开左键
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        // 等待动画
        Thread.sleep(2 * 1000);
    }

    /**
     * 截图，计算与目标点的距离
     * @return
     * @throws IOException
     */
    public double countLengthByCap() throws IOException
    {
        // 创建需要截取的矩形区域
        Rectangle rect = new Rectangle(0, 50, 590, 1060);

        // 截屏操作
        BufferedImage bufImage = robot.createScreenCapture(rect);

        //ImageIO.write(bufImage, "PNG", new File("capture.png"));
        ImageIcon  icon= new ImageIcon(bufImage);
        Image scaledInstance = icon.getImage().getScaledInstance(59*3, 106*3, Image.SCALE_DEFAULT);
        icon.setImage(scaledInstance);
        imagePanel.setIcon(icon);

        int width = countWidth(bufImage);

        System.out.println(width * ratio);

        double height = width * ratio;

        return Math.pow(Math.pow(width, 2) + Math.pow(height, 2), 0.5);
    }

    /**
     * 获取跳棋 到 下一块跳板的中心 的 宽度
     *
     */
    private int countWidth(BufferedImage bufferedImage) throws IOException
    {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        int halmaXSum = 0;
        int halmaXCount = 0;
        int halmaYMax = 0;
        int boardX = 0;

        //从截屏从上往下逐行遍历像素点，以棋子颜色作为位置识别的依据，最终取出棋子颜色最低行所有像素点的平均值，即计算出棋子所在的坐标
        for (int y = 300; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int pixel = bufferedImage.getRGB(x, y);
                RGBInfo rgbInfo = processRGBInfo(pixel);
                //根据RGB的颜色来识别棋子的位置，
                if (rgbInfo.isHalma())
                {
                    halmaXSum += x;
                    halmaXCount++;
                    //棋子底行的Y坐标值
                    halmaYMax = Math.max(y, halmaYMax);
                }
            }
        }

        if (halmaXSum != 0 && halmaXCount != 0)
        {
            //棋子底行的X坐标值
            int halmaX = halmaXSum / halmaXCount;

            //从gameScoreBottomY开始
            for (int y = 300; y < height; y++)
            {
                int pixel = bufferedImage.getRGB(0, y);
                RGBInfo rgbInfo = processRGBInfo(pixel);
                int lastPixelR = rgbInfo.getRValue();
                int lastPixelG = rgbInfo.getGValue();
                int lastPixelB = rgbInfo.getBValue();
                //只要计算出来的boardX的值大于0，就表示下个跳板的中心坐标X值取到了。
                if (boardX > 0)
                {
                    break;
                }
                int boardXSum = 0;
                int boardXCount = 0;
                for (int x = 0; x < width; x++)
                {
                    pixel = bufferedImage.getRGB(x, y);
                    RGBInfo rgbInfo1 = processRGBInfo(pixel);
                    int pixelR = rgbInfo1.getRValue();
                    int pixelG = rgbInfo1.getGValue();
                    int pixelB = rgbInfo1.getBValue();
                    //处理棋子头部比下一个跳板还高的情况
                    if (Math.abs(x - halmaX) < halmaBodyWidth)
                    {
                        continue;
                    }

                    //从上往下逐行扫描至下一个跳板的顶点位置，下个跳板可能为圆形，也可能为方框，取多个点，求平均值
                    if ((Math.abs(pixelR - lastPixelR) + Math.abs(pixelG - lastPixelG) + Math.abs(pixelB - lastPixelB)) > 10)
                    {
                        boardXSum += x;
                        boardXCount++;
                    }
                }

                if (boardXSum > 0)
                {
                    boardX = boardXSum / boardXCount;
                }
            }

            return Math.abs(halmaX - boardX);
        }

        return 0;
    }

    /**
     * 获取指定坐标的RGB值
     *
     * @author LeeHo
     * @update 2017年12月31日 下午12:12:43
     */
    private RGBInfo processRGBInfo(int pixel)
    {
        RGBInfo rgbInfo = new RGBInfo();
        //转换为RGB数字
        rgbInfo.setRValue((pixel & 0xff0000) >> 16);
        rgbInfo.setGValue((pixel & 0xff00) >> 8);
        rgbInfo.setBValue((pixel & 0xff));
        return rgbInfo;
    }

    class RGBInfo
    {
        private int RValue;

        private int GValue;

        private int BValue;

        public int getRValue()
        {
            return RValue;
        }

        public void setRValue(int rValue)
        {
            RValue = rValue;
        }

        public int getGValue()
        {
            return GValue;
        }

        public void setGValue(int gValue)
        {
            GValue = gValue;
        }

        public int getBValue()
        {
            return BValue;
        }

        public void setBValue(int bValue)
        {
            BValue = bValue;
        }

        public boolean isHalma(){

            return this.RValue > 50 && this.RValue < 60
                    && this.GValue > 53 && this.GValue < 63
                    && this.BValue > 95 && this.BValue < 110;
        }
    }
}
