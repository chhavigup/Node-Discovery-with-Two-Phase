/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package genericnode;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * This is the client code to be used 
 * @author nehaa
 */
public class TCPClient {

    public static String executeCommand(String addr, int port, String cmd, String key, String val) throws IOException {
        Socket s = new Socket(addr, port);
        
        DataOutputStream dout = new DataOutputStream(s.getOutputStream());
        dout.writeUTF(cmd + " " + key + "+" + val);
        
        DataInputStream input = new DataInputStream(new BufferedInputStream(s.getInputStream()));
        String br = input.readUTF();

        return br;
    }

}
