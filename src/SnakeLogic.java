import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.TimerTask;

public class SnakeLogic extends JPanel implements ActionListener, Observer {


    private Timer timer;
    private int moves = 0;
    private boolean isItAWin = false;
    //[delay] changes update speed. default is 100.
    protected int delay = 100;
    private long mouseTime = 0;
    MovingMachine vehicle;
    Random rand = new Random();

    //snake and mouse coordinates and hard limits
    protected final int MOUSELIMIT = 4;
    protected final int SNAKELIMIT = 50;
    protected int[] snakeX = new int[SNAKELIMIT];
    protected int[] snakeY = new int[SNAKELIMIT];
    protected ArrayList<Integer> mouseX = new ArrayList<>();
    protected ArrayList<Integer> mouseY = new ArrayList<>();
    protected int mouseNum = 0;
    protected int snakeLength = 3;
    protected boolean[] mouseMoveState = new boolean[MOUSELIMIT];

    //mouse spawn and move numbers
    //[mouseDelay] changes rate that the mice spawn and move, delay will be [mouseDelay]*[mouseTime]. default is 20.
    private int mouseDelay = 20;
    private int mouseDelayTime = 0;
    private int mouseSpawnPercentage = 50;
    private int mouseMovePercentage = 50;

    //car delay time
    //[carDelay] changes rate that the car is summoned. default 500
    protected int carDelayTime = 0;
    protected int carDelay = 150;
    protected int carWarningTime = 120;
    protected boolean carWarning = false;
    protected int leftOrRight;

    //snake movement and facing direction
    private boolean north = false;
    private boolean south = false;
    private boolean east = false;
    private boolean west = false;

    //Artwork
    // background images *randomize eventually*
    private BufferedImage bgGrass;
    private BufferedImage bgDesert;
    private BufferedImage bgKitchen;

    // snake and mouse art
    private BufferedImage mouseIdle;
    private BufferedImage mouseMove;
    private BufferedImage northMouth;
    private BufferedImage eastMouth;
    private BufferedImage southMouth;
    private BufferedImage westMouth;
    private BufferedImage northMouth1;
    private BufferedImage eastMouth1;
    private BufferedImage southMouth1;
    private BufferedImage westMouth1;
    private BufferedImage snakeBody;
    private BufferedImage vacuum;
    private BufferedImage car;
    private BufferedImage deathSign;

    public SnakeLogic(World world) {
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        world.addObserver(this);
        this.addKeyListener(world);
        initBoard();
    }

    private void initBoard() {
        try {
            String filePath = "C:\\Users\\Adrian\\Desktop\\Programming\\Github\\BetterPrettySnake\\BetterSnakeGame\\src\\assets\\";
            bgGrass = ImageIO.read(new File(filePath + "bgGrass.png"));
//            bgDesert = ImageIO.read(new File("C:\\Users\\Adrian\\Desktop\\Programming\\Github\\BetterPrettySnake\\BetterSnakeGame\\src\\assets\\bgDesert.png"));
//            bgKitchen = ImageIO.read(new File("C:\\Users\\Adrian\\Desktop\\Programming\\Github\\BetterPrettySnake\\BetterSnakeGame\\src\\assets\\bgKitchen.png"));

            mouseIdle = ImageIO.read(new File(filePath+"mouseIdle.png"));
            mouseMove = ImageIO.read(new File(filePath+"mouseMove.png"));
            northMouth = ImageIO.read(new File(filePath+"northMouth.png"));
            northMouth1 = ImageIO.read(new File(filePath+"northMouth2.png"));
            eastMouth = ImageIO.read(new File(filePath+"eastMouth.png"));
            eastMouth1 = ImageIO.read(new File(filePath+"eastMouth2.png"));
            southMouth = ImageIO.read(new File(filePath+"southMouth.png"));
            southMouth1 = ImageIO.read(new File(filePath+"southMouth2.png"));
            westMouth = ImageIO.read(new File(filePath+"westMouth.png"));
            westMouth1 = ImageIO.read(new File(filePath+"westMouth2.png"));
            snakeBody = ImageIO.read(new File(filePath+"snakeBody.png"));
            car = ImageIO.read(new File(filePath+"car.png"));
            vacuum = ImageIO.read(new File(filePath+"vacuum.png"));
            deathSign = ImageIO.read(new File(filePath+"deathSign.png"));

        } catch (IOException ex) {
            this.setBackground(Color.BLACK);
            ex.printStackTrace();
        }

        timer = new Timer(delay, this);
        timer.start();
        Arrays.fill(mouseMoveState, false);

        // instantiate vehicle
        // if Kitchen
        int n = 0;
        switch (n) {
            case 0:
                vehicle = new Car(this);
                break;
//                case 1: MovingMachine vehicle = new Vacuum(this);
        }
    }

    //multiplies x,y coords in order for painting
    protected int calc(char x, int i) {
        // for snake: x,y
        //for mouse: m,n
        if (x == 'x') {
            return ((snakeX[i] * 25) + 25);
        }
        if (x == 'y') {
            return ((snakeY[i] * 25) + 25);
        }
        if (x == 'm') {
            return ((mouseX.get(i) * 25) + 25);
        }
        if (x == 'n') {
            return ((mouseY.get(i) * 25) + 25);
        }
        System.out.println("calc error");
        return 1;
    }

    protected void stopMovement() {
        north = false;
        south = false;
        east = false;
        west = false;
    }

    private void bodyLocationUpdate(int i) {
        snakeY[i] = snakeY[i - 1];
        snakeX[i] = snakeX[i - 1];
    }

    //if spot is not occupied by snake or boundaries
    private boolean spotIsValid(int x, int y) {
        if (x > -2 && x < 27 && y > -2 && y < 26) {
            for (int i = 0; i < snakeLength; i++) {
                if (x == snakeX[i] && y == snakeY[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean mouseSpotIsValid(int x, int y) {
        if (spotIsValid(x, y)) {
            for (int i = 0; i < mouseNum; i++) {
                if (x == mouseX.get(i) && y == mouseY.get(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    protected void mouseSpawn() {
        int x = 1;
        int y = 1;
        do {
            x = Math.abs(rand.nextInt() % 26);
            y = Math.abs(rand.nextInt() % 26);
        }
        while (!(mouseSpotIsValid(x, y)));
        mouseX.add(x);
        mouseY.add(y);
        mouseNum++;
    }


    private void mouseMove(int i) {
        int n = Math.abs(rand.nextInt() % 4);
        switch (n) {
            case 0:
                if (mouseSpotIsValid(mouseX.get(i) + 1, mouseY.get(i))) {
                    mouseX.set(i, mouseX.get(i) + 1);
                }
                break;
            case 1:
                if (mouseSpotIsValid(mouseX.get(i) - 1, mouseY.get(i))) {
                    mouseX.set(i, mouseX.get(i) - 1);
                }
                break;
            case 2:
                if (mouseSpotIsValid(mouseX.get(i), mouseY.get(i) + 1)) {
                    mouseY.set(i, mouseY.get(i) + 1);
                }
                break;
            case 3:
                if (mouseSpotIsValid(mouseX.get(i), mouseY.get(i) - 1)) {
                    mouseY.set(i, mouseY.get(i) - 1);
                }
                break;
        }
    }

    protected boolean eatBehavior(int i) {
        if (snakeLength < SNAKELIMIT) {
            mouseX.remove(i);
            mouseY.remove(i);
            mouseNum--;
            growSnake();
            if (mouseNum == 0) {
                mouseSpawn();
            }
            return true;
        }
        return false;
    }

    protected void growSnake() {
        snakeX[snakeLength] = snakeX[snakeLength - 1];
        snakeY[snakeLength] = snakeY[snakeLength - 1];
        snakeLength++;
    }


    private void endGame() {
        System.out.println("game over");
    }


    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        //draws bg depending on the number called
        //NEEDS UPDATING
        g.drawImage(bgGrass, 0, 0, this);

        //paints initial snake position
        if (moves == 0) {
            snakeX[0] = 6;
            snakeX[1] = 5;
            snakeX[2] = 4;

            snakeY[0] = 4;
            snakeY[1] = 4;
            snakeY[2] = 4;
            east = true;
        }

        //paints snake head and body
        for (int i = 0; i < snakeLength; i++) {
            if (i == 0) {
                if (north) {
                    g.drawImage(northMouth, calc('x', i), calc('y', i), this);
                }
                if (east) {
                    g.drawImage(eastMouth, calc('x', i), calc('y', i), this);
                }
                if (west) {
                    g.drawImage(westMouth, calc('x', i), calc('y', i), this);
                }
                if (south) {
                    g.drawImage(southMouth, calc('x', i), calc('y', i), this);
                }
            }
            if (i != 0) {
                g.drawImage(snakeBody, calc('x', i), calc('y', i), this);
            }
        }

        //paints mice
        for (int i = 0; i < mouseNum; i++) {
            if (mouseMoveState[i] == true) {
                g.drawImage(mouseMove, calc('m', i), calc('n', i), this);
            } else {
                g.drawImage(mouseIdle, calc('m', i), calc('n', i), this);
            }
        }

        //paints warning sign
        if (carWarning) {
            if (leftOrRight == 1) {
                g.drawImage(deathSign, 34, 309, 66, 341, 0, 0, 32, 32, this);
            } else {
                g.drawImage(deathSign, 34, 309, 66, 341, 32, 0, 0, 32, this);
            }
        }

        //paints car/vacuum
        //if(background is kitchen)
        //paint vacuum
        if (vehicle.exists) {
            if (vehicle.isVac) {
                g.drawImage(vacuum, vehicle.calc(vehicle.x), vehicle.calcY(vehicle.y), this);
            } else {
                g.drawImage(car, vehicle.calc(vehicle.x), vehicle.calcY(vehicle.y), this);
            }
        }

        //else

    }

    @Override
    @SuppressWarnings("Duplicates")
    public void actionPerformed(ActionEvent e) {
        //snake head and body move
        if (north) {
            for (int i = snakeLength - 1; i > -1; i--) {
                if (i == 0) {
                    if (!spotIsValid(snakeX[0], snakeY[0] - 1)) {
                        endGame();
                    }
                    snakeY[0]--;
                } else bodyLocationUpdate(i);
            }
        }
        if (south) {
            for (int i = snakeLength - 1; i > -1; i--) {
                if (i == 0) {
                    if (!spotIsValid(snakeX[0], snakeY[0] + 1)) {
                        endGame();
                    }
                    snakeY[0]++;
                } else bodyLocationUpdate(i);
            }
        }
        if (east) {
            for (int i = snakeLength - 1; i > -1; i--) {
                if (i == 0) {
                    if (!spotIsValid(snakeX[0] + 1, snakeY[0])) {
                        endGame();
                    }
                    snakeX[0]++;
                } else bodyLocationUpdate(i);
            }
        }
        if (west) {
            for (int i = snakeLength - 1; i > -1; i--) {
                if (i == 0) {
                    if (!spotIsValid(snakeX[0] - 1, snakeY[0])) {
                        endGame();
                    }
                    snakeX[0]--;
                } else bodyLocationUpdate(i);
            }
        }

        //mouse spawn and move
        mouseDelayTime++;
        if (mouseDelayTime == mouseDelay) {
            if (Math.abs(rand.nextInt() % 100) < mouseSpawnPercentage && mouseNum < MOUSELIMIT) {
                mouseSpawn();
            }
            for (int i = 0; i < mouseNum; i++) {
                if (Math.abs(rand.nextInt() % 100) < mouseMovePercentage) {
                    mouseMove(i);
                }
            }
            mouseDelayTime = 0;
        }

        //eating behavior
        if (mouseX.contains(snakeX[0])) {
            for (int i = 0; i < mouseNum; i++) {
                if (mouseX.get(i) == snakeX[0] && mouseY.get(i) == snakeY[0]) {
                    eatBehavior(i);

                }
            }
        }

        //car behavior

        carDelayTime++;
        if (carDelayTime == carWarningTime) {
            leftOrRight = Math.abs(rand.nextInt() % 2);
        }
        if (carDelayTime > carWarningTime && carDelayTime < 141) {
            carWarning = true;
        } else {
            carWarning = false;
        }

        if (carDelay == carDelayTime) {
            switch (leftOrRight) {
                case 0:
                    vehicle.spawnLeft();
                    break;
                case 1:
                    vehicle.spawnRight();
                    break;
            }
            carDelayTime = 0;
        }

        if (vehicle.left) {
            vehicle.x--;
        } else if (vehicle.right) {
            vehicle.x++;
        }
        if (vehicle.exists && (vehicle.x == vehicle.spawn2 || vehicle.x == vehicle.spawn1)) {
            vehicle.despawn();
        }


        repaint();
    }

    //changes direction that the snake is facing
    public void update(KeyEvent e) {

        //WILL CLOSE GAME AND WINDOW
//        if (e.getKeyCode() == KeyEvent.VK_1) {
//            JComponent comp = (JComponent) e.getSource();
//            Window win = SwingUtilities.getWindowAncestor(comp);
//            win.dispose();
//            System.out.println("goodbye");
//        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (!south) {
                moves++;
                stopMovement();
                north = true;
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            if (!north) {
                moves++;
                stopMovement();
                south = true;
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (!west) {
                moves++;
                stopMovement();
                east = true;
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (!east) {
                moves++;
                stopMovement();
                west = true;
            }
        }

    }
}