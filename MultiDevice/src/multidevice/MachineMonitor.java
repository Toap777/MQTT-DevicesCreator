/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multidevice;

import java.util.Random;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author eliseu
 */
public class MachineMonitor {    
    private static final String DEVICE_TYPE = "nobroker";
    private static final String SNIFFER_IP = "192.168.0.101";
    private static final String BROKER_PORT = "1883";
    private static final String TOPIC_REGIST_DEVICE = "/registdevice";   
    private static final String TOPIC_GET_LIST = "/getlist";
    
    private static final int SAMPLING_RATE = 1000;
    
    /**
     * @param args the command line arguments
     * @throws org.json.JSONException
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws JSONException, InterruptedException {
        
        JSONObject registJSON = new JSONObject();
        //String deviceID = args[0];
        String deviceID = "Device_" + UUID.randomUUID();
        System.out.println("Device ID: " + deviceID + ", Type: " + DEVICE_TYPE);
        registJSON.put("device_id", deviceID);
        registJSON.put("device_type", DEVICE_TYPE);
        JSONArray attrs = new JSONArray();
        addAttribute(attrs, "LoadAverage", "double");
        registJSON.put("attributes", attrs);

        SubscribeThread snifferBroker = new SubscribeThread(deviceID);
        //String snifferURL = "tcp://" + SNIFFER_IP + ":1883";
        snifferBroker.connectInternalBroker(deviceID, SNIFFER_IP, BROKER_PORT);
        //snifferBroker.connect(snifferURL, deviceID);
        snifferBroker.subscribe(TOPIC_GET_LIST);
        snifferBroker.publish(registJSON.toString(), TOPIC_REGIST_DEVICE);

        //PUBLISH DATA
        //Runnable r = new PublishThread(deviceID, snifferBroker);
        //(new Thread(r, deviceID + "_Publisher")).start();
        
        String topicCPUUsage = "/" + deviceID + "/attrs/LoadAverage";
        //Random rand = new Random();

        while (true) {
            String cpu = "" + UUID.randomUUID();
            snifferBroker.publish(cpu, topicCPUUsage);
            
            Thread.sleep(SAMPLING_RATE);
        }
    }
    
    public static void addAttribute(JSONArray attrs, String objectID, String objectType) throws JSONException{
        JSONObject attributeJSON = new JSONObject();
        attributeJSON.put("object_id", objectID);
        attributeJSON.put("type", objectType);
        attrs.put(attributeJSON);
    }       
}
