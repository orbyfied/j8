package j8_util.math.expr;

import net.orbyfied.j8.util.math.expr.Context;
import net.orbyfied.j8.util.math.expr.ExpressionNode;
import net.orbyfied.j8.util.math.expr.ExpressionParser;
import net.orbyfied.j8.util.math.expr.ExpressionValue;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Key;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

public class SimpleGraphingTest {

    static final int W = 800;
    static final int H = 600;

    // gui stuff
    JFrame frame;
    JPanel panel;
    JTextField textField;
    JLabel     statusLabel;

    // rendering
    Canvas canvas;
    BufferStrategy bs;

    // math
    Context ctx;
    ExpressionParser parser;
    ExpressionNode   node;

    // input thread
    Thread inputThread;
    volatile boolean close = false;

    //////////////////////

    volatile HashSet<Character> keys = new HashSet<>();

    volatile double camx;
    volatile double camy;
    volatile double cams = 1;

    void init() {
        // init math
        parser = new ExpressionParser();
        ctx = Context.newDefaultGlobal();

        // init gui
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame  = new JFrame();
        canvas = new Canvas();

        frame.add(canvas);

        frame.setPreferredSize(new Dimension(W, H));
        frame.pack();
        frame.setVisible(true);

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                synchronized (keys) {
                    keys.add(e.getKeyChar());
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                synchronized (keys) {
                    keys.remove(e.getKeyChar());
                }
            }
        });

        // init rendering
        canvas.createBufferStrategy(2);
        bs = canvas.getBufferStrategy();
    }

    void redraw(Graphics2D g) {
        g.setStroke(new BasicStroke(3));

        double h2 = H / 2f;
        double w2 = W / 2f;

        if (node != null) {
            int lx = 0;
            int ly = 0;
            double xv = 1 / cams;
            for (double x = camx; x < camx + W; x += xv) {
                // set up context
                Context local = ctx.child()
                        .setValue("x", x);

                // evaluate expression
                ExpressionValue<?> value = node.evaluate(local);
                if (value.isNil())
                    continue;
                double dy = value.getValueAs(Double.class);

                // plot point
                int sx = (int)(x  + w2 - camx);
                int sy = (int)(dy - h2 - camy);
                g.setColor(Color.BLACK);
//                g.fillOval(x - 3, tny - 3, 6, 6);
                g.drawLine(sx, sy, lx, ly);

                // update last
                lx = sx;
                ly = sy;
            }
        }
    }

    final BasicStroke st1 = new BasicStroke(1);

    void mainLoop() {
        final int gw = 32;
        final int gh = 32;
        while (frame.isVisible() && !close) {
            // prepare for render
            Graphics2D g = (Graphics2D) bs.getDrawGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // clear screen
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, W, H);

            // render
            redraw(g);

            // draw grid
            g.setStroke(st1);
            int cgx = (int) (camx % gw);
            int cgy = (int) (-camy % gh);
            int mx  = (W - cgx) + gw;
            int my  = (H - cgy) + gh;
            for (int x = -cgx; x < mx; x += gw) {
                double wx = camx + x;
                for (int y = -cgy; y < my; y += gh) {
                    double wy = -camy + y;
                    g.setColor(Color.BLACK);
                    g.drawLine(0, y, W, y);
                    g.setColor(Color.GREEN);
                    g.drawString("y: " + wy, W - 50, y);
                }
                g.setColor(Color.BLACK);
                g.drawLine(x, 0, x, H);
                g.setColor(Color.GREEN);
                g.drawString("x: " + wx, x, H - 50);
            }

            // input
            synchronized (keys) {
                if (keys.contains('w'))
                    camy += 1.5;
                if (keys.contains('s'))
                    camy -= 1.5;
                if (keys.contains('a'))
                    camx -= 1.5;
                if (keys.contains('d'))
                    camx += 1.5;
            }

            // show
            bs.show();

            // sleep
            sleep(10);
        }

        close = true;
    }

    volatile Scanner scanner = new Scanner(System.in);

    @Test
    void test() {
        init();

        node = parser.forString("x")
                .lex()
                .parse()
                .getAstNode();

        inputThread = new Thread(() -> {
            System.out.println(":: Started Input.");

            while (!close) {
                // wait for input
                String input;
                try {
                    input = scanner.nextLine();
                } catch (Exception e) { continue; }

                // parse input
                switch (input.charAt(0)) {
                    case '=' -> {
                        try {
                            // reparse
                            synchronized (node) {
                                node = parser.forString(textField.getText())
                                        .lex()
                                        .parse()
                                        .getAstNode();
                            }

                            System.out.println("> Successfully parsed!");
                        } catch (Exception e) {
                            System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
                        }
                    }
                }

                switch (input) {
                    case "exit" -> {
                        close = true;
                    }
                }
            }
        }, "TestInputThread");

        inputThread.start();

        // main loop
        mainLoop();
    }

    void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
