package org.sv.go;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

/**
 * Created by Jainrong on 2017/03/29.
 */
public class Chessboard extends JPanel {
    //  默认的棋盘方格长度及数目
    public static final int defaultGridLen = 22, defaultGridNum = 19;

    /**利用Vector保存所有已下的棋子,包括在棋盘上的所有棋子和被踢掉的，
     **若某一次落子没有造成踢子，包括所有被这个棋子提掉的棋子及这个棋子本身.
     **Vector 类可以实现可增长的对象数组。与数组一样，它包含可以使用整数索引进行访问的组件。
     **Vector 的大小可以根据需要增大或缩小，以适应创建 Vector 后进行添加或移除项的操作。
     */
    private Vector chessman;
    private int alreadyNum;             // 已下数目
    private int currentTurn;            // 轮到谁下
    private int gridNum, gridLen;       // 方格长度及数目
    private int chessmanLength;         // 棋子的直径
    private Chesspoint[][] map;         // 在棋盘上的所有棋子
    private Image offScreen;            //用来绘制棋盘
    private Graphics offGrid;           //用来绘制方格和棋子
    private int size;                   // 棋盘的宽度及高度
    private int top = 13, left = 13;    // 棋盘上边及左边的边距
    //Point类表示 (x,y) 坐标空间中的位置的点，以整数精度指定。
    private Point mouseClick;           // 鼠标的位置，即map数组中的下标
    private ControlPanel controlPanel;  // 控制面板

    //获得控制板的距离
    public int getWidth() {
        return size + controlPanel.getWidth() + 35;
    }

    public int getHeight() {
        return size;
    }

    //绘制棋盘外观
    public Chessboard() {
        gridNum = defaultGridNum;                       //方格数目为19
        gridLen = defaultGridLen;                       //方格长度为22
        chessmanLength = gridLen * 9 / 10;              //棋子直径为22*9/10
        size = 2 * left + gridNum * gridLen;            //正方形棋盘边长为2*13+19*22
        addMouseListener(new PalyChess());              //注册鼠标监听器,监听鼠标按下事件
        addMouseMotionListener(new mousePosition());    //注册鼠标监听器,监听鼠标移动事件
        setLayout(new BorderLayout());                  //设置布局模式
        controlPanel = new ControlPanel();              //创建控制面板
        setSize(getWidth(), size);                      //设置宽度和大小
        add(controlPanel, "West");            //添加"控制面板",为"西"
        startGame();                                    //开始游戏
    }

    public void addNotify() {
        //创建按钮的同位体。按钮的同位体允许应用程序更改按钮的外观。而不更改其功能。
        super.addNotify();
        //创建一幅用于双缓冲的可在屏幕外绘制的图象
        offScreen = createImage(size, size);
        //为offScreen组件创建图形的上下文
        offGrid = offScreen.getGraphics();
    }

    public void paint(Graphics g) {
        //将颜色选取器的当前颜色设置为指定的 RGB 颜色。即设置画笔颜色
        offGrid.setColor(new Color(180, 150, 100));
        offGrid.fillRect(0, 0, size, size);

        /***画出棋盘格子***/
        //设置画笔颜色为黑色
        offGrid.setColor(Color.black);
        for (int i = 0; i < gridNum + 1; i++) {
            int x1 = left + i * gridLen;            //13+i*22
            int x2 = x1;
            int y1 = top;                           //top=13(前面已定义)
            int y2 = top + gridNum * gridLen;       //13+i*22
            //画竖线，在画布中心绘制直线（使用当前画笔颜色在（x1,y1)和（x2,y2)间画一条线段
            offGrid.drawLine(x1, y1, x2, y2);

            x1 = left;
            x2 = left + gridNum * gridLen;
            y1 = top + i * gridLen;
            y2 = y1;
            //画横线，在画布中心绘制直线（使用当前画笔颜色在（x1,y1)和（x2,y2)间画一条线段
            offGrid.drawLine(x1, y1, x2, y2);
        }
        /***这是通过画线的方式将棋盘绘制出来***/


        /***画出棋子***/
        for (int i = 0; i < gridNum + 1; i++) {
            for (int j = 0; j < gridNum + 1; j++) {
                //前面定义Chesspoint[][] map;即在棋盘上的所有棋子
                if (map[i][j] == null)
                    continue;
                //给棋子设置相应的颜色
                offGrid.setColor(map[i][j].color == Chesspoint.black ? Color.black : Color.white);
                //在指定区域绘制圆形
                offGrid.fillOval(left + i * gridLen - chessmanLength / 2,
                        top + j * gridLen - chessmanLength / 2, chessmanLength, chessmanLength);
            }
        }

        /***画出鼠标的位置，即下一步将要下的位置***/
        if (mouseClick != null) {
            //设置画笔颜色
            /***应该是他少减一个数着具体的也没改好,这先用-3*defaultGridLen***/
            offGrid.setColor(currentTurn == Chesspoint.black ? Color.gray : new Color(200, 200, 250));
            //使用当前颜色填充外接指定矩形框的椭圆。
            offGrid.fillOval(left + mouseClick.x * gridLen - chessmanLength / 2,
                    top + mouseClick.y * gridLen - chessmanLength / 2, chessmanLength, chessmanLength);
        }
        //把画面一次性画出
        g.drawImage(offScreen, 80, 0, this);
    }

    // 更新棋盘
    public void update(Graphics g) {
        paint(g);//绘制
    }


    /***下棋子,这是对鼠标按下事件的处理类,是内部类***/
    class PalyChess extends MouseAdapter { // 放一颗棋子
        //鼠标按键在组件上按下时调用
        public void mousePressed(MouseEvent evt) {
            int xoff = left / 2;
            int yoff = top / 2;

            /***程序中的那个棋子与鼠标不对位的漏洞,很有可能是这里和下边鼠标事件的X坐标出现了问题
             应该是少减一个数,具体的也没改好,这先用-3*defaultGridLen,位置大概也正确了***/
            //getX()返回事件相对于源组件的水平 x 坐标。
            int x = (evt.getX() - xoff - 3 * defaultGridLen) / gridLen;
            //getY()返回事件相对于源组件的水平 y 坐标。
            int y = (evt.getY() - yoff) / gridLen;
            if (x < 0 || x > gridNum || y < 0 || y > gridNum)
                return;//返回空

            //在void函数中可以用一个不带值的return来结束程序
            if (map[x][y] != null)
                return;

            /***清除多余的棋子***/
            if (alreadyNum < chessman.size()) {
                int size = chessman.size();
                for (int i = size - 1; i >= alreadyNum; i--)
                    chessman.removeElementAt(i);//从此向量中移除i变量
            }

            Chesspoint goPiece = new Chesspoint(x, y, currentTurn);

            map[x][y] = goPiece;
            //将棋子添加到chessman中
            chessman.addElement(goPiece);
            //已下棋子数目自加
            alreadyNum++;
            if (currentTurn == Chesspoint.black) {
                currentTurn = Chesspoint.white;
            } else {
                currentTurn = Chesspoint.black;
            }
            //***判断在[x,y]落子后，是否可以提掉对方的子
            take(x, y);
            //***判断是否挤死了自己，若是则已落的子无效
            if (allDead(goPiece).size() != 0) {
                map[x][y] = null;
                repaint();//重绘此组件。
                controlPanel.setMsg("无效下棋");//控制面板提示"无效下棋"
                //***back***
                chessman.removeElement(goPiece);//移除棋子
                alreadyNum--;//已下棋子数目自减
                if (currentTurn == Chesspoint.black) {
                    currentTurn = Chesspoint.white;//轮到白子下
                } else {
                    currentTurn = Chesspoint.black;//轮到黑子下
                }
                return;
            }

            mouseClick = null;
            // 更新控制面板
            controlPanel.setLabel();
            //更新标签
        }

        public void mouseExited(MouseEvent evt) {// 鼠标退出时，清除将要落子的位置
            mouseClick = null;
            repaint();//重绘
        }
    }

    private class mousePosition extends MouseMotionAdapter {// 取得将要落子的位置

        public void mouseMoved(MouseEvent evt) {
            int xoff = left / 2;
            int yoff = top / 2;
            /***这里也是上边说的棋子不对位的漏洞所对应的代码,这也放一个-3*defaultGridLen***/
            int x = (evt.getX() - xoff - 3 * defaultGridLen) / gridLen;
            int y = (evt.getY() - yoff) / gridLen;

            //在void函数中可以用一个不带值的return来结束程序
            if (x < 0 || x > gridNum || y < 0 || y > gridNum) {
                return;
            }
            if (map[x][y] != null) {
                return;
            }

            mouseClick = new Point(x, y);//鼠标位置为（x,y)
            repaint();//重绘此组件。
        }
    }

    //判断在[x,y]落子后，是否可以踢掉对方的子
    //以下这两个数组是用来定位一下棋子四周的坐标
    public static int[] xdir = {0, 0, 1, -1};
    public static int[] ydir = {1, -1, 0, 0};

    public void take(int x, int y) {
        Chesspoint goPiece;
        if ((goPiece = map[x][y]) == null) {
            return;
        }
        int color = goPiece.color;
        //取得棋子四周围的几个子
        Vector v = around(goPiece);
        for (int l = 0; l < v.size(); l++) {
            //elementAt()返回指定索引处的组件。
            Chesspoint q = (Chesspoint) (v.elementAt(l));
            if (q.color == color)
                continue;
            //若颜色不同，取得和q连在一起的所有已死的子，
            //若没有已死的子则返回一个空的Vector
            Vector dead = allDead(q);
            //移去所有已死的子
            removeAll(dead);
            //如果踢子，则保存所有被踢掉的棋子
            if (dead.size() != 0) {
                Object obj = chessman.elementAt(alreadyNum - 1);
                if (obj instanceof Chesspoint) {
                    goPiece = (Chesspoint) (chessman.elementAt(alreadyNum - 1));
                    dead.addElement(goPiece);
                } else {
                    Vector vector = (Vector) obj;
                    for (int i = 0; i < vector.size(); i++) {
                        dead.addElement(vector.elementAt(i));
                    }
                }
                // 更新Vector chessman中的第num个元素
                chessman.setElementAt(dead, alreadyNum - 1);
            }
        }
        repaint();
    }

    //判断棋子周围是否有空白
    public boolean aroundBlank(Chesspoint goPiece) {
        for (int l = 0; l < xdir.length; l++) {
            int x1 = goPiece.x + xdir[l];
            int y1 = goPiece.y + ydir[l];
            //xdir与ydir的取值是xdir={ 0, 0, 1, -1 }; ydir = { 1, -1, 0, 0 };也就是当前棋子的四周
            if (x1 < 0 || x1 > gridNum || y1 < 0 || y1 > gridNum)
                continue;
            if (map[x1][y1] == null)
                return true;//当发现有空白时就返回一个TRUE
        }
        return false;
    }

    //取得棋子四周围的几个子
    public Vector around(Chesspoint goPiece) {
        Vector v = new Vector();
        for (int l = 0; l < xdir.length; l++) {
            int x1 = goPiece.x + xdir[l];
            int y1 = goPiece.y + ydir[l];
            //xdir与ydir的取值是xdir={ 0, 0, 1, -1 }; ydir = { 1, -1, 0, 0 };也就是当前棋子的四周
            if (x1 < 0 || x1 > gridNum || y1 < 0 || y1 > gridNum
                    || map[x1][y1] == null)
                continue;
            v.addElement(map[x1][y1]);//将map[x1][y1]组件添加到此v的末尾。
        }
        return v;
    }

    //取得连在一起的所有已死的子
    public Vector allDead(Chesspoint q) {
        Vector v = new Vector();
        v.addElement(q);//将q组件添加到此v的末尾。
        int count = 0;
        //true时执行循环语句
        while (true) {
            int origsize = v.size();
            for (int i = count; i < origsize; i++) {
                Chesspoint goPiece = (Chesspoint) (v.elementAt(i));
                if (aroundBlank(goPiece))
                    return new Vector();
                Vector around = around(goPiece);
                for (int j = 0; j < around.size(); j++) {
                    Chesspoint a = (Chesspoint) (around.elementAt(j));
                    if (a.color != goPiece.color)
                        continue;
                    if (v.indexOf(a) < 0)//indexOf(a)，返回a中第一次出现处的索引。
                        v.addElement(a);//将a组件添加到此v的末尾。
                }
            }
            if (origsize == v.size())
                break;
            else
                count = origsize;
        }
        return v;
    }

    // 从棋盘上移去中棋子
    public void removeAll(Vector v) {
        for (int i = 0; i < v.size(); i++) {
            Chesspoint q = (Chesspoint) (v.elementAt(i));//返回i处的组件赋给q。
            map[q.x][q.y] = null;
        }
        repaint();//重绘此组件。
    }

    //悔棋
    public void back() {
        if (alreadyNum == 0) {
            controlPanel.setMsg("无子可悔");//调用controlPanel的消息方法,在标签上输出"无子可悔"
            return;
        }
        Object obj = chessman.elementAt(--alreadyNum);
        //instanceof是Java的一个二元操作符，和==，>，<是同一类东西。由于它是由字母组成的，所以也是Java的保留关键字。
        //它的作用是测试它左边的对象是否是它右边的类的实例，返回boolean类型的数据。
        if (obj instanceof Chesspoint) {
            Chesspoint goPiece = (Chesspoint) obj;
            map[goPiece.x][goPiece.y] = null;
            currentTurn = goPiece.color;
        } else {
            Vector v = (Vector) obj;
            for (int i = 0; i < v.size(); i++) {
                Chesspoint q = (Chesspoint) (v.elementAt(i));
                if (i == v.size() - 1) {
                    map[q.x][q.y] = null;
                    int index = chessman.indexOf(v);//返回v处的组件赋给index。
                    //setElementAt(Object, int)将此列表指定 index 处的组件设置为指定的对象。
                    chessman.setElementAt(q, index);
                    currentTurn = q.color;
                } else {
                    map[q.x][q.y] = q;
                }
            }
        }
        controlPanel.setLabel();// // 更新控制面板
        repaint();//重绘此组件。
    }

    //悔棋后再次前进
    public void forward() {
        if (alreadyNum == chessman.size()) {
            controlPanel.setMsg("不能前进");//设置controlPanel的消息对象"不能前进"
            return;
        }
        Object obj = chessman.elementAt(alreadyNum++);
        Chesspoint goPiece;
        //instanceof是Java的一个二元操作符，和==，>，<是同一类东西。由于它是由字母组成的，所以也是Java的保留关键字。
        //它的作用是测试它左边的对象是否是它右边的类的实例，返回boolean类型的数据。
        if (obj instanceof Chesspoint) {
            goPiece = (Chesspoint) (obj);
            map[goPiece.x][goPiece.y] = goPiece;
        } else {
            Vector v = (Vector) obj;
            goPiece = (Chesspoint) (v.elementAt(v.size() - 1));//返回v.size() - 1)处的组件赋给goPiece。
            map[goPiece.x][goPiece.y] = goPiece;
        }
        if (goPiece.color == Chesspoint.black) {
            currentTurn = Chesspoint.white;//到白子下
        } else {
            currentTurn = Chesspoint.black;//到黑子下
        }
        take(goPiece.x, goPiece.y);//踢子
        controlPanel.setLabel();//更新控制面板
        repaint();//重绘组件
    }

    //重新开始游戏
    public void startGame() {
        chessman = new Vector(); //chessman定义为一种向量
        alreadyNum = 0;//alreadyNum 初始植为零
        map = new Chesspoint[gridNum + 1][gridNum + 1];//map定义为一个二维数组,用来存放所有棋子
        currentTurn = Chesspoint.black;//到黑子下
        controlPanel.setLabel();//更新控制面板的标签
        repaint();//重绘组件
    }

    //控制面板类
    class ControlPanel extends Panel {
        protected Label lblTurn = new Label("", Label.CENTER);//创建标签对象
        protected Label lblNum = new Label("", Label.CENTER);//创建标签对象
        protected Label lblMsg = new Label("", Label.CENTER);//创建标签对象
        protected Choice choice = new Choice();//创建一个新的选择菜单。
        protected Button back = new Button("悔 棋");//创建"悔棋"按钮
        protected Button start = new Button("重新开局");//创建"重新开局"按钮

        public int getWidth() {
            return 45;//返回组件的当前宽度45。
        }

        public int getHeight() {
            return size;//返回组件的当前高度size。
        }

        //选择棋盘的大小
        public ControlPanel() {
            setSize(this.getWidth(), this.getHeight());//设置控制面板大小
            setLayout(new GridLayout(12, 1, 0, 10));//设置布局管理器
            setLabel();//设置标签
            choice.add("19 X 19");//添加"19X19"进选择按钮
            choice.add("13 X 13");//添加“13X13”进选择按钮
            choice.add(" 9 X 9 ");//添加“9X9”进选择按钮
            choice.addItemListener(new ChessAction());//在选择按钮中添加监听器
            add(lblTurn);//添加lblTurn标签对象
            add(lblNum);//添加lblNum标签对象
            add(start);//添加开局按钮
            add(choice);//添加选择菜单
            add(lblMsg);//添加lblMsg标签对象
            add(back);//添加“悔棋”按钮
            back.addActionListener(new BackChess());//给悔棋按钮,添加事件监听器
            start.addActionListener(new BackChess());//给重新开始按钮,添加事件监听器
            setBackground(new Color(120, 120, 200));//设置背景颜色
        }

        public Insets getInsets() {
            return new Insets(5, 5, 5, 5);
        }

        //悔棋
        private class BackChess implements ActionListener {
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource() == back)//如果鼠标点击"悔棋"按钮，则返回上一步（悔棋）
                    Chessboard.this.back();
                else if (evt.getSource() == start)//如果鼠标点击“重新开始”按钮，则重新开局
                    Chessboard.this.startGame();
            }
        }

        //下棋动作
        private class ChessAction implements ItemListener {
            public void itemStateChanged(ItemEvent evt) {
                String s = (String) (evt.getItem());
                int rects = Integer.parseInt(s.substring(0, 2).trim());//这是重新计算所要格子数.
                if (rects != Chessboard.this.gridNum) {
                    /**这里出现了错误,致使出现了能放大棋盘而不能缩小的错误//这里原来是gridLen * defaultGridNum,
                    现在将其改为defaultGridLen * defaultGridNum就可以了,主要是这是在计算棋盘长度时出现了错误*/
                    Chessboard.this.gridLen = (defaultGridLen * defaultGridNum) / rects;
                    Chessboard.this.chessmanLength = gridLen * 9 / 10;
                    Chessboard.this.gridNum = rects;
                    Chessboard.this.startGame();
                }
            }
        }

        // 待下方的颜色与步数
        public void setLabel() {
            //如果待下方是黑子，则显示“轮到黑子”，否则显示“轮到白子”
            lblTurn.setText(Chessboard.this.currentTurn == Chesspoint.black ? "轮到黑子" : "轮到白子 ");
            //如果待下方是黑子,则棋子颜色为黑色，否则为白色
            lblTurn.setForeground(Chessboard.this.currentTurn == Chesspoint.black ? Color.black : Color.white);
            //每下一步，步数加1
            lblNum.setText("第 " + (Chessboard.this.alreadyNum + 1) + " 手");
            //如果待下方是黑子，则该棋子的前景颜色为黑色，否则为白色
            lblNum.setForeground(Chessboard.this.currentTurn == Chesspoint.black ? Color.black : Color.white);
            //将该标签设置为空文本
            lblMsg.setText("");
        }

        public void setMsg(String msg) {// 提示信息
            lblMsg.setText(msg);
        }
    }
}