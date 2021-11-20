package com.my.mian;

import com.my.entity.Cell;
import com.my.entity.Tetromino;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Tetris extends JPanel
{
    // 声明正在下落的方块
    private Tetromino currentOne = Tetromino.randomOne();
    // 声明将要下落的方块
    private Tetromino nextOne = Tetromino.randomOne();
    // 声明游戏主区域
    private Cell[][] wall = new Cell[18][9];
    // 声明单元格的值为48像素
    private static final int CELL_SIZE = 48;

    // 声明游戏分数池
    private int[] score_pool = {0, 1, 2, 5, 10};
    // 声明当前获得游戏的分数
    private int totalScore = 0;
    // 声明当前已清除的行数
    private int totalLine = 0;

    // 声明游戏三种状态，分别是：游戏中、暂停、游戏结束
    private static final int PLAYING = 0;
    private static final int PAUSE = 1;
    private static final int GAMEOVER = 2;
    // 声明变量存放当前游戏状态的值
    private int game_state;
    // 声明一个数组，用来显示游戏状态
    String[] show_state = {"P[pause]", "C[continue]", "S[replay]"};

    // 载入方块图片
    public static BufferedImage I;
    public static BufferedImage J;
    public static BufferedImage L;
    public static BufferedImage O;
    public static BufferedImage S;
    public static BufferedImage T;
    public static BufferedImage Z;
    public static BufferedImage backImage;

    static
    {
        try
        {
            I = ImageIO.read(new File("images/I.png"));
            J = ImageIO.read(new File("images/J.png"));
            L = ImageIO.read(new File("images/L.png"));
            O = ImageIO.read(new File("images/O.png"));
            S = ImageIO.read(new File("images/S.png"));
            T = ImageIO.read(new File("images/T.png"));
            Z = ImageIO.read(new File("images/Z.png"));
            backImage = ImageIO.read(new File("images/background.png"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // 重写绘图方法
    @Override
    public void paint(Graphics g) {
        g.drawImage(backImage, 0, 0, null);
        // 平移坐标轴
        g.translate(22, 15);
        // 绘制游戏主区域
        paintWall(g);
        // 绘制正在下落四方格
        paintCurrentOne(g);
        // 绘制下一个将要下落的四方格
        paintNextOne(g);
        // 绘制游戏得分
        paintScore(g);
        // 绘制当前游戏状态
        paintState(g);
    }

    // 游戏开始方法
    public void start()
    {
        game_state = PLAYING;
        // 匿名内部类监听键盘
        KeyListener keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                switch(code)
                {
                    case KeyEvent.VK_DOWN:
                        sortDropAction(); // 下移一格
                        break;
                    case KeyEvent.VK_LEFT:
                        moveLeftAction(); // 左移
                        break;
                    case KeyEvent.VK_RIGHT:
                        moveRightAction(); // 右移
                        break;
                    case KeyEvent.VK_UP:
                        rotateRightAction(); // 顺时针旋转
                        break;
                    case KeyEvent.VK_SPACE:
                        handDropAction(); // 瞬时下落
                        break;
                    case KeyEvent.VK_P:
                        // 判断当前游戏状态
                        if(game_state == PLAYING)
                        {
                            game_state = PAUSE;
                        }
                        break;
                    case KeyEvent.VK_C:
                        // 判断游戏状态
                        if(game_state == PAUSE)
                        {
                            game_state = PLAYING;
                        }
                        break;
                    case KeyEvent.VK_S:
                        // 表示游戏重新开始
                        game_state = PLAYING;
                        wall = new Cell[18][9];
                        currentOne = Tetromino.randomOne();
                        nextOne = Tetromino.randomOne();
                        totalScore = 0;
                        totalLine = 0;
                        break;
                }
            }
        };

        // 将俄罗斯方块窗口设置为焦点
        this.addKeyListener(keyListener);
        this.requestFocus();

        while(true)
        {
            // 当前游戏状态在游戏中，每隔0.5秒下落
            if(game_state == PLAYING)
            {
                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                // 判断能否下落
                if(canDrop())
                {
                    currentOne.softDrop();
                }
                else
                {
                    // 嵌入到墙中
                    landToWall();
                    // 判断能否消行
                    destroyLine();
                    // 判断游戏是否结束
                    if(isGameOver())
                    {
                        game_state = GAMEOVER;
                    }
                    else
                    {
                        currentOne = nextOne;
                        nextOne = Tetromino.randomOne();
                    }
                }
            }
            repaint();
        }
    }

    // 顺时针旋转
    public void rotateRightAction()
    {
        currentOne.rotateRight();
        // 判断是否越界或者重合
        if(outOfBounds() || coincide())
        {
            currentOne.rotateLeft();
        }
    }

    // 按键一次四方格下落一格
    public void sortDropAction()
    {
        // 判断能否下落
        if(canDrop())
        {
            // 当前四方格下落一格
            currentOne.softDrop();
        }
        else
        {
            // 将四方格嵌入到墙中
            landToWall();
            // 判断能否消行
            destroyLine();
            // 判断游戏是否结束
            if(isGameOver())
            {
                game_state = GAMEOVER;
            }
            else
            {
                currentOne = nextOne;
                nextOne = Tetromino.randomOne();
            }
        }
    }

    // 瞬时下落
    public void handDropAction()
    {
        while(true)
        {
            // 判断四方格能否下落
            if(canDrop())
            {
                currentOne.softDrop();
            }
            else
            {
                break;
            }
        }
        // 嵌入到墙中
        landToWall();
        //判断能否消行
        destroyLine();
        // 判断游戏是否结束
        if(isGameOver())
        {
            game_state = GAMEOVER;
        }
        else
        {
            // 游戏未结束，生成新的四方格
            currentOne = nextOne;
            nextOne = Tetromino.randomOne();
        }

    }

    // 将四方格嵌入到墙中
    private void landToWall()
    {
        Cell[] cells = currentOne.cells;
        for(Cell cell : cells)
        {
            int row = cell.getRow();
            int col = cell.getCol();
            wall[row][col] = cell;
        }
    }

    // 判断四方格能否下落
    public boolean canDrop()
    {
        Cell[] cells = currentOne.cells;
        for(Cell cell : cells)
        {
            int row = cell.getRow();
            int col = cell.getCol();
            // 判断是否到达底部
            if(row == wall.length-1)
            {
                return false;
            }
            else if(wall[row+1][col] != null)
            {
                return false;
            }
        }
        return true;
    }

    // 消行方法
    public void destroyLine()
    {
        // 声明变量，统计当前消除的行数
        int line = 0;
        int maxRow = 0;
        int minRow = 1000;
        Cell[] cells = currentOne.cells;
        for(Cell cell : cells)
        {
            int row = cell.getRow();
            maxRow = row > maxRow ? row : maxRow;
            minRow = row < minRow ? row : maxRow;
            // 从上到下判断是否已满并消行
            for(int j = minRow; j <= maxRow; j++)
            {
                // 判断当前行是否已满
                if(isFullLine(j))
                {
                    line++;
                    for(int i = row; i > 0; i--)
                    {
                        System.arraycopy(wall[i-1], 0, wall[i], 0, wall[0].length);
                    }
                    wall[0] = new Cell[9];
                }
            }

        }

        // 在分数池中获取分数，累加到总分数中
        totalScore += score_pool[line];
        // 统计消除总行数
        totalLine += line;
    }

    // 判断当前行是否已满
    public boolean isFullLine(int row)
    {
        Cell[] cells = wall[row];
        for(Cell cell : cells)
        {
            if(null == cell)
            {
                return false;
            }
        }
        return true;
    }

    // 判断游戏是否结束
    public boolean isGameOver()
    {
        Cell[] cells = nextOne.cells;
        for(Cell cell : cells)
        {
            int row = cell.getRow();
            int col = cell.getCol();
            if(wall[row][col] != null)
            {
                return true;
            }
        }
        return false;
    }

    // 绘制当前游戏状态
    private void paintState(Graphics g)
    {
        if(game_state == PLAYING) // 游戏中
        {
            g.drawString(show_state[0], 500, 660);
        }
        else if(game_state == PAUSE) // 暂停
        {
            g.drawString(show_state[1], 500, 660);
        }
        else // 游戏结束
        {
            g.drawString(show_state[2], 500, 660);
            g.setColor(Color.RED);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 60));
            g.drawString("GAMEOVER", 30, 400);
            g.drawString("您消除的行数："+totalLine, 30, 460);
            g.drawString("您的得分："+totalScore, 30, 520);
        }
    }

    // 绘制分数
    private void paintScore(Graphics g)
    {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        g.drawString("SCORES:" + totalScore, 500, 248);
        g.drawString("LINES:" + totalLine, 500, 430);
    }

    // 绘制下一个四方格
    private void paintNextOne(Graphics g)
    {
        Cell[] cells = nextOne.cells;
        for(Cell cell : cells)
        {
            int x = cell.getCol() * CELL_SIZE + 370;
            int y = cell.getRow() * CELL_SIZE + 25;
            g.drawImage(cell.getImage(), x, y, null);
        }
    }

    // 绘制当前四方格
    private void paintCurrentOne(Graphics g)
    {
        Cell[] cells = currentOne.cells;
        for (Cell cell : cells)
        {
            int x = cell.getCol() * CELL_SIZE;
            int y = cell.getRow() * CELL_SIZE;
            g.drawImage(cell.getImage(), x, y, null);
        }
    }

    private void paintWall(Graphics g)
    {
        for(int i = 0; i < wall.length; i++)
        {
            for (int j = 0; j < wall[i].length; j++)
            {
                int x = j * CELL_SIZE;
                int y = i * CELL_SIZE;
                Cell cell = wall[i][j];
                // 判断当前单元格是否有小方块，没有则绘制矩形，否则将小方块1嵌入到墙中
                if(null == cell)
                {
                    g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                }
                else
                {
                    g.drawImage(cell.getImage(), x, y, null);
                }

            }
        }
    }

    // 判断游戏是否出界
    public boolean outOfBounds()
    {
        Cell[] cells = currentOne.cells;
        for(Cell cell : cells)
        {
            int col = cell.getCol();
            int row = cell.getRow();
            if(row < 0 || row > wall.length-1 || col < 0 || col > wall[0].length-1)
            {
                return true; // 为true表示越界了
            }
        }
        return false;
    }

    // 判断方块是否重合
    public boolean coincide()
    {
        Cell[] cells = currentOne.cells;
        for(Cell cell : cells)
        {
            int row = cell.getRow();
            int col = cell.getCol();
            if(wall[row][col] != null)
            {
                return true; // 为true表示重合了
            }
        }
        return false;
    }

    // 按键一次，四方格左移一次
    public void moveLeftAction()
    {
        currentOne.moveLeft();
        // 判断是否越界或者四方格是否重合
        if(outOfBounds() || coincide())
        {
            currentOne.moveRight();
        }
    }

    // 按键一次，四方格右移一次
    public void moveRightAction()
    {
        currentOne.moveRight();
        // 判断是否越界或者四方格是否越界
        if(outOfBounds() || coincide())
        {
            currentOne.moveLeft();
        }
    }

    public static void main(String[] args)
    {
        // 创建一个窗口对象
        JFrame frame = new JFrame("俄罗斯方块");
        // 创建游戏界面（面板）
        Tetris panel = new Tetris();
        // 将面板嵌入到窗口中
        frame.add(panel);
        // 设置可见
        frame.setVisible(true);
        // 设置窗口尺寸
        frame.setSize(810, 940);
        // 设置窗口居中
        frame.setLocationRelativeTo(null);
        // 设置窗口关闭时程序终止
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 游戏开始
        panel.start();
    }
}