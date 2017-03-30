package org.sv.go;

/**
 * Created by Jianrong on 2017/03/29.
 */
class Chesspoint {
    public static int black = 0, white = 1;
    int x, y;
    int color;//用来标识这颗子是什么颜色的.

    public Chesspoint(int i, int j, int c)//定义Chesspoint函数，里面有3个变量i,j,c
    {
        x = i;
        y = j;
        color = c;
    }

    public String toString()//储存x,y的位置和颜色
    {
        String c = (color == black ? "black" : "white");
        return "[" + x + "," + y + "]:" + c;
    }
}
