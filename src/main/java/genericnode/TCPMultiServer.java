/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 * This class contains the logic for TCP server to run on multi threads
 *
 * @author nehaa
 */
package genericnode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is for Key value mutli thread server node
 *
 * @author nehaa
 */
public class TCPMultiServer {

    MembershipServerData membershipServerData;
//    ConcurrentHashMap<String, ArrayList<String>> ipPortMap;
    private Timer timer;

    private ConcurrentHashMap<String, String> lockMap = new ConcurrentHashMap<>();
    private HashMap<String, String> data = new HashMap<String, String>();
    private String ms_ip;
    private int ms_port;
    private int port;
    private String ipAddress;

    public void executeCommand(int portNumber, String msIp, int msPort) throws IOException {
        port = portNumber;
        ms_ip = msIp;
        ms_port = msPort;
        ipAddress = InetAddress.getLocalHost().getHostAddress();

        //Act like client and make call to membership server, to update add its ip and port
        notifyMembershipServer(port, ms_ip, ms_port);

        //Act like client and get the full ipData from MS
        String membershipDataString = getMembershipData(ms_ip, ms_port);
        membershipServerData = new MembershipServerData();
        membershipServerData.reloadData(membershipDataString);
//        ipPortMap = MembershipServerData.getMapFromData(membershipDataString);

        //Start a thread to read MS data and keep updating it
        syncMembershipServerData(port, ms_ip, ms_port);

        ServerSocket s = new ServerSocket(port);
        try {
            while (true) {
                Socket socket = s.accept();
                DataInputStream dataInput = new DataInputStream(socket.getInputStream());
                String inputString = dataInput.readUTF();

                String cmd = inputString.substring(0, inputString.indexOf(" "));
                String key = inputString.substring(inputString.indexOf(" "), inputString.indexOf("+"));
                String value = inputString.substring(inputString.indexOf("+"));

                //if exit, close socket
                if (cmd.equals("exit")) {
                    DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                    String str = "<the server then exits>";
                    output.writeUTF(str);
                    s.close();

                    break;
                } else {
                    new Thread(new TCPServerThread(socket, inputString)).start();
                }
            }
        } catch (SocketException e) {
            System.out.println("Server is closed");
        } catch (IOException ex) {
            System.out.println("Server is closed");
        }
    }

    public void notifyMembershipServer(int port, String ms_ip, int ms_port) {
        try {
            System.out.println("Notify membership \n");
            String responseString = TCPClient.executeCommand(ms_ip, ms_port, "putip", ipAddress, String.valueOf(port));
            System.out.println("MS Server says: " + responseString);

        } catch (IOException u) {
            System.out.println(u);
        }
    }

    public String getMembershipData(String ms_ip, int ms_port) {
        try {
//            System.out.println("get membership \n");

            String responseString = TCPClient.executeCommand(ms_ip, ms_port, "getdata", "", "");
            System.out.println("MS Server says: " + responseString);
            return responseString;
        } catch (IOException u) {
            System.out.println(u);
            return "";
        }
    }

    public void syncMembershipServerData(int port, String ms_ip, int ms_port) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                updateMembershipData();
            }
        }, 30000L, 30000L); //Refreshes every 30 seconds
    }

    public void updateMembershipData() {
        String membershipData = getMembershipData(ms_ip, ms_port);
        membershipServerData.reloadData(membershipData);
        System.out.println("updated membership :" + membershipServerData.getData());
//        ipPortMap = MembershipServerData.getMapFromData(membershipData);
    }

    public class TCPServerThread implements Runnable {

        String inputString;
        Socket socket;

        public TCPServerThread(Socket socket, String inputString) {
            this.inputString = inputString;
            this.socket = socket;
        }

        @Override
        public void run() {
            try {

                DataOutputStream output = new DataOutputStream(socket.getOutputStream());

                String cmd = inputString.substring(0, inputString.indexOf(" "));
                String key = inputString.substring(inputString.indexOf(" "), inputString.indexOf("+"));
                String value = inputString.substring(inputString.indexOf("+") + 1);

                String str = "Invalid command";

                if (cmd.equals("put") && key != "" && value != "") {
                    str = doPut(key, value);
                } else if (cmd.equals("dput1") && key != "" && value != "") {
                    if (lockMap.get(key) == null) {
                        System.out.println("locking!");
                        lockMap.put(key, value);
                        str = "LOCKED key=" + key;
                    } else {
                        str = "ABORT key=" + key;
                    }
                } else if (cmd.equals("dput2") && key != "" && value != "") {
                    
                    data.put(key, value);

                    lockMap.remove(key);
                    str = "SUCCESS server response:put key=" + key;
                } else if (cmd.equals("dputabort") && key != "" && value != "") {
                    lockMap.remove(key);
                    str = "LOCK Removed for dputabort key=" + key;
                } else if (cmd.equals("get") && key != "") {
                    str = "server response:get key=" + key + " get val=" + data.get(key);
                } else if (cmd.equals("del") && key != "") {
                    str = doDel(key);
//                    data.remove(key);
//                    str = "server response:delete key=" + key;
                } else if (cmd.equals("ddel1") && key != "") {
                    if (lockMap.get(key) == null) {
                        System.out.println("locking for del1!");
                        lockMap.put(key, value);
                        str = "LOCKED key=" + key;
                    } else {
                        str = "ABORT key=" + key;
                    }
                } else if (cmd.equals("ddel2") && key != "") {
                    data.remove(key);

                    lockMap.remove(key);
                    str = "SUCCESS server response:delete key=" + key;
                } else if (cmd.equals("ddelabort") && key != "") {
                    lockMap.remove(key);
                    str = "LOCK Removed for ddelabort key=" + key;
                } else if (cmd.equals("store")) {
                    String st = "";
                    for (Map.Entry<String, String> map : data.entrySet()) {
                        st += "server response:" + "\n" + "key:" + map.getKey() + ":value:" + map.getValue() + ":";

                    }
                    str = st;
                }

                output.writeUTF(str);
                System.out.println("Data sent to client" + str);

            } catch (SocketException e) {
                System.out.println("Server is closing");
            } catch (IOException ex) {
                System.out.println("Server is closing");
            }

        }

        public String doDel(String key) {
//            data.remove(key);
//            String str = "server response:delete key=" + key;
//            return str;
            
            boolean successddel1 = false;
            for (int i = 0; i < 10; i++) {
                successddel1 = dDel1Iterate(key);
                if (successddel1) {
                    System.out.println("successful in ddel1");
                    break;
                }
            }

            System.out.println("successddel1" + successddel1);
            if (successddel1) {
                System.out.println("trying dodel2Iterate");
                //do dput2 for all nodes
                doDel2Iterate(key);

                System.out.println("removing from data");
                //do for local node
                data.remove(key);
                return "server response:delete key=" + key;
            }

            return "server response:error could not del key=" + key;
            
        }

        public String doPut(String key, String value) {

            boolean successdput1 = false;
            for (int i = 0; i < 10; i++) {
                successdput1 = dPut1Iterate(key, value);
                if (successdput1) {
                    System.out.println("successful in dput1");
                    break;
                }
            }

            if (successdput1) {
                //do dput2 for all nodes
                doPut2Iterate(key, value);

                //do for local node
                data.put(key, value);
                return "server response:put key=" + key;
            }

            return "server response:error could not put key=" + key;
        }

        public boolean dPut1Iterate(String key, String value) {
            ConcurrentHashMap<String, ArrayList<String>> lockedIPMap = new ConcurrentHashMap<String, ArrayList<String>>();
            System.out.println("dPut1Iterate");
            ConcurrentHashMap<String, ArrayList<String>> currentIPPortMap = membershipServerData.getMembershipData();
            for (Map.Entry<String, ArrayList<String>> entry : currentIPPortMap.entrySet()) {
                if (entry != null) {

                    String ip = entry.getKey();
                    ArrayList<String> portList = entry.getValue();
                    System.out.println("portlist" + portList);
                    for (int i = 0; i < portList.size(); i++) {
                        System.out.println("current " + ipAddress + port + " trying " + ip + Integer.parseInt(portList.get(i)) + ipAddress.equals(ip) + (Integer.parseInt(portList.get(i)) == port));
                        //skip of this server node
                        if (ipAddress.equals(ip) && Integer.parseInt(portList.get(i)) == port) {
                            System.out.println("skipping " + ipAddress + port);
                            continue;
                        }

                        String response = "ABORT";
                        try {
                            System.out.println("calling client dput1 " + ip + " " + portList.get(i));
                            response = TCPClient.executeCommand(ip, Integer.parseInt(portList.get(i)), "dput1", key, value);
                        } catch (Exception e) {
                            System.out.println(e);
                            response = "ABORT";
                        }

                        if (response.contains("LOCKED")) {
                            MembershipServerData.addData(lockedIPMap, ip, portList.get(i));
                        } else if (response.contains("ABORT")) {
                            System.out.println("Got Abort");
                            //do abort for entire lockedIPMap
                            doAbort1Iterate(lockedIPMap, key, value);
                            return false;
                        }

                    }

                }
            }

            return true;
        }
        
        public boolean dDel1Iterate(String key) {
            ConcurrentHashMap<String, ArrayList<String>> lockedIPMap = new ConcurrentHashMap<String, ArrayList<String>>();
            System.out.println("dDel1Iterate");
            ConcurrentHashMap<String, ArrayList<String>> currentIPPortMap = membershipServerData.getMembershipData();
            for (Map.Entry<String, ArrayList<String>> entry : currentIPPortMap.entrySet()) {
                if (entry != null) {

                    String ip = entry.getKey();
                    ArrayList<String> portList = entry.getValue();
                    System.out.println("portlist" + portList);
                    for (int i = 0; i < portList.size(); i++) {
                        System.out.println("current " + ipAddress + port + " trying " + ip + Integer.parseInt(portList.get(i)) + ipAddress.equals(ip) + (Integer.parseInt(portList.get(i)) == port));
                        //skip of this server node
                        if (ipAddress.equals(ip) && Integer.parseInt(portList.get(i)) == port) {
                            System.out.println("skipping " + ipAddress + port);
                            continue;
                        }

                        String response = "ABORT";
                        try {
                            System.out.println("calling client ddel1 " + ip + " " + portList.get(i));
                            response = TCPClient.executeCommand(ip, Integer.parseInt(portList.get(i)), "ddel1", key, "dummy");
                        } catch (Exception e) {
                            System.out.println(e);
                            response = "ABORT";
                        }

                        if (response.contains("LOCKED")) {
                            MembershipServerData.addData(lockedIPMap, ip, portList.get(i));
                        } else if (response.contains("ABORT")) {
                            System.out.println("Got Abort ddel1");
                            //do abort for entire lockedIPMap
                            doDelAbortIterate(lockedIPMap, key);
                            return false;
                        }

                    }

                }
            }

            return true;
        }

        public void doAbort1Iterate(ConcurrentHashMap<String, ArrayList<String>> lockedIPMap, String key, String value) {
            for (Map.Entry<String, ArrayList<String>> entry : lockedIPMap.entrySet()) {
                if (entry != null) {

                    String ip = entry.getKey();
                    ArrayList<String> portList = entry.getValue();
                    for (int i = 0; i < portList.size(); i++) {
                        if (ipAddress.equals(ip) && Integer.parseInt(portList.get(i)) == port) {
                            continue;
                        }

                        try {

                            String response = TCPClient.executeCommand(ip, Integer.parseInt(portList.get(i)), "dputabort", key, value);
                        } catch (Exception e) {
                            //do nothing
                        }
                    }

                }
            }
        }

        public void doDelAbortIterate(ConcurrentHashMap<String, ArrayList<String>> lockedIPMap, String key) {
            for (Map.Entry<String, ArrayList<String>> entry : lockedIPMap.entrySet()) {
                if (entry != null) {

                    String ip = entry.getKey();
                    ArrayList<String> portList = entry.getValue();
                    for (int i = 0; i < portList.size(); i++) {
                        if (ipAddress.equals(ip) && Integer.parseInt(portList.get(i)) == port) {
                            continue;
                        }

                        try {

                            String response = TCPClient.executeCommand(ip, Integer.parseInt(portList.get(i)), "ddelabort", key, "DUMMY");
                        } catch (Exception e) {
                            //do nothing
                        }
                    }

                }
            }
        }

        public void doPut2Iterate(String key, String value) {
            ConcurrentHashMap<String, ArrayList<String>> currentIPPortMap = membershipServerData.getMembershipData();
            for (Map.Entry<String, ArrayList<String>> entry : currentIPPortMap.entrySet()) {
                if (entry != null) {

                    String ip = entry.getKey();
                    ArrayList<String> portList = entry.getValue();
                    for (int i = 0; i < portList.size(); i++) {
                        if (ipAddress.equals(ip) && Integer.parseInt(portList.get(i)) == port) {
                            continue;
                        }

                        try {
                            String response = TCPClient.executeCommand(ip, Integer.parseInt(portList.get(i)), "dput2", key, value);
                        } catch (Exception e) {
                            //do nothing
                        }
                    }

                }
            }
        }
        
        public void doDel2Iterate(String key) {
            ConcurrentHashMap<String, ArrayList<String>> currentIPPortMap = membershipServerData.getMembershipData();
            for (Map.Entry<String, ArrayList<String>> entry : currentIPPortMap.entrySet()) {
                if (entry != null) {

                    String ip = entry.getKey();
                    ArrayList<String> portList = entry.getValue();
                    for (int i = 0; i < portList.size(); i++) {
                        if (ipAddress.equals(ip) && Integer.parseInt(portList.get(i)) == port) {
                            System.out.println("skipping self");
                            continue;
                        }

                        try {
                            String response = TCPClient.executeCommand(ip, Integer.parseInt(portList.get(i)), "ddel2", key, "DUMMY");
                        } catch (Exception e) {
                            //do nothing
                        }
                    }

                }
            }
        }
    }
}
