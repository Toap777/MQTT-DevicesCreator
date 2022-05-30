/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multidevice;

import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class startsup one MQTT-RD example device publishing on {@value TOPIC_REGIST_DEVICE}, /attrs/LoadAverage and listening to {@value TOPIC_GET_LIST}.
 * @author eliseu
 */
public class MachineMonitor {    
    private static final String DEVICE_TYPE = "nobroker";
    private static final String SNIFFER_IP = "192.168.0.101";
    private static final String BROKER_PORT = "1883";
    private static final String TOPIC_REGIST_DEVICE = "/registdevice";   
    private static final String TOPIC_GET_LIST = "/getlist";
    //publish interval on example topic.
    private static final int SAMPLING_RATE = 1000;
    
    /**
     * This method creates this devices description. Starts up MQTT connection. Register this device and subscribe to /getList.
     * After that it starts to publish on ../attrs/LoadAverage in an interval of {@value SAMPLING_RATE} ms.
     * Also, entrypoint for compiled program "Multidevice.jar"
     * @param args the command line arguments
     * @throws org.json.JSONException On JSON IO error.
     * @throws java.lang.InterruptedException On finishing the program with interrupt statement e.g. strg + x
     */
    public static void main(String[] args) throws JSONException, InterruptedException {

        //Assemble device self description JSON object:
        JSONObject registJSON = new JSONObject();
        //String deviceID = args[0];
        String deviceID = "Device_" + UUID.randomUUID();
        System.out.println("Device ID: " + deviceID + ", Type: " + DEVICE_TYPE);
        registJSON.put("device_id", deviceID);
        registJSON.put("device_type", DEVICE_TYPE);
        JSONArray attrs = new JSONArray();
        addAttribute(attrs, "LoadAverage", "double");
        registJSON.put("attributes", attrs);

        //Starts a thread that processes incoming message s on topic /getList
        SubscribeThread snifferBroker = new SubscribeThread(deviceID);
        //String snifferURL = "tcp://" + SNIFFER_IP + ":1883";
        snifferBroker.connectInternalBroker(deviceID, SNIFFER_IP, BROKER_PORT); //connect as MQTT Client with deviceID to sniffer.
        //snifferBroker.connect(snifferURL, deviceID);
        snifferBroker.subscribe(TOPIC_GET_LIST);
        snifferBroker.publish(registJSON.toString(), TOPIC_REGIST_DEVICE); //send registration message to broker

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

    /**
     * This method adds an attribute to IOT-device's self-description.
     * @param attrs device's attribute list
     * @param objectID attribute id
     * @param objectType attribute data type
     * @throws JSONException thrown on error while assembling attribute json representation
     */
    public static void addAttribute(JSONArray attrs, String objectID, String objectType) throws JSONException{
        JSONObject attributeJSON = new JSONObject();
        attributeJSON.put("object_id", objectID);
        attributeJSON.put("type", objectType);
        attrs.put(attributeJSON);
    }
}
