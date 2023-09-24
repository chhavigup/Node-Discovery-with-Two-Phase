/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package genericnode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is used for Nodes.cfg config type to manage data in the file
 * @author nehaa
 */
public class MembershipServerDataConfig {

    String port;
    ConcurrentHashMap<String, ArrayList<String>> ipPortMap;

    public MembershipServerDataConfig() {
        ipPortMap = new ConcurrentHashMap<String, ArrayList<String>>();
    }

    public MembershipServerDataConfig(ConcurrentHashMap<String, ArrayList<String>> ipPortMap) {
        this.ipPortMap = ipPortMap;
    }

    public ConcurrentHashMap<String, ArrayList<String>> getMembershipData() {
        return ipPortMap;
    }

    public void addData(String ip, String port) {
        ArrayList<String> tempList = null;
        if (ipPortMap.containsKey(ip)) {
            tempList = ipPortMap.get(ip);
            if (tempList == null) {
                tempList = new ArrayList<String>();
            }
            tempList.add(port);
        } else {
            tempList = new ArrayList();
            tempList.add(port);
        }
        ipPortMap.put(ip, tempList);
    }

    //return string = {ip:port,ip:port}
    public String getData() {
        String str = "";

        //convert hashmap into string
        for (Map.Entry<String, ArrayList<String>> entry : ipPortMap.entrySet()) {
            if (entry != null) {
                //if not last in arraylist add ,
                if (str != "") {
                    str += ",";
                }

                String ip = entry.getKey();
                ArrayList<String> portList = entry.getValue();
                for (int i = 0; i < portList.size(); i++) {
                    String substring = ip + ":" + portList.get(i);
                    str += substring;

                    //if last in arraylist skip ,
                    if (i < (portList.size() - 1)) {
                        str += ",";
                    }
                }

            }
        }

        return str;
    }

    public void reloadDataFromNodeCFG() {
        try {
            File f1 = new File("/tmp/nodes.cfg");
            if (!f1.exists()) {
                f1.createNewFile(); //dummy file gets created, this would be later updated with the right content
            }

            String tmpdir = System.getProperty("java.io.tmpdir");
            System.out.println(tmpdir);
            BufferedReader reader = new BufferedReader(new FileReader("/../../tmp/nodes.cfg"));
            String current;
            String tobetransmit = "";
            String trim;
            int skipComma = 0;
            while ((current = reader.readLine()) != null) {
                if (skipComma > 0) {
                    tobetransmit += ",";
                }
                trim = current.trim();
                tobetransmit += trim;
                skipComma++;
            }

            reloadData(tobetransmit);
        } catch (Exception e) {
            //do nothing
            System.out.println(e);
        }
    }

    public void reloadData(String data) {
        ConcurrentHashMap<String, ArrayList<String>> finalMap = new ConcurrentHashMap<String, ArrayList<String>>();

        if (data == "") {
            return;
        }

        List<String> initList = Arrays.asList(data.split(","));

        for (String item : initList) {
            String[] iterString = item.split(":");
            addData(finalMap, iterString[0], iterString[1]);

        }

        ipPortMap = finalMap;
        //return finalMap;
    }

    public static ConcurrentHashMap<String, ArrayList<String>> getMapFromData(String data) {
        ConcurrentHashMap<String, ArrayList<String>> finalMap = new ConcurrentHashMap<String, ArrayList<String>>();

        if (data == "") {
            return finalMap;
        }

        List<String> initList = Arrays.asList(data.split(","));

        for (String item : initList) {
            String[] iterString = item.split(":");
            addData(finalMap, iterString[0], iterString[1]);

        }

        return finalMap;

    }

    public static void addData(ConcurrentHashMap<String, ArrayList<String>> ipPortMap, String ip, String port) {
        ArrayList<String> tempList = null;
        if (ipPortMap.containsKey(ip)) {
            tempList = ipPortMap.get(ip);
            if (tempList == null) {
                tempList = new ArrayList<String>();
            }
            tempList.add(port);
        } else {
            tempList = new ArrayList();
            tempList.add(port);
        }
        ipPortMap.put(ip, tempList);
    }

}
