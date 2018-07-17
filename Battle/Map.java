// Robert Kucera
// CSCI 437W
// Map.java
//
// This program draws the battlefield for the rts game

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.Random;

public class Map extends JPanel implements ActionListener
{
    private final int MAP_WIDTH = 1250;
    private final int MAP_HEIGHT = 650;

    private final int DELAY = 25;
    private final int DELAY_PER_SEC = 40; 
    private final int RATE_EARN = 4; // earn money how many seconds

    private final int COMP_X_COOR = MAP_WIDTH - 395;
    private final int PLAY_X_COOR = 220;
    
    private int timeStage; // usesd for money
    private int currentSecond; // used for money
    
    private int topPoint;
    private int midPoint;
    private int lowPoint;
    private int playerStratPoints;
    private int compStratPoints;

    private Timer timer;
    private boolean inGame = true;


    // 34 Image icons
    private Image HQP;
    private Image HQC;
    private Image AAC;
    private Image AAC1;
    private Image AAC2;
    private Image AAC3;
    private Image AAP;
    private Image AAP1;
    private Image AAP2;
    private Image AAP3;
    private Image flagCL;
    private Image flagUncapL;
    private Image flagPL;
    private Image JeepC;
    private Image JeepC1;
    private Image JeepP;
    private Image JeepP1;
    private Image PlaneC;
    private Image PlaneC1;
    private Image PlaneC2;
    private Image PlaneC3;
    private Image PlaneP;
    private Image PlaneP1;
    private Image PlaneP2;
    private Image PlaneP3;
    private Image selector;
    private Image TankC;
    private Image TankC1;
    private Image TankC2;
    private Image TankC3;
    private Image TankP;
    private Image TankP1;
    private Image TankP2;
    private Image TankP3;

    private Image ii;

    private int playerMoney;
    private int compMoney;

    private int playerVehCnt;
    private int compVehCnt;

    private int kills;
    private int loses;
    private int playerHealth;
    private int enemyHealth;

    // player selected lane
    private int currentLane;

    // 0 = easy, 1 = medium, 2 = hard
    private int aiLevel = 1;

    private int[][] playerVeh = new int [30][5];
    private int[][] compVeh = new int [30][4];


    public Map()
    {
        loadImages();
        initVariables();

        addKeyListener(new TAdapter());

        setFocusable(true);

        setPreferredSize (new Dimension(MAP_WIDTH, MAP_HEIGHT));
        setBackground (Color.green);
        setDoubleBuffered(true);
    }

    private void initVariables()
    {
        int i;
        int j;

        playerStratPoints = 0;
        compStratPoints = 0;
        currentLane = 1; // start in middle lane, 0 = top 1 = mid, 2 = low

        playerVehCnt = 0;
        compVehCnt = 0;

        for (i = 0; playerVeh.length > i; i++)
        {
            playerVeh[i][0] = 5;
        }

        for (i = 0; compVeh.length > i; i++)
        {
            compVeh[i][0] = 5;
        }

        topPoint = 0;
        midPoint = 0;
        lowPoint = 0;

        timeStage = 0;
        currentSecond = 0;

        timer = new Timer(DELAY, this);
        timer.start();
    }

    @Override
    public void addNotify()
    {
        super.addNotify();
        requestFocus();

        startGame();
    }

    private void playGame(Graphics2D g2d)
    {
        
        drawSelector(g2d);
        addMoney();
        moveVehicles(); // also checks collisions
        drawFlags(g2d);
        drawVehicles(g2d);
        getAiActions();
        checkHealth();
    }

    private void buildCompVeh(int t, int h, int l, int c)
    {
        int i;
        
        if (compMoney >= c)
        {
            if (30 >= compVehCnt)
            {
                compMoney = compMoney - c;
                compVehCnt++;

                for (i = 0; compVeh.length > i; i++)
                {
                    if (compVeh[i][0] == 5)
                    {
                        compVeh[i][0] = t;
                        compVeh[i][1] = h;
                        compVeh[i][2] = l;
                        compVeh[i][3] = 1250 - 395;
                        break;
                    }
                }
            }
        }
    }

    private void getAiActions()
    {
        int y;
        int p;
        int c;
        boolean laneCheck = false;
        boolean quadrant = false;

        int type = 5;
        int health = 5;
        int lane = 5;
        int cost = 0;
        int x; // chech for player coordinates
        int closestCompX; // check for comp coordinates
        int closestX;

        boolean markTop;
        boolean markMid;
        boolean markLow;

        Random generator = new Random();
        int randomLane;
        int randomVeh;
        int i;
        int[][] index = new int[1][1];

        // aiLevel other than 0, 1, or 2 for training and testing purposes
        if (aiLevel == 0)
        {
            // easy ai

            if (compMoney >= 500)
            {
                if (30 >= compVehCnt)
                {

                    // build random vehicle in random lane
                    randomLane = generator.nextInt(3);
                    randomVeh = generator.nextInt(4);

                    for (i = 0; compVeh.length > i; i++)
                    {
                        if (compVeh[i][0] == 5)
                        {
                            compVeh[i][0] = randomVeh;
                            
                            // if jeep health = 2 or else health = 4
                            if (randomVeh == 0)
                            {
                                compVehCnt++;
                                compMoney = compMoney - 150;
                                compVeh[i][1] = 2;
                            }
                            else
                            {
                                compVehCnt++;
                                compMoney = compMoney - 500;
                                compVeh[i][1] = 4;
                            }

                            compVeh[i][2] = randomLane;
                            compVeh[i][3] = MAP_WIDTH - 395;
                            break;
                        }
                    }
                }
            }
        }

        if (aiLevel == 1)
        {
            // this way hard can act similar under certain  scenarios
            actLikeMedAi();
        }

        if (aiLevel == 2)
        {
            markTop = false;
            markMid = false;
            markLow = false;

            // hard ai
            if (1000 > compMoney)
            {
                actLikeMedAi();
            }

            else if (2000 > compMoney)
            {
                // all points capped?
                if (topPoint == 2 && midPoint == 2 && lowPoint ==2)
                {
                    actLikeMedAi();
                }

                else
                {
                    if (topPoint != 2)
                    {
                        for (i = 0; playerVeh.length > i; i++)
                        {
                            if (playerVeh[i][2] == 0)
                            {
                                markTop = true;
                            }
                        }
                    }

                    else if (midPoint != 2)
                    {
                        for (i = 0; playerVeh.length > i; i++)
                        {
                            if (playerVeh[i][2] == 1)
                            {
                                markMid = true;
                            }
                        }
                    }
                    else if (lowPoint != 2)
                    {
                        for (i = 0; playerVeh.length > i; i++)
                        {
                            if (playerVeh[i][2] == 2)
                            {
                                markLow = true;
                            }
                        }
                    }
                    if (!markTop)
                    {
                        buildCompVeh(0, 2, 0, 150);
                    }
                    else if (!markMid)
                    {
                        buildCompVeh(0, 2, 1, 150);
                    }
                    else if (!markLow)
                    {
                        buildCompVeh(0, 2, 2, 150);
                    }
                }
            }

            else if (compMoney > 2000)
            {
                if (topPoint == 2 && midPoint == 2 && lowPoint == 2)
                {
                    while (compMoney >= 2000)
                    {
                        type = generator.nextInt(4);
                        lane = generator.nextInt(3);

                        if (type == 0)
                        {
                            cost = 150;
                            health = 2;
                        }
                        else
                        {
                            cost = 500;
                            health = 4;
                        }
                        buildCompVeh(type, health, lane, cost); 
                    }
                }


                //build random vehicle
                // till hits 1000 or less money

            }
        }
    }

    private void actLikeMedAi()
    {
        int y;
        int p;
        int c;
        boolean laneCheck = false;
        boolean quadrant = false;

        int type = 5;
        int health = 5;
        int lane = 5;
        int cost = 0;
        int x; // chech for player coordinates
        int closestCompX; // check for comp coordinates
        int closestX;

        boolean markTop;
        boolean markMid;
        boolean markLow;

        Random generator = new Random();
        int randomLane;
        int randomVeh;
        int i;
        int[][] index = new int[1][1];

        // medium ai
        // actions player has no vehicles
        if (playerVehCnt == 0)
        {
            if (topPoint != 2)
            {
                for (c = 0; compVeh.length > c; c++)
                {
                    if (compVeh[c][0] != 5)
                    {
                        if (compVeh[c][2] == 0)
                        {
                            laneCheck = true;
                            break;
                        }
                    }
                }
                if (!laneCheck)
                {
                    if (compMoney >= 150)
                    {
                        for (c = 0; compVeh.length > c; c++)
                        {
                            if (compVeh[c][0] == 5)
                            {
                                compVehCnt++;
                                compMoney = compMoney - 150;
                                compVeh[c][0] = 0;
                                compVeh[c][1] = 2;
                                compVeh[c][2] = 0;
                                compVeh[c][3] = COMP_X_COOR;
                                break;
                            }
                        }
                    }
                }
            }
                 
            if (midPoint != 2)
            {
                laneCheck = false;

                for (c = 0; compVeh.length > c; c++)
                {
                    if (compVeh[c][0] != 5)
                    {
                        if (compVeh[c][2] == 1)
                        {
                            laneCheck = true;
                            break;
                        }
                    }
                }
                if (!laneCheck)
                {
                    if (compMoney >= 150)
                    {
                        for (c = 0; compVeh.length > c; c++)
                        {
                            if (compVeh[c][0] == 5)
                            {
                                compVehCnt++;
                                compMoney = compMoney - 150;
                                compVeh[c][0] = 0;
                                compVeh[c][1] = 2;
                                compVeh[c][2] = 1;
                                compVeh[c][3] = COMP_X_COOR;
                                break;
                            }
                        }
                    }
                }
            }
              
            if (lowPoint != 2)
            {
                for (c = 0; compVeh.length > c; c++)
                {
                    if (compVeh[c][0] != 5)
                    {
                        if (compVeh[c][2] == 2)
                        {
                            laneCheck = true;
                            break;
                        }
                    }
                }
                if (!laneCheck)
                {
                    if (compMoney >= 150)
                    {
                        for (c = 0; compVeh.length > c; c++)
                        {
                            if (compVeh[c][0] == 5)
                            {
                                compVehCnt++;
                                compMoney = compMoney - 150;
                                compVeh[c][0] = 0;
                                compVeh[c][1] = 2;
                                compVeh[c][2] = 2;
                                compVeh[c][3] = COMP_X_COOR;
                                break;
                            }
                        }
                    }
                }
            }
            if (topPoint == 2 && midPoint == 2 && lowPoint == 2)
            {
                // if player sits ai will attack with jeep
                if (compMoney >= 150)
                {
                    buildCompVeh(0, 2, generator.nextInt(3), 150);
                }
            }
        }

        // player has vehicle
        if (playerVehCnt >= 1)
        {
            closestX = 0;
            laneCheck = false;
            closestCompX = 0;
            
            // found closest threat
            // no compVeh in lane

            for (y = 0; playerVehCnt > y; y++){

            for (p = 0; playerVeh.length > p; p++)
            {
                // must be unchecked vehicle
                if (playerVeh[p][0] != 5 && playerVeh[p][4] != 1)
                {
                    x = playerVeh[p][3];
                        
                    if (x > closestX)
                    {
                        closestX = x;
                        type = playerVeh[p][0];
                        health = playerVeh[p][1];
                        lane = playerVeh[p][2];
                    }
                }
            }



            laneCheck = false;

                for (c = 0; compVeh.length > c; c++)
                {
                    if (compVeh[c][0] != 5)
                    {
                        laneCheck = true;
                        break;
                    }
                    if (type == 0 || health == 1)
                    {
                        buildCompVeh(0, 2, lane, 150);

                        for (p = 0; playerVeh.length > p; p++)
                        {
                            if (playerVeh[p][3] == closestX)
                            {
                                playerVeh[p][4] = 1;
                            }
                        }
                    }
                    
                    // greater than %25 health or not jeep
                    else if (compMoney >= 500)
                    {
                        if (type == 1)
                        {
                            buildCompVeh(2, 4, lane, 500);

                            for (p = 0; playerVeh.length > p; p++)
                            {
                                if (playerVeh[p][3] == closestX)
                                {
                                    playerVeh[p][4] = 1;
                                }
                             }
                         }

                        else if (type == 2)
                        {
                            buildCompVeh(3, 4, lane, 500);

                            for (p = 0; playerVeh.length > p; p++)
                            {
                                if (playerVeh[p][3] == closestX)
                                {
                                    playerVeh[p][4] = 1;
                                }
                            }
                        }

                        else
                        {
                            buildCompVeh(1, 4, lane, 500);

                            for (p = 0; playerVeh.length > p; p++)
                            {
                                if (playerVeh[p][3] == closestX)
                                {
                                    playerVeh[p][4] = 1;
                                }
                            }
                        }
                    }

                    for (p = 0; playerVeh.length > p; p++)
                    {
                        // must be unchecked vehicle
                        if (playerVeh[p][0] != 5 && playerVeh[p][4] != 1)
                        {
                            x = playerVeh[p][3];
                        
                            if (x > closestX)
                            {
                                x = closestX;
                                type = playerVeh[p][0];
                                health = playerVeh[p][1];
                                lane = playerVeh[p][2];
                            }
                        }
                    }

                
            
                }
            
                    
                // did not have $500
                if (500 > compMoney)
                {
                    // within 25 percent map of base
                    // will wait
                    // not within proximity will cap points
                    if (MAP_WIDTH -420 > closestX)
                    {
                        markTop = false;
                        markMid = false;
                        markLow = false;
                        // check for uncapped empty lanes
                        for (y = 0; playerVeh.length > y; y++)
                        {
                            if (playerVeh[y][2] == 0)
                            {
                                markTop = true;
                            }

                            else if (playerVeh[y][2] == 1)
                            {
                                markMid = true;
                            }

                            else if (playerVeh[y][2] == 2)
                            {
                                markLow = true;
                            }
                        }

                        if (!markTop)
                        {
                            if (compMoney >= 150)
                            {
                                buildCompVeh(0, 2, 0, 150);
                            }
                        }
                        else if (!markMid)
                        {
                            if (compMoney >= 150)
                            {
                                buildCompVeh(0, 2, 1, 150);
                            }
                        }
                        else if (!markLow)
                        {
                            if (compMoney >= 150)
                            {
                                buildCompVeh(0, 2, 2, 150);
                            }
                        }
                    }
                }
            }
        }
            // find what compVeh's are in lane
            // closest threat with comp in lane
    }   

    private int[] findClosestThreat()
    {
        int p;
        int x;
        int closestX = 0;
        int type;
        int health;
        int lane;
        int[] result = new int[4];

        for (p = 0; playerVeh.length > p; p++)
        {
            // must be unchecked vehicle
            if (playerVeh[p][0] != 5 && playerVeh[p][4] != 1)
            {
                x = playerVeh[p][3];
                        
                if (x > closestX)
                {
                    x = closestX;
                    result[0] = playerVeh[p][0];
                    result[1] = playerVeh[p][1];
                    result[2] = playerVeh[p][2];
                    result[3] = x;
                            
                }
            }

        }
        return result;
    }
    private void addMoney()
    {
        playerStratPoints = 0;
        compStratPoints = 0;

        // $200 every 4 sec + $100 for each strat point
        timeStage++;
        if (topPoint == 1)
        {
            playerStratPoints++;
        }
        if (topPoint == 2)
        {
            compStratPoints++;
        }
        if (midPoint == 1)
        {
            playerStratPoints++;
        }
        if (midPoint == 2)
        {
            compStratPoints++;
        }
        if (lowPoint == 1)
        {
            playerStratPoints++;
        }
        if (lowPoint == 2)
        {
            compStratPoints++;
        }


        // slows down earn rate so money does not grow too rapidly
        if (timeStage >= DELAY_PER_SEC)
        {
            currentSecond++;
            timeStage = 0;
        }

        if (currentSecond >= RATE_EARN)
        {
            playerMoney = playerMoney + (playerStratPoints * 100) + 200;
            compMoney = compMoney + (compStratPoints * 100) + 200;
            currentSecond = 0;
        }
    }

    private void drawFlags(Graphics2D g2d)
    {
        // top flag
        if (topPoint == 0)
        {
            g2d.drawImage(flagUncapL, MAP_WIDTH / 2, 150, null);
        }

        else if (topPoint == 1)
        {
            g2d.drawImage(flagPL, MAP_WIDTH / 2, 150, null);
        }

        else if (topPoint == 2)
        {
            g2d.drawImage(flagCL, MAP_WIDTH / 2, 150, null);
        }

        // middle flag
        if (midPoint == 0)
        {
            g2d.drawImage(flagUncapL, MAP_WIDTH / 2, 300, null);
        }

        else if (midPoint == 1)
        {
            g2d.drawImage(flagPL, MAP_WIDTH / 2, 300, null);
        }

        else if (midPoint == 2)
        {
            g2d.drawImage(flagCL, MAP_WIDTH / 2, 300, null);
        }

        // bottom flag
        if (lowPoint == 0)
        {
            g2d.drawImage(flagUncapL, MAP_WIDTH / 2, 450, null);
        }

        else if (lowPoint == 1)
        {
            g2d.drawImage(flagPL, MAP_WIDTH / 2, 450, null);
        }

        else if (lowPoint == 2)
        {
            g2d.drawImage(flagCL, MAP_WIDTH / 2, 450, null);
        }
    }

    private void showIntroScreen(Graphics2D g2d)
    {
        String s1 = "Press 'e' for Easy Difficulty";
        String s2 = "Press 'm' for Medium Difficulty";
        String s3 = "Press 'h' for Hard Difficulty";

        g2d.setColor(Color.red);
        g2d.fillRect(0, 0, 1250, 650);

        g2d.setColor(Color.black);
        g2d.drawString(s1, 150, 150);
        g2d.drawString(s2, 150, 300);
        g2d.drawString(s3, 150, 450);

    }

    private void drawMap(Graphics2D g2d)
    {
        g2d.setColor(Color.cyan);
        g2d.fillRect(0, 0, MAP_WIDTH, 150);

        g2d.setColor(Color.lightGray);
        g2d.drawLine(0, 450, MAP_WIDTH, 450);
        g2d.drawLine(0, 300, MAP_WIDTH, 300); // separates Lanes

        loadImage();
        g2d.drawImage(HQP, 0, 0, null);
        g2d.drawImage(HQC, MAP_WIDTH - 214, 0, null);
    }

    public void drawData(Graphics2D g2d)
    {
        String data = "Money: $" + playerMoney + "    Kills: " + kills 
            + "    Loses: " + loses + "    HQ Health: " + playerHealth 
            + "    Enemy HQ: " + enemyHealth +"    Difficulty: " + aiLevel
            + "    Comp Money: $" + compMoney;
            

        g2d.setColor(Color.black);
        g2d.drawString(data, 0, 625);
    }


    private void checkHealth()
    {
        if (0 >= playerHealth || 0 >= enemyHealth)
        {
            stopGame();
        }
    }

    // check this code here
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        doDrawing(g);
    }

    private void doDrawing(Graphics g)
    {
        Graphics2D g2d = (Graphics2D) g;
        
        drawMap(g2d);
        drawData(g2d);

        drawData(g2d);

        if (inGame)
        {
            playGame(g2d);
        }

        else
        {
            showIntroScreen(g2d);
        }

        g2d.drawImage(ii, 0 , 0, this);
        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }

    private void startGame()
    {
        playerMoney = 1000;
        compMoney = 1000;
        kills = 0;
        loses = 0;
        playerHealth = 20;
        enemyHealth = 20;

        
    }


    private void loadImage()
    {
        ImageIcon hqPlayer = new ImageIcon("HQP.png");
        HQP = hqPlayer.getImage();

        ImageIcon hqComp = new ImageIcon("HQC.png");
        HQC = hqComp.getImage();
    }

    private void loadImages()
    {
        HQP = new ImageIcon("HQP.png").getImage();
        HQC = new ImageIcon("HQC.png").getImage();

        AAC = new ImageIcon("AAC.png").getImage();
        AAC1 = new ImageIcon("AAC1.png").getImage();
        AAC2 = new ImageIcon("AAC2.png").getImage();
        AAC3 = new ImageIcon("AAC3.png").getImage();
        AAP = new ImageIcon("AAP.png").getImage();
        AAP1 = new ImageIcon("AAP1.png").getImage();
        AAP2 = new ImageIcon("AAP2.png").getImage();
        AAP3 = new ImageIcon("AAP3.png").getImage();
    
        flagCL = new ImageIcon("flagCL.png").getImage();
        flagUncapL = new ImageIcon("flagUncapL.png").getImage();
        flagPL = new ImageIcon("flagPL.png").getImage();

        JeepC = new ImageIcon("JeepC.png").getImage();
        JeepC1 = new ImageIcon("JeepC1.png").getImage();
        JeepP = new ImageIcon("JeepP.png").getImage();
        JeepP1 = new ImageIcon("JeepP1.png").getImage();

        PlaneC = new ImageIcon("PlaneC.png").getImage();
        PlaneC1 = new ImageIcon("PlaneC1.png").getImage();
        PlaneC2 = new ImageIcon("PlaneC2.png").getImage();
        PlaneC3 = new ImageIcon("PlaneC3.png").getImage();
        PlaneP = new ImageIcon("PlaneP.png").getImage();
        PlaneP1 = new ImageIcon("PlaneP1.png").getImage();
        PlaneP2 = new ImageIcon("PlaneP2.png").getImage();
        PlaneP3 = new ImageIcon("PlaneP3.png").getImage();

        selector = new ImageIcon("selector.png").getImage();

        TankC = new ImageIcon("TankC.png").getImage();
        TankC1 = new ImageIcon("TankC1.png").getImage();
        TankC2 = new ImageIcon("TankC2.png").getImage();
        TankC3 = new ImageIcon("TankC3.png").getImage();
        TankP = new ImageIcon("TankP.png").getImage();
        TankP1 = new ImageIcon("TankP1.png").getImage();
        TankP2 = new ImageIcon("TankP2.png").getImage();
        TankP3 = new ImageIcon("TankP3.png").getImage();
    }

    private void drawSelector(Graphics2D g2d)
    {
        if (currentLane == 0)
        {
            g2d.drawImage(selector, 181, 150, null);
        }

        else if (currentLane == 1)
        {
            g2d.drawImage(selector, 181, 300, null);
        }

        else
        {
            g2d.drawImage(selector, 181, 450, null);
        }
    }

    private void moveVehicles()
    {
        int p;
        int c;
        int lane;

        for (p = 0; playerVeh.length > p; p++)
        {
            if (playerVeh[p][0] != 5)
            {
                if (MAP_WIDTH - 398 >= playerVeh[p][3])
                {
                    playerVeh[p][3] = playerVeh[p][3] + 1;

                    if (playerVeh[p][3] >= MAP_WIDTH / 2)
                    {
                        lane = playerVeh[p][2];

                        if (lane == 0)
                        {
                            topPoint = 1;
                        }

                        else if (lane == 1)
                        {
                            midPoint = 1;
                        }

                        else
                        {
                            lowPoint = 1;
                        }
                    }

                    if (playerVeh[p][3] >= MAP_WIDTH - 400)
                    {
                        // shoot enemy hq
                        enemyHealth--;
                    }

                    for (c = 0; compVeh.length > c; c++)
                    {
                        if (compVeh[c][0] != 5)
                        {
                            // collision!
                            if (playerVeh[p][2] == compVeh[c][2] 
                                && playerVeh[p][3] == compVeh[c][3])
                            { 
                                // player Jeep
                                if (playerVeh[p][0] == 0 
                                    && compVeh[c][0] == 0)
                                {
                                    playerVeh[p][0] = 5;
                                    compVeh[c][0] = 5;
                                    kills++;
                                    loses++;
                                    compVehCnt--;
                                    playerVehCnt--;
                                }

                                else if (playerVeh[p][0] == 0) // and other veh
                                {
                                    playerVeh[p][0] = 5;
                                    compVeh[c][1] = compVeh[c][1] - 1;
                                    
                                    if (0 >= compVeh[c][1])
                                    {
                                        compVeh[c][0] = 5;
                                        kills++;
                                        compVehCnt--;
                                    }
                                    loses++;
                                    playerVehCnt--;
                                }

                                // player tank
                                else if (playerVeh[p][0] == 1 
                                    && compVeh[c][0] == 0)
                                {
                                    playerVeh[p][1] = playerVeh[p][1] - 1;
                                    if (0 >= playerVeh[p][1])
                                    {
                                        playerVeh[p][0] = 5;
                                        loses++;
                                        playerVehCnt--;
                                    }

                                    compVeh[c][0] = 5;
                                    kills++;
                                    compVehCnt--;
                                }

                                else if (playerVeh[p][0] == 1
                                    && compVeh[c][0] == 1)
                                {
                                    playerVeh[p][0] = 5;
                                    compVeh[c][0] = 5;
                                    kills++;
                                    loses++;
                                    compVehCnt--;
                                    playerVehCnt--;
                                }

                                else if (playerVeh[p][0] == 1 
                                    && compVeh[c][0] ==2)
                                {
                                    playerVeh[p][0] =5;
                                    compVeh[c][1] = compVeh[c][1] - 1;
                                    if (0 >= compVeh[c][1])
                                    {
                                        compVeh[c][0] = 5;
                                        kills++;
                                        compVehCnt--;
                                    }
                                    loses++;
                                    playerVehCnt--;
                                }

                                else if (playerVeh[p][0] == 1
                                    && compVeh[c][0] == 3)
                                {
                                    playerVeh[p][1] = playerVeh[p][1] - 1;
                                    if (0 >= playerVeh[p][1])
                                    {
                                        playerVeh[p][0] = 5;
                                        loses++;
                                        playerVehCnt--;
                                    }
                                    compVeh[c][0] = 5;
                                    kills++;
                                    compVehCnt--;
                                }

                                // player plane
                                else if (playerVeh[p][0] == 2 
                                    && compVeh[c][0] == 0)
                                {
                                    playerVeh[p][1] = playerVeh[p][1] - 1;
                                    if (0 >= playerVeh[p][1])
                                    {
                                        playerVeh[p][0] = 5;
                                        loses++;
                                        playerVehCnt--;
                                    }
                                    compVeh[c][0] = 5;
                                    kills++;
                                    compVehCnt--;
                                }

                                else if (playerVeh[p][0] == 2
                                    && compVeh[c][0] == 1)
                                {
                                    playerVeh[p][1] = playerVeh[p][1] - 1;
                                    
                                    if (0 >= playerVeh[p][1])
                                    {
                                        playerVeh[p][0] = 5;
                                        loses++;
                                        playerVehCnt--;
                                    }

                                    compVeh[c][0] = 5;
                                    kills++;
                                    compVehCnt--;
                                }

                                else if (playerVeh[p][0] == 2 
                                    && compVeh[c][0] ==2)
                                {
                                    playerVeh[p][0] = 5;
                                    compVeh[c][0] = 5;
                                    kills++;
                                    loses++;
                                    compVehCnt--;
                                    playerVehCnt--;
                                }

                                else if (playerVeh[p][0] == 2
                                    && compVeh[c][0] == 3)
                                {
                                    playerVeh[p][0] = 5;
                                    compVeh[c][1] = compVeh[c][1] - 1;
                                    if (0 >= compVeh[c][1])
                                    {
                                        compVeh[c][0] = 5;
                                        kills++;
                                        compVehCnt--;
                                    }
                                    loses++;
                                    playerVehCnt--;
                                }

                                // player aa
                                else if (playerVeh[p][0] == 3 
                                    && compVeh[c][0] == 0)
                                {
                                    playerVeh[p][1] = playerVeh[p][1] - 1;
                                    
                                    if (0 >= playerVeh[p][1])
                                    {
                                        playerVeh[p][0] = 5;
                                        loses++;
                                        playerVehCnt--;
                                    }
                                    compVeh[c][0] = 5;
                                    kills++;
                                    compVehCnt--;
                                }

                                else if (playerVeh[p][0] == 3
                                    && compVeh[c][0] == 1)
                                {
                                    playerVeh[p][0] = 5;
                                    compVeh[c][1] = compVeh[c][1] -1;
                                    if (0 >= compVeh[c][1])
                                    {
                                        compVeh[c][0] = 5;
                                        kills++;
                                        compVehCnt--;
                                    }
                                    loses++;
                                    playerVehCnt--;
                                }

                                else if (playerVeh[p][0] == 3 
                                    && compVeh[c][0] ==2)
                                {
                                    playerVeh[p][1] = playerVeh[p][1] - 1; 

                                    if (0 >= playerVeh[p][1])
                                    {
                                        playerVeh[p][0] = 5;
                                        loses++;
                                        playerVehCnt--;
                                    }
                                    compVeh[c][0] = 5;
                                    kills++;
                                    compVehCnt--;
                                }

                                else if (playerVeh[p][0] == 3
                                    && compVeh[c][0] == 3)
                                {
                                    playerVeh[p][0] = 5;
                                    compVeh[c][0] =5;
                                    kills++;
                                    loses++;
                                    playerVehCnt--;
                                    compVehCnt--;
                                }
                            }
                        }
                    }
                }
            }
        }


        for (c = 0; compVeh.length > c; c++)
        {
            if (compVeh[c][0] != 5)
            {
                if (compVeh[c][3] > 222)
                {
                    compVeh[c][3] = compVeh[c][3] - 1;

                    if (MAP_WIDTH / 2 > compVeh[c][3])
                    {
                        lane = compVeh[c][2];

                        if (lane == 0)
                        {
                            topPoint = 2;
                        }

                        else if (lane == 1)
                        {
                            midPoint = 2;
                        }

                        else
                        {
                            lowPoint = 2;
                        }
                    }

                    if (225 >= compVeh[c][3])
                    {
                        // shoot hq
                        playerHealth--;
                    }

                    for (p = 0; playerVeh.length > p; p++)
                    {
                        if (playerVeh[p][0] != 5)
                        {
                            // collision!
                            if (playerVeh[p][2] == compVeh[c][2] 
                                && playerVeh[p][3] == compVeh[c][3])
                            {
                                // player Jeep
                                if (playerVeh[p][0] == 0 
                                    && compVeh[c][0] == 0)
                                {
                                    playerVeh[p][0] = 5;
                                    compVeh[c][0] = 5;
                                    kills++;
                                    loses++;
                                    compVehCnt--;
                                    playerVehCnt--;
                                }

                                else if (playerVeh[p][0] == 0) // and other veh
                                {
                                    playerVeh[p][0] = 5;
                                    compVeh[c][1] = compVeh[c][1] - 1;
                                    
                                    if (0 >= compVeh[c][1])
                                    {
                                        compVeh[c][0] = 5;
                                        kills++;
                                        compVehCnt--;
                                    }
                                    loses++;
                                    playerVehCnt--;
                                }

                                // player tank
                                else if (playerVeh[p][0] == 1 
                                    && compVeh[c][0] == 0)
                                {
                                    playerVeh[p][1] = playerVeh[p][1] - 1;
                                    if (0 >= playerVeh[p][1])
                                    {
                                        playerVeh[p][0] = 5;
                                        loses++;
                                        playerVehCnt--;
                                    }

                                    compVeh[c][0] = 5;
                                    kills++;
                                    compVehCnt--;
                                }

                                else if (playerVeh[p][0] == 1
                                    && compVeh[c][0] == 1)
                                {
                                    playerVeh[p][0] = 5;
                                    compVeh[c][0] = 5;
                                    kills++;
                                    loses++;
                                    compVehCnt--;
                                    playerVehCnt--;
                                }

                                else if (playerVeh[p][0] == 1 
                                    && compVeh[c][0] ==2)
                                {
                                    playerVeh[p][0] =5;
                                    compVeh[c][1] = compVeh[c][1] - 1;
                                    if (0 >= compVeh[c][1])
                                    {
                                        compVeh[c][0] = 5;
                                        kills++;
                                        compVehCnt--;
                                    }
                                    loses++;
                                    playerVehCnt--;
                                }

                                else if (playerVeh[p][0] == 1
                                    && compVeh[c][0] == 3)
                                {
                                    playerVeh[p][1] = playerVeh[p][1] - 1;
                                    if (0 >= playerVeh[p][1])
                                    {
                                        playerVeh[p][0] = 5;
                                        loses++;
                                        playerVehCnt--;
                                    }
                                    compVeh[c][0] = 5;
                                    kills++;
                                    compVehCnt--;
                                }

                                // player plane
                                else if (playerVeh[p][0] == 2 
                                    && compVeh[c][0] == 0)
                                {
                                    playerVeh[p][1] = playerVeh[p][1] - 1;
                                    if (0 >= playerVeh[p][1])
                                    {
                                        playerVeh[p][0] = 5;
                                        loses++;
                                        playerVehCnt--;
                                    }
                                    compVeh[c][0] = 5;
                                    kills++;
                                    compVehCnt--;
                                }

                                else if (playerVeh[p][0] == 2
                                    && compVeh[c][0] == 1)
                                {
                                    playerVeh[p][1] = playerVeh[p][1] - 1;
                                    
                                    if (0 >= playerVeh[p][1])
                                    {
                                        playerVeh[p][0] = 5;
                                        loses++;
                                        playerVehCnt--;
                                    }

                                    compVeh[c][0] = 5;
                                    kills++;
                                    compVehCnt--;
                                }

                                else if (playerVeh[p][0] == 2 
                                    && compVeh[c][0] ==2)
                                {
                                    playerVeh[p][0] = 5;
                                    compVeh[c][0] = 5;
                                    kills++;
                                    loses++;
                                    compVehCnt--;
                                    playerVehCnt--;
                                }

                                else if (playerVeh[p][0] == 2
                                    && compVeh[c][0] == 3)
                                {
                                    playerVeh[p][0] = 5;
                                    compVeh[c][1] = compVeh[c][1] - 1;
                                    if (0 >= compVeh[c][1])
                                    {
                                        compVeh[c][0] = 5;
                                        kills++;
                                        compVehCnt--;
                                    }
                                    loses++;
                                    playerVehCnt--;
                                }

                                // player aa
                                else if (playerVeh[p][0] == 3 
                                    && compVeh[c][0] == 0)
                                {
                                    playerVeh[p][1] = playerVeh[p][1] - 1;
                                    
                                    if (0 >= playerVeh[p][1])
                                    {
                                        playerVeh[p][0] = 5;
                                        loses++;
                                        playerVehCnt--;
                                    }
                                    compVeh[c][0] = 5;
                                    kills++;
                                    compVehCnt--;
                                }

                                else if (playerVeh[p][0] == 3
                                    && compVeh[c][0] == 1)
                                {
                                    playerVeh[p][0] = 5;
                                    compVeh[c][1] = compVeh[c][1] -1;
                                    if (0 >= compVeh[c][1])
                                    {
                                        compVeh[c][0] = 5;
                                        kills++;
                                        compVehCnt--;
                                    }
                                    loses++;
                                    playerVehCnt--;
                                }

                                else if (playerVeh[p][0] == 3 
                                    && compVeh[c][0] ==2)
                                {
                                    playerVeh[p][1] = playerVeh[p][1] - 1; 

                                    if (0 >= playerVeh[p][1])
                                    {
                                        playerVeh[p][0] = 5;
                                        loses++;
                                        playerVehCnt--;
                                    }
                                    compVeh[c][0] = 5;
                                    kills++;
                                    compVehCnt--;
                                }

                                else if (playerVeh[p][0] == 3
                                    && compVeh[c][0] == 3)
                                {
                                    playerVeh[p][0] = 5;
                                    compVeh[c][0] =5;
                                    kills++;
                                    loses++;
                                    playerVehCnt--;
                                    compVehCnt--;
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private void drawVehicles(Graphics2D g2d)
    {
        int i;
        int j;

        int type = 5;
        int health = 5;
        int lane = 5;
        int x = 5;
        int y = 5;

        for (i = 0; playerVeh.length > i; i++)
        {
        
            if (playerVeh[i][0] != 5)
            {
                type = playerVeh[i][0];
                health = playerVeh[i][1];
                lane = playerVeh[i][2];
                x = playerVeh[i][3];

                if (lane == 0)
                    y = 150;
                else if (lane == 1)
                    y = 300;
                else
                    y = 450;
            
            
                // player jeep
                if (type == 0 && health == 2)
                {
                    g2d.drawImage(JeepP, x, y, null);
                }

                else if (type == 0 && health == 1)
                {
                    g2d.drawImage(JeepP1, x, y, null);
                }

                // player tank
                else if (type == 1 && health == 4)
                {
                    g2d.drawImage(TankP, x, y, null);
                }

                else if (type == 1 && health == 3)
                {
                    g2d.drawImage(TankP3, x, y, null);
                }

                else if (type == 1 && health == 2)
                {
                    g2d.drawImage(TankP2, x, y, null);
                }

                else if (type == 1 && health == 1)
                {
                    g2d.drawImage(TankP1, x, y, null);
                }

                // player plane
                else if (type == 2 && health == 4)
                {
                    g2d.drawImage(PlaneP, x, y, null);
                }

                else if (type == 2 && health == 3)
                {
                    g2d.drawImage(PlaneP3, x, y, null);
                }

                else if (type == 2 && health == 2)
                {
                    g2d.drawImage(PlaneP2, x, y, null);
                }

                else if (type == 2 && health == 1)
                {
                    g2d.drawImage(PlaneP1, x, y, null);
                }
            
                // player AA
                else if (type == 3 && health == 4)
                {
                    g2d.drawImage(AAP, x, y, null);
                }

                else if (type == 3 && health == 3)
                {
                    g2d.drawImage(AAP3, x, y, null);
                }

                else if (type == 3 && health == 2)
                {
                    g2d.drawImage(AAP2, x, y, null);
                }

                else if (type == 3 && health == 1)
                {
                    g2d.drawImage(AAP1, x, y, null);
                }
            }
        }

        type = 5;
        health = 5;
        lane = 5;
        x = 5;
        y = 5;
         
        for (j = 0; compVeh.length > j; j++)
        {
            if (compVeh[j][0] != 5)
            {
                type = compVeh[j][0];
                health = compVeh[j][1];
                lane = compVeh[j][2];
                x = compVeh[j][3];
                    if (lane == 0)
                        y = 150;
                    else if (lane == 1)
                        y = 300;
                    else
                        y = 450;
            

                // comp jeep
                if (type == 0 && health == 2)
                {
                    g2d.drawImage(JeepC, x, y, null);
                }

                else if (type == 0 && health == 1)
                {
                    g2d.drawImage(JeepC1, x, y, null);
                }

                // comp tank
                else if (type == 1 && health == 4)
                {
                    g2d.drawImage(TankC, x, y, null);
                }

                else if (type == 1 && health == 3)
                {
                    g2d.drawImage(TankC3, x, y, null);
                }

                else if (type == 1 && health == 2)
                {
                    g2d.drawImage(TankC2, x, y, null);
                }

                else if (type == 1 && health == 1)
                {
                    g2d.drawImage(TankC1, x, y, null);
                }

                // comp plane
                else if (type == 2 && health == 4)
                { 
                    g2d.drawImage(PlaneC, x, y, null);
                }

                else if (type == 2 && health == 3)
                {
                    g2d.drawImage(PlaneC3, x, y, null);
                }

                else if (type == 2 && health == 2)
                {
                    g2d.drawImage(PlaneC2, x, y, null);
                }

                else if (type == 2 && health == 1)
                {
                    g2d.drawImage(PlaneC1, x, y, null);
                }
            
                // comp AA
                else if (type == 3 && health == 4)
                {
                    g2d.drawImage(AAC, x, y, null);
                }

                else if (type == 3 && health == 3)
                {
                    g2d.drawImage(AAC3, x, y, null);
                }

                else if (type == 3 && health == 2)
                {
                    g2d.drawImage(AAC2, x, y, null);
                }

                else if (type == 3 && health == 1)
                {
                    g2d.drawImage(AAC1, x, y, null);
                }
            }
        }
    }

    private void stopGame()
    {
        timer.stop();
    }

    public class TAdapter extends KeyAdapter
    {
        @Override
        public void keyPressed(KeyEvent e)
        {
            int key = e.getKeyCode();

            int i;
            int j;

            if (inGame)
            {
                if (key == KeyEvent.VK_UP)
                {
                    currentLane--;

                    if (0 > currentLane)
                    {
                        currentLane = 2;
                    }
                }

                else if (key == KeyEvent.VK_DOWN)
                {
                    currentLane++;

                    if (currentLane >= 3)
                    {
                        currentLane = 0;
                    }
                }

                else if (key == KeyEvent.VK_NUMPAD0)
                {
                    if (playerMoney >= 150)
                    {
                        if (30 >= playerVehCnt)
                        {
                            playerMoney = playerMoney - 150;
                            playerVehCnt++;

                            for (i = 0; playerVeh.length > i; i++)
                            {
                                if (playerVeh[i][0] == 5)
                                {
                                    playerVeh[i][0] = 0;
                                    playerVeh[i][1] = 2;
                                    playerVeh[i][2] = currentLane;
                                    playerVeh[i][3] = 210;
                                    playerVeh[i][4] = 0;
                                    break;
                                }
                            }
                        }
                    }
                }

                else if (key == KeyEvent.VK_NUMPAD1)
                {
                    if (playerMoney >= 500)
                    {
                        // build tank
                        if (30 >= playerVehCnt)
                        {
                            playerMoney = playerMoney - 500;
                            playerVehCnt++;

                            for (i = 0; playerVeh.length > i; i++)
                            {
                                if (playerVeh[i][0] == 5)
                                {
                                    playerVeh[i][0] = 1;
                                    playerVeh[i][1] = 4;
                                    playerVeh[i][2] = currentLane;
                                    playerVeh[i][3] = 210;
                                    playerVeh[i][4] = 0;
                                    break;
                                }
                            }
                        }
                    }
                }

                else if (key == KeyEvent.VK_NUMPAD2)
                {
                    if (playerMoney >= 500)
                    {
                        // build plane
                        if (30 >= playerVehCnt)
                        {
                            playerMoney = playerMoney - 500;
                            playerVehCnt++;

                            for (i = 0; playerVeh.length > i; i++)
                            {
                                if (playerVeh[i][0] == 5)
                                {
                                    playerVeh[i][0] = 2;
                                    playerVeh[i][1] = 4;
                                    playerVeh[i][2] = currentLane;
                                    playerVeh[i][3] = 210;
                                    playerVeh[i][4] = 0;
                                    break;
                                }
                            }
                        }
                    }
                }

                else if (key == KeyEvent.VK_NUMPAD3)
                {
                    if (playerMoney >= 500)
                    {
                        // build aa
                        if (30 >= playerVehCnt)
                        {
                            playerMoney = playerMoney - 500;
                            playerVehCnt++;

                            for (i = 0; playerVeh.length > i; i++)
                            {
                                if (playerVeh[i][0] == 5)
                                {
                                    playerVeh[i][0] = 3;
                                    playerVeh[i][1] = 4;
                                    playerVeh[i][2] = currentLane;
                                    playerVeh[i][3] = 210;
                                    playerVeh[i][4] = 0;
                                    break;
                                }
                            }
                        }
                    }
                }

                else if (key == KeyEvent.VK_PAUSE)
                {
                    if (timer.isRunning())
                    {
                        timer.stop();
                    }
                    else
                    {
                        timer.start();
                    }
                }
            }

            else
            {
                if (key == KeyEvent.VK_NUMPAD0)
                {
                    // no ai
                    aiLevel = 4;
                    inGame = true;
                    startGame();
                }
                
                else if (key == KeyEvent.VK_NUMPAD1)
                {
                    aiLevel = 0;
                    inGame = true;
                    startGame();
                }

                else if (key == KeyEvent.VK_NUMPAD2)
                {
                    aiLevel = 1;
                    inGame = true;
                    startGame();
                }

                else if (key == KeyEvent.VK_NUMPAD3)
                {
                    aiLevel = 2;
                    inGame = true;
                    startGame();
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {    
         repaint();        
    }
    

}
