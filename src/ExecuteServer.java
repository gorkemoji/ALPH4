/*
 * Emircan Görkem ECE - 210303049
 * Ebrar Esila Mutlu - 190303066
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;

public class ExecuteServer extends JFrame {
    JTextField txt1, txt3;
    JTextArea txt2;
    JButton btn1, btn2;
    JLabel lbl1;
    int port;
    boolean pressed = false;
    boolean btnDscnnt = false;
    boolean typing = false;
    Socket link;
    ServerSocket servSock;
    BufferedReader input;
    PrintWriter output;
    InetSocketAddress ipAddress;
    static String cliIPAddress;

    public ExecuteServer() {
        super("SERVER APP");

        setSize(400,400);
        setLayout(null);
        setResizable(false);

        lbl1 = new JLabel("Port: ");
        lbl1.setBounds(20,-10,100,100);

        txt1 = new JTextField(1);
        txt1.setBounds(60,31,40,20);

        txt2 = new JTextArea();
        txt2.setBounds(43,80,300,200);
        txt2.setEditable(false);
        txt2.setBackground(Color.white);

        btn1 = new JButton("Start");
        btn1.setBounds(120,26,85,30);
        btn1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btnDscnnt){
                    try {
                        input.close();
                        output.close();
                        servSock.close();
                        link.close();
                        txt2.setText("Closed the connection.");
                        System.exit(0);
                    } catch (IOException er) {
                        er.printStackTrace();
                    }
                }
                port = Integer.parseInt(txt1.getText());
                pressed = true;
            }
        });

        txt3 = new JTextField(2);
        txt3.setBounds(43,290,220,35);
        txt3.setToolTipText("Type your message here.");

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
        add(btn1);
        add(txt2);
        add(txt3);
        add(btn2);

        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    public void run() throws InterruptedException {
        Encryption enc = new Encryption();

        while (!pressed) {
            Thread.sleep(200);
        }

        try {
            servSock = new ServerSocket(port);
            txt2.setText("The server is now at listening mode. Port: " + port);

            link = servSock.accept();
            txt2.setText("Connected! Now you can chat with Client!");
            input = new BufferedReader(new InputStreamReader(link.getInputStream())); // It was a Scanner's object but I had to use "readLine();"
            output = new PrintWriter(link.getOutputStream(),true);
            btnDscnnt = true;
            btn1.setText("Dscnnct");
            txt1.setEditable(false);
            txt2.setEditable(false);
            Thread.sleep(5000);
            txt2.setText(null);

            ipAddress = (InetSocketAddress) link.getRemoteSocketAddress();
            cliIPAddress = ipAddress.getAddress().getHostAddress();

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

                        output.println(enc.encrypt(txt3.getText()));
                        if (!txt2.getText().equals(""))
                        txt2.setText(txt2.getText() + "\n" + "Server: " + txt3.getText());
                        else
                            txt2.setText("Server: " + txt3.getText());
                        try {
                            logWriter("1", txt3.getText());
                            txt3.setText("");
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
                            if (!txt2.getText().equals(""))
                            txt2.setText(txt2.getText() + "\n" + "Client: " + enc.decrypt(in));
                            else
                                txt2.setText("Client: " + enc.decrypt(in));
                            in = input.readLine();
                            if (!(cliIPAddress.equals("localhost")||cliIPAddress.equals("127.0.0.1"))) {
                                logWriter("2", enc.decrypt(in));
                            }
                        }
                        input.close();
                        output.close();
                        servSock.close();
                        link.close();
                    } catch (IOException e) {
                        txt2.setText("Port closed.");
                        txt3.setEditable(false);
                        btn1.setText("Exit");
                        txt1.setText("");
                        btn2.setEnabled(false);
                    }
                }
            });

            syncReceive.start();

        } catch (Exception e) {
            System.out.println("The port you want the app to listen is full, incorrect or closed the connection...");
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
            bw.write("][Server][");
        }
        else {
            bw.write("][Client][");
        }

        bw.write(message);
        bw.write("]");
        bw.newLine();
        bw.close();
    }
}

