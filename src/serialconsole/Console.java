/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serialconsole;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.*; 
import java.util.Random;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
/**
 *
 * @author Ngu
 */
public class Console extends Frame implements Runnable, SerialPortEventListener{

    
    
    
    //color 
    Color[] mColors = {Color.BLACK, Color.BLUE, Color.CYAN, Color.GRAY, Color.ORANGE, Color.YELLOW, Color.PINK};

    //kích thước box
    private static final int BOX_WIDTH = 3200;
    private static final int BOX_HEIGHT = 4800;
    //vẽ lại ball
    Thread mThread;

    //mảng Ball
    ArrayList<Point> mPoints = new ArrayList<>();

//số lượng ball
    int numberBall;
    
     //--------------
    SerialPort serialPort = null;

    private static final String PORT_NAMES[] = {
        "/dev/cu.wchusbserial1420",
        "/dev/tty.usbmodem", // Mac OS X
    //        "/dev/usbdev", // Linux
    //        "/dev/tty", // Linux
    //        "/dev/serial", // Linux
    //        "COM3", // Windows
    };

    private String appName;
    private BufferedReader input;
    private OutputStream output;

    private static final int TIME_OUT = 1000; // Port open timeout
    private static final int DATA_RATE = 9600; // Arduino serial port

    public boolean initialize() {
        try {
            CommPortIdentifier portId = null;
            Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

            // Enumerate system ports and try connecting to Arduino over each
            //
            System.out.println("Trying:");
            while (portId == null && portEnum.hasMoreElements()) {
                // Iterate through your host computer's serial port IDs
                //
                CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
                System.out.println("   port" + currPortId.getName());
                for (String portName : PORT_NAMES) {
                    if (currPortId.getName().equals(portName)
                            || currPortId.getName().startsWith(portName)) {

                        // Try to connect to the Arduino on this port
                        //
                        // Open serial port
                        serialPort = (SerialPort) currPortId.open(appName, TIME_OUT);
                        portId = currPortId;
                        System.out.println("Connected on port" + currPortId.getName());
                        break;
                    }
                }
            }

            if (portId == null || serialPort == null) {
                System.out.println("Oops... Could not connect to Arduino");
                return false;
            }

            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);

            // Give the Arduino some time
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void sendData(String data) {
        try {
            System.out.println("Sending data: '" + data + "'");

            // open the streams and send the "y" character
            output = serialPort.getOutputStream();
            output.write(data.getBytes());
        } catch (Exception e) {
            System.err.println(e.toString());
            System.exit(0);
        }
    }

    //
    // This should be called when you stop using the port
    //
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    //
    // Handle serial port event
    //
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        //System.out.println("Event received: " + oEvent.toString());
        try {
            switch (oEvent.getEventType()) {
                case SerialPortEvent.DATA_AVAILABLE:
                    if (input == null) {
                        input = new BufferedReader(
                                new InputStreamReader(
                                        serialPort.getInputStream()));
                    }
                    System.out.println("------");
                    
                    String x = input.readLine();                    
                    System.out.println(x);
                    
                    String y = input.readLine();
                    System.out.println(y);
                    
                    Point mPoint = new Point(Integer.parseInt(x), Integer.parseInt(y), 20, Color.BLUE);
                    mPoints.add(mPoint);
                    
                    //Thread.sleep(100);
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }
    //--------------
    
    

    public Console() {
        /**
         * Frame
         */
        setResizable(false);
        setTitle("BallCollusion");
        setBounds(10, 10, BOX_WIDTH, BOX_HEIGHT);
        //setSize(BOX_WIDTH, BOX_HEIGHT);
        setVisible(true);
        //exit 
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });
        
        appName = getClass().getName();
        /**
         * init Ball[10]
         */
        /**
         * repaint();
         */
        mThread = new Thread(this);
        mThread.start();
    }

    public void paint(Graphics graphics) {
        try {
            for (int i = 0; i < mPoints.size(); i++) {
                graphics.setColor(mPoints.get(i).getPoint_color());
                int x = mPoints.get(i).getPoint_x();
                int y = mPoints.get(i).getPoint_y();
                int radius = mPoints.get(i).getPoint_radius();
                graphics.fillOval(x, y, radius, radius);
            }
            
        } catch (Exception e) {
            System.out.println("" + e);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                repaint();
            } catch (Exception e) {
                System.out.println("" + e);
            }
        }
    }
    /**
     *
     * @param array
     * @return Color.
     */
    public static Color getRandom(Color[] array) {
        int index = new Random().nextInt(array.length);
        return array[index];
    }

    /**
     * @param max
     * @param min
     * @return int
     */
    public static int getRandom(int max, int min) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
    
    
    
    
    
   
    public static void main(String[] args) {
        Console mConsole = new Console();
        mConsole.initialize();
         try {
            Thread.sleep(20000);
            mConsole.close();
        } catch (InterruptedException ie) {
        }
        
    }

}