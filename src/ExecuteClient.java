/*
 * Emircan Görkem ECE - 210303049
 * Ebrar Esila Mutlu - 190303066
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;

public class ExecuteClient extends JFrame {
    JTextField txt1, txt2, txt4;
    JTextArea txt3;
    JButton btn1, btn2;
    JLabel lbl1, lbl2;
    int port;
    boolean pressed = false;
    boolean btnDscnnt = false;
    boolean typing = false;
    static String ip;
    Socket link;
    BufferedReader input;
    PrintWriter output;
    public ExecuteClient() {
        super("CLIENT APP");

        setSize(400,400);
        setLayout(null);

        lbl1 = new JLabel("Server IP: ");
        lbl1.setBounds(20,-10,100,100);

        txt1 = new JTextField(1);
        txt1.setBounds(85,31,97,20);

        lbl2 = new JLabel("Port: ");
        lbl2.setBounds(191,4,100,70);

        txt2 = new JTextField(1);
        txt2.setBounds(225,31,40,20);

        btn1 = new JButton("Connect");
        btn1.setBounds(280,25,85,30);
        btn1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btnDscnnt){
                    try {
                        link.close();
                        input.close();
                        output.close();
                        txt3.setText("Closed the connection.");
                        System.exit(0);
                    }
                    catch (IOException er){
                        er.printStackTrace();
                    }
                }
                ip = txt1.getText();
                port = Integer.parseInt(txt2.getText());
                pressed = true;
            }
        });

        txt3 = new JTextArea();
        txt3.setBounds(43,80,300,200);
        txt3.setEditable(false);
        txt3.setBackground(Color.white);

        txt4 = new JTextField(2);
        txt4.setBounds(43,290,220,35);
        txt4.setToolTipText("Type your message here.");

        btn2 = new JButton("Send");
        btn2.setBounds(271,291,70,32);
        btn2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                typing = true;
            }
        });

        add(lbl1);
        add(txt1);
        add(lbl2);
        add(txt2);
        add(btn1);
        add(txt3);
        add(txt4);
        add(btn2);

        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    public void run() throws InterruptedException {
        Encryption enc = new Encryption();

        while(!pressed) {
            Thread.sleep(200);
        }

        try {
            link = new Socket(ip, port);
            input = new BufferedReader(new InputStreamReader(link.getInputStream())); // It was a Scanner's object but I had to use "readLine();"
            output = new PrintWriter(link.getOutputStream(),true);

            txt3.setText("Connected! Now you can chat with Server!");
            btn1.setText("Dscnnct");
            btnDscnnt = true;
            txt1.setEditable(false);
            txt2.setEditable(false);
            Thread.sleep(5000);
            txt3.setText(null);

            Thread syncSend = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        while (!typing) {
                            try {
                                Thread.sleep(200);
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }

                        output.println(enc.encrypt(txt4.getText()));
                        if (!txt3.getText().equals(""))
                        txt3.setText(txt3.getText() + "\n" + "Client: " + txt4.getText());
                        else
                            txt3.setText("Client: " + txt4.getText());
                        try {
                            logWriter("1", txt4.getText());
                            txt4.setText("");
                            typing = false;
                        } catch (Exception e) {
                            System.out.println("Error");
                        }
                    }
                }
            });

            syncSend.start();

            Thread syncReceive = new Thread(new Runnable() {
                String in; // It gave me an inner error. That's why I declared it here.
                @Override
                public void run() {
                    try {
                        in = input.readLine(); // It gave me an IOException when I started to use BufferedReader instead of Scanner.

                        while (in != null) {
                            if (!txt3.getText().equals(""))
                                txt3.setText(txt3.getText() + "\n" + "Server: " + enc.decrypt(in));
                            else
                                txt3.setText("Server: " + enc.decrypt(in));
                            in = input.readLine();
                            if (!((ip.equals("localhost")||ip.equals("127.0.0.1")))) {
                                logWriter("2", enc.decrypt(in));
                            }
                        }
                        input.close();
                        output.close();
                        link.close();
                    } catch (IOException e) {
                        txt3.setText("Port closed.");
                        btn1.setText("Exit");
                        txt1.setText("");
                        txt2.setText("");
                        txt4.setEditable(false);
                        btn2.setEnabled(false);
                    }
                }
            });

            syncReceive.start();

        } catch (Exception e) {
            System.out.println("The port you want the app to connect is full, incorrect or closed the connection...");
        }
    }
    public static void logWriter(String mode, String message) throws IOException {
        LocalDate ld = LocalDate.now();
        LocalTime lt = LocalTime.now();
        FileWriter fw = new FileWriter("logs.txt",true);
        BufferedWriter bw = new BufferedWriter(fw);

        String dateNow = ld.toString();
        String timeNow = lt.toString();

        String[] date = dateNow.split("-");
        String[] time = timeNow.split(":");

        bw.write("[" + date[2] + "." + date[1] + "." + date[0] + " ");
        bw.write(time[0] + ":" + time[1] + ":" + time[2].substring(0,2));

        if (mode.equals("1")) {
            bw.write("][Client][");
        }
        else {
            bw.write("][Server][");
        }

        bw.write(message);
        bw.write("]");
        bw.newLine();
        bw.close();
    }
}
