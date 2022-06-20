/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package device;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class startsup one MQTT-RD example device publishing on {@value TOPIC_REGIST_DEVICE}, /attrs/LoadAverage and listening to {@value TOPIC_GET_LIST}.
 * @author eliseu
 */
public class TestDevice implements Runnable{
    private static final String DEVICE_TYPE = "nobroker";
    private static final String SNIFFER_IP = "192.168.0.2";
    private static final String BROKER_PORT = "1883";
    private static final String TOPIC_REGIST_DEVICE = "/registdevice";
    private static final String TOPIC_GET_LIST = "/getlist";

    private static final String DELETE_DEVICE_CHANNEL = "/sniffercommunication";

    private static final String DELETE_DEVICE_OPERATION = "deletedevice";

    //publish interval on example topic.
    private static final int SAMPLING_RATE = 1000;

    private String deviceID;
    private HashMap<String, Type> attributes = new HashMap<>();
    private SubscribeThread brokerConnection;

    public boolean ShouldRun = true;

    public TestDevice(){
        deviceID = "Device_" + UUID.randomUUID();
        System.out.println("Device ID: " + deviceID + ", Type: " + DEVICE_TYPE);
        attributes.put("LoadAverage",Double.class);
    }

    /**
     * This method creates this devices description. Starts up MQTT connection. Register this device and subscribe to /getList.
     * After that it starts to publish on ../attrs/LoadAverage in an interval of {@value SAMPLING_RATE} ms.
     * Also, entrypoint for compiled program "Multidevice.jar"
     * @throws JSONException On JSON IO error.
     */
    public void run () {
        try{
            //Assemble device self description JSON object:
            JSONObject registJSON = new JSONObject();

            registJSON.put("device_id", deviceID);
            registJSON.put("device_type", DEVICE_TYPE);

            JSONArray attrs = new JSONArray();
            attributes.entrySet().forEach(entry -> {
                try {
                    addAttribute(attrs, entry.getKey(), entry.getValue().getClass().getTypeName());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });
            registJSON.put("attributes", attrs);

            //Starts a thread that processes incoming message s on topic /getList
            brokerConnection = new SubscribeThread(deviceID);

            //String snifferURL = "tcp://" + SNIFFER_IP + ":1883";
            brokerConnection.connectInternalBroker(deviceID, SNIFFER_IP, BROKER_PORT); //connect as MQTT Client with deviceID to sniffer.
            //snifferBroker.connect(snifferURL, deviceID);
            brokerConnection.subscribe(TOPIC_GET_LIST);
            brokerConnection.publish(registJSON.toString(), TOPIC_REGIST_DEVICE); //send registration message to broker


            //PUBLISH DATA
            //Runnable r = new PublishThread(deviceID, snifferBroker);
            //(new Thread(r, deviceID + "_Publisher")).start();

            String topicCPUUsage = "/" + deviceID + "/attrs/LoadAverage";
            //Random rand = new Random();
            long counter = 0;
            while (true && ShouldRun) {
                String cpu = "" + UUID.randomUUID();
                brokerConnection.publish(cpu, topicCPUUsage);
                System.out.println("id: " + deviceID + " published message " + counter++);
                Thread.sleep(SAMPLING_RATE);
            }
        }
        catch (InterruptedException IEx){
            System.out.println("Device thread of device " + deviceID + " was interrupted.");
        }
        catch (JSONException jsEx){
            System.out.println("Device thread of device " + deviceID + " had JSON read/ write error");
        }
        catch (Exception e){
            System.out.println("Device thread of device " + deviceID + "exception: " + e.getMessage() + "\n" + e.getStackTrace());
        }
        finally {
            stop();
        }
    }

    public void stop() {
        System.out.println("Stopping thread of device " + deviceID + " !");
        try{
            brokerConnection.subscriptions.forEach(subscription -> brokerConnection.unsubscribe(subscription));
            JSONObject deviceDescription = new JSONObject();
            deviceDescription.put("device_id",deviceID);
            deviceDescription.put("sniffer_id","systec");

            JSONObject message = new JSONObject();
            message.put("operation", DELETE_DEVICE_OPERATION);
            message.put("object",deviceDescription);

            brokerConnection.publish(message.toString(),DELETE_DEVICE_CHANNEL);
            brokerConnection.disconnect();
        }
        catch (JSONException ex){
            System.out.println("Exception while creating device information. No gurantee that a valid delete message was sent.");
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
