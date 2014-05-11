package smtpserver;

import java.io.*;
import java.net.*;

public class incioServidor {

    static ServerSocket s;

    public static void main(String args[]) {
        try {
            s = new ServerSocket(25);
            
            while (true) {
                Socket incoming = s.accept();
                Thread t = new Thread(new ServidorSMTP(incoming));
                t.start();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
