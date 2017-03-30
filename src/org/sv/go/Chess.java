package org.sv.go;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jianrong on 2017/03/29.
 */
public class Chess extends JFrame {
    Chessboard goBoard = new Chessboard();//初始化外观

    public Chess() {
        this.setTitle("SvGO");                  //设置标题为“SvGO”
        this.setLayout(new BorderLayout());     //设置布局管理器
        this.setSize(goBoard.getSize());        //设置大小
        this.add(goBoard, "Center"); //添加棋盘面板并居中
        this.setResizable(false);               //设置窗体为不可调整大小
        this.setLayout(new BorderLayout());     //设置布局管理器
        this.setSize(550, 490);    //设置窗口显示范围大小
        this.setVisible(true);                  //设置窗体的可见性为可见
    }

    //取得宽度
    public int getWidth() {
        return goBoard.getWidth();
    }

    //取得高度
    public int getHeight() {
        return goBoard.getHeight();
    }

    // 主函数，开始下棋程序
    public static void main(String[] args) {
        Chess svo = new Chess();
    }
}
