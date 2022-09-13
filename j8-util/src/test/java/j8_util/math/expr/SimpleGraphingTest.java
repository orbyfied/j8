package j8_util.math.expr;

import net.orbyfied.j8.util.math.expr.Context;
import net.orbyfied.j8.util.math.expr.ExpressionNode;
import net.orbyfied.j8.util.math.expr.ExpressionParser;
import net.orbyfied.j8.util.math.expr.ExpressionValue;
import net.orbyfied.j8.util.math.expr.error.ExprException;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.util.HashSet;
import java.util.Scanner;

public class SimpleGraphingTest {

    static final int W = 1280;
    static final int H = 720;

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

    int statusMessageTime;
    String statusMessage;
    Color statusColor;
    StringBuilder exprStrBuilder;
    volatile boolean endTextEntry;

    boolean showDebug;

    void endText() {
        statusMessageTime = 100;

        // parse
        try {
            node = parser.forString(exprStrBuilder.toString())
                    .lex()
                    .parse()
                    .getAstNode();

            statusColor = Color.GREEN;
            statusMessage = "Successfully parsed.";
        } catch (Exception e) {
            statusColor = Color.RED;
            statusMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    void init() {
        // init math
        parser = new ExpressionParser();
        ctx = Context.newDefaultGlobal();
//        cache = new double[W];

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

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int k = e.getKeyCode();

                if (k == /* F3 */ 114) {
                    showDebug = !showDebug;
                    return;
                }

                if (k == /* enter */ 10) {
                    // enter text mode
                    if (exprStrBuilder == null) {
                        exprStrBuilder = new StringBuilder();
                        return;
                    } else {
                        // end text mode
                        endText();
                        endTextEntry = true;
                        return;
                    }
                }

                if (exprStrBuilder != null && k == /* esc */ 27) {
                    // exit text mode
                    endTextEntry = true;
                    return;
                }

                if (exprStrBuilder != null) {
                    switch (k) {
                        case /* backspace */ 8 -> {
                            if (exprStrBuilder.length() == 0)
                                break;
                            exprStrBuilder.delete(exprStrBuilder.length() - 1, exprStrBuilder.length() + 1);
                        }

                        // ignore
                        case /* shift */ 16, /* ctrl */ 17 -> { /* ignore */ }
                        default -> exprStrBuilder.append(e.getKeyChar());
                    }

                }

                synchronized (keys) {
                    if (exprStrBuilder == null) {
                        keys.add(e.getKeyChar());
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                synchronized (keys) {
                    keys.remove(e.getKeyChar());
                }
            }
        });

        frame.setPreferredSize(new Dimension(W, H));
        frame.setTitle("Graphing Calculator");
        frame.pack();
        frame.setVisible(true);

        // init rendering
        canvas.createBufferStrategy(2);
        bs = canvas.getBufferStrategy();

        camx = W / 2f;
        camy = H / 2f;
    }

    void redrawLine(Graphics2D g) {
        g.setStroke(new BasicStroke(3));

        double h2 = H / 2f;
        double w2 = W / 2f;


        if (node != null) {
            try {
                int lx = -1;
                int ly = -1;
                double xv = 1 / cams;
                for (double x = camx - w2; x < camx - w2 + W; x += xv) {
                    // set up context
                    Context local = ctx.child()
                            .setValue("x", x);

                    // evaluate expression
                    ExpressionValue<?> value = node.evaluate(local);
                    if (value.isNil() || value.getType() != ExpressionValue.Type.NUMBER)
                        continue;
                    double dy = value.getValueAs(Double.class);

                    // plot point
                    int sx = (int) (x + w2 - camx);
                    int sy = H - (int) (dy + h2 - camy);
                    g.setColor(Color.BLACK);
//                g.fillOval(x - 3, tny - 3, 6, 6);
                    g.drawLine(sx, sy, lx == -1 ? sx : lx, ly == -1 ? sy : ly);
                    if ((int) (x / 4) == 0 && (int) (dy / 4) == 0)
                        g.fillOval(sx - 4, sy - 4, 8, 8);

                    // update last
                    lx = sx;
                    ly = sy;
                }
            } catch (ExprException e) {
                statusMessageTime = 100;
                statusColor = Color.RED;
                statusMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                if (e.getLocation() != null)
                    statusMessage += "\n     " + e.getLocation().toStringFancy(10, false);
            }
        }
    }

    int tc = 0;
    float targetFps = 60.0f;
    float targetSt  = 1f / targetFps;
    float fps = 0.0f;
    double elapsedTime = 0.0f;

    final BasicStroke st1  = new BasicStroke(1);
    final BasicStroke st1b = new BasicStroke(3);
    final Color gcol = Color.GREEN.darker().darker();
    final Color tbgc = new Color(0, 0, 0, 120);
    final Font font = new Font("Sans Serif", Font.PLAIN, 10);
    final Font txtFont = new Font("Sans Serif", Font.PLAIN, 18);
    final AffineTransform xtt = new AffineTransform();

    {
        xtt.setToRotation(Math.toRadians(270), 5, 5);
    }

    void mainLoop() {
        final int gw = 32;
        final int gh = 32;

        long t1;
        long t2;
        while (frame.isVisible() && !close) {
            // setup
            final double w2 = W / 2f;
            final double h2 = H / 2f;

            // timings
            t1 = System.currentTimeMillis();

            // prepare for render
            Graphics2D g = (Graphics2D) bs.getDrawGraphics();
            g.setFont(font);

            // clear screen
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, W, H);

            // draw grid
            g.setStroke(st1);
            int cgx = (int) (camx % gw);
            int cgy = (int) (-camy % gh);
            int mx  = (W - cgx) + gw;
            int my  = (H - cgy) + gh;

            for (int x = -cgx; x < mx; x += gw) {
                double wx = camx + x - w2;
                for (int y = -cgy; y < my; y += gh) {
                    double wy = camy + (H - y) - h2;
                    if ((int)wy == 0)
                        g.setStroke(st1b);
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawLine(0, y, W, y);
                    g.setStroke(st1);
                    g.setColor(gcol);
                    g.drawString("y: " + wy, W - 75, y);
                }
                if ((int)wx == 0)
                    g.setStroke(st1b);
                g.setColor(Color.LIGHT_GRAY);
                g.drawLine(x, 0, x, H);
                g.setStroke(st1);
                g.setColor(gcol);
                g.drawString("x: " + wx, x, H - 50);
            }

            // render
            redrawLine(g);

            // render text shit
            if (statusMessageTime > 0) {
                String[] sln = statusMessage.split("\n");
                statusMessageTime--;
                g.setFont(txtFont);

                for (int ln = sln.length - 1; ln >= 0; ln--) {
                    g.setColor(tbgc);
                    g.fillRect(0, H - 124 - ((sln.length - ln) * 24), W, 24);
                    g.setColor(statusColor);
                    g.drawString(sln[ln], 20, H - 105 - ((sln.length - ln) * 24));
                }

            }

            if (exprStrBuilder != null) {
                g.setFont(txtFont);
                g.setColor(tbgc);
                g.fillRect(0, H - 90, W, 40);
                g.setColor(Color.WHITE);
                g.drawString(exprStrBuilder.toString() +
                        ((tc / 5) % 2 == 0 ? "_" : ""), 20, H - 65);
                if (endTextEntry) {
                    exprStrBuilder = null;
                    endTextEntry = false;
                }
            }

            // show debug
            if (showDebug) {
                g.setColor(tbgc);
                g.fillRect(20, 20, 250, 250);
                g.setColor(Color.WHITE);
                g.setFont(txtFont);
                g.drawString("FPS/MAX: " + fps + " / " + targetFps, 25, 40);
                g.drawString("Elapsed: " + elapsedTime, 25, 55);
            }

            // input
            synchronized (keys) {
                if (keys.contains('w'))
                    camy += 800 * elapsedTime;
                if (keys.contains('s'))
                    camy -= 800 * elapsedTime;
                if (keys.contains('a'))
                    camx -= 800 * elapsedTime;
                if (keys.contains('d'))
                    camx += 800 * elapsedTime;
                if (keys.contains('i'))
                    cams += 3 * elapsedTime;
                if (keys.contains('o'))
                    cams -= 3 * elapsedTime;
                if (keys.contains('r')) {
                    camx = W / 2f;
                    camy = H / 2f;
                    cams = 1f;
                }
            }

            // show
            bs.show();

            // timings
            t2 = System.currentTimeMillis();
            long t = t2 - t1;
            double elapsed = t / 1000f;

            // update values
            elapsedTime = elapsed;
            fps = (float) Math.min(targetFps, 1f / elapsed);
            // sleep tick
            if (elapsed < targetSt)
                sleep((int) ((targetSt - elapsed) * 1000));

            tc++;
        }

        close = true;
    }

    volatile Scanner scanner = new Scanner(System.in);

    @Test
    void test() {
        init();

        String str;
        str = "sin(x / 20) * 100";
//        str = "(x/10) ^ 3";

        node = parser.forString(str)
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
