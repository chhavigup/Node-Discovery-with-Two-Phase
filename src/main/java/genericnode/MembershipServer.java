/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package genericnode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the membership server for key value type
 *
 * @author nehaa
 */
public class MembershipServer {

//    private static HashMap<String, String> ipPortData = new HashMap<>();
    
    public static void executeCommand(int port) throws IOException {
        ServerSocket s = new ServerSocket(port);
        MembershipServerData ipPortData = new MembershipServerData();

        while (true) {
            Socket socket = s.accept();

            DataInputStream dataInput = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            String string = dataInput.readUTF();
            System.out.println(string + "testing\n");

            String cmd = string.substring(0, string.indexOf(" "));
            String key = string.substring(string.indexOf(" ") + 1, string.indexOf("+"));
            String value = string.substring(string.indexOf("+")+1);

            String str = "Invalid command";

            if (cmd.equals("putip") && key != "" && value != "") {
                System.out.println("putip\n");
                ipPortData.addData(key, value);
                str = "server response:put key=" + key;
            } else if (cmd.equals("getdata")) {
                System.out.println("getdata\n");
                str = ipPortData.getData();
            } else if (cmd.equals("exit")) {
                str = "<the server then exits>";
                output.writeUTF(str);
                s.close();
                break;
            }

            output.writeUTF(str);
            System.out.println("Data sent to client" + str);

        }
    }
}
