package org.sv.go;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jianrong on 2017/03/29.
 */
public class Chess extends JFrame {
    Chessboard goBoard = new Chessboard();//��ʼ�����

    public Chess() {
        this.setTitle("SvGO");                  //���ñ���Ϊ��SvGO��
        this.setLayout(new BorderLayout());     //���ò��ֹ�����
        this.setSize(goBoard.getSize());        //���ô�С
        this.add(goBoard, "Center"); //���������岢����
        this.setResizable(false);               //���ô���Ϊ���ɵ�����С
        this.setLayout(new BorderLayout());     //���ò��ֹ�����
        this.setSize(550, 490);    //���ô�����ʾ��Χ��С
        this.setVisible(true);                  //���ô���Ŀɼ���Ϊ�ɼ�
    }

    //ȡ�ÿ��
    public int getWidth() {
        return goBoard.getWidth();
    }

    //ȡ�ø߶�
    public int getHeight() {
        return goBoard.getHeight();
    }

    // ����������ʼ�������
    public static void main(String[] args) {
        Chess svo = new Chess();
    }
}
