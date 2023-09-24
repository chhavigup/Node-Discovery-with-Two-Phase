/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package genericnode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is for storing the data for Key value central server type
 * @author nehaa
 */
public class MembershipServerData {

    ConcurrentHashMap<String, ArrayList<String>> ipPortMap;

    public MembershipServerData() {
        ipPortMap = new ConcurrentHashMap<String, ArrayList<String>>();
    }

    public MembershipServerData(ConcurrentHashMap<String, ArrayList<String>> ipPortMap) {
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
        for(Map.Entry<String, ArrayList<String>> entry : ipPortMap.entrySet()) {
            if(entry != null) {
                //if not last in arraylist add ,
                if(str != "") {
                    str += ",";
                }
                
                String ip = entry.getKey();
                ArrayList<String> portList = entry.getValue();
                for(int i = 0; i < portList.size() ; i++) {
                    String substring = ip + ":" + portList.get(i);
                    str += substring;
                    
                    //if last in arraylist skip ,
                    if(i < (portList.size() - 1)) {
                        str += ",";
                    }
                }
                
                
            }
        }
        
        return str;
    }
    
    public void reloadData(String data) {
        ConcurrentHashMap<String, ArrayList<String>> finalMap = new ConcurrentHashMap<String, ArrayList<String>>();
        
        if(data == "") {
            return;
        }
        
        List<String> initList = Arrays.asList(data.split(","));
        
        for(String item : initList) {            
            String[] iterString = item.split(":");
            addData(finalMap, iterString[0], iterString[1]);
            
        }
            
        ipPortMap = finalMap;
        //return finalMap;
    }

    public static ConcurrentHashMap<String, ArrayList<String>> getMapFromData(String data) {
        ConcurrentHashMap<String, ArrayList<String>> finalMap = new ConcurrentHashMap<String, ArrayList<String>>();
        
        if(data == "") {
            return finalMap;
        }
        
        List<String> initList = Arrays.asList(data.split(","));
        
        for(String item : initList) {            
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
