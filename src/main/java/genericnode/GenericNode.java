/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package genericnode;

import java.io.IOException;
import java.net.InetAddress;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author wlloyd
 */
public class GenericNode {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
//        HashMap<String, String> data = new HashMap<>();
        ConcurrentHashMap<String, String> data = new ConcurrentHashMap<>();

        Long startTime = 0L;

        if (args[0].equals("tc")) {
            System.out.println("TCP CLIENT");
            String addr = args[1];
            int port = Integer.parseInt(args[2]);
            String cmd = args[3];
            String key = (args.length > 4) ? args[4] : "";
            String val = (args.length > 5) ? args[5] : "";
            try {

                String responseString = TCPClient.executeCommand(addr, port, cmd, key, val);
                System.out.println("Server says: \n " + responseString);

            } catch (IOException u) {
                System.out.println(u);
            }

        } else if (args[0].equals("ts")) {
            System.out.println("TCP SERVER");
            int port = Integer.parseInt(args[1]);

            String ms_ip = args[2];
            int ms_port = Integer.parseInt(args[3]);

            try {

                TCPMultiServer tcpMultiServer = new TCPMultiServer();
                tcpMultiServer.executeCommand(port, ms_ip, ms_port);

            } catch (Exception e) {
                System.out.println(e);
                System.out.println("Server is closed");
            }
        } else if (args[0].equals("ts_config")) {
            System.out.println("TCP SERVER");
            int port = Integer.parseInt(args[1]);

//            String ms_ip = args[2];
//            int ms_port = Integer.parseInt(args[3]);

            try {

                TCPMultiServerConfig tcpMultiServerConfig = new TCPMultiServerConfig();
                tcpMultiServerConfig.executeCommand(port, "dummy", 111);

            } catch (Exception e) {
                System.out.println(e);
                System.out.println("Server is closed");
            }

        } else if (args[0].equals("ms_start")) {
            System.out.println("Starting Membership Server");

            try {
                int port = 1234;

                MembershipServer.executeCommand(port);

            } catch (Exception e) {
                System.out.println(e);
                System.out.println("Server is closing");
            }

        } else {
            String msg = "GenericNode Usage:\n\n"
                    + "Client:\n"
                    + "uc/tc <address> <port> put <key> <msg>  UDP/TCP CLIENT: Put an object into store\n"
                    + "uc/tc <address> <port> get <key>  UDP/TCP CLIENT: Get an object from store by key\n"
                    + "uc/tc <address> <port> del <key>  UDP/TCP CLIENT: Delete an object from store by key\n"
                    + "uc/tc <address> <port> store  UDP/TCP CLIENT: Display object store\n"
                    + "uc/tc <address> <port> exit  UDP/TCP CLIENT: Shutdown server\n"
                    + "rmic <address> put <key> <msg>  RMI CLIENT: Put an object into store\n"
                    + "rmic <address> get <key>  RMI CLIENT: Get an object from store by key\n"
                    + "rmic <address> del <key>  RMI CLIENT: Delete an object from store by key\n"
                    + "rmic <address> store  RMI CLIENT: Display object store\n"
                    + "rmic <address> exit  RMI CLIENT: Shutdown server\n\n"
                    + "Server:\n"
                    + "us/ts <port>  UDP/TCP SERVER: run udp or tcp server on <port>.\n"
                    + "rmis  run RMI Server.\n";
            System.out.println(msg);
        }

    }
}
