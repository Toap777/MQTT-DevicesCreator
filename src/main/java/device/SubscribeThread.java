/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package device;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mqtt.GenericClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class provides methods to process incoming mqtt messages.
 * @author eliseu
 */
public class SubscribeThread extends GenericClient{
    //Expected topic to querie resource collection
    private static final String TOPIC_GET_LIST = "/getlist";
    //sniffer name for this test scenario
    private static final String SNIFFER_ID = "systec";
    //In this scenario a device subscribes to all "LoadAverage" attributes of all available other devices in the network. This a list of already subscribed devices by id.
    List<String> subscriptions = new LinkedList<>();

    String deviceID;

    /**
     * Constructor of class
     * @param deviceID identifier used in MQTT connection
     */
    public SubscribeThread(String deviceID) {
        this.deviceID = deviceID;
    }

    /**
     * Processes incoming mqtt messages on subscribed topics. The clients can only process messages containing resourceList transmitted over topic TOPIC_GET_LIST in this scenario.
     * @param message message received MQTT message
     * @param topic the topic the message was published to
     */
    @Override
    public void processMessage(String message, String topic) {
        try {
            //
            if (topic.equals(TOPIC_GET_LIST)) {
                checkList(new JSONObject(message));
            }

        } catch (JSONException ex) {
            Logger.getLogger(SubscribeThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * On lost connection do nothing.
     */
    @Override
    public void lostConnection() {}

    /**
     * Checks the passed resourceCollection for new devices and let this device subscribe to loadAverageTopic of new devices.
     * Logs errors to console.
     * @param resourceCollection resourceCollection
     */
    private void checkList(JSONObject resourceCollection) {
        try {
            JSONArray sniffers = resourceCollection.getJSONArray("sniffers");
            //Loop through all sniffers
            for (int i = 0; i < sniffers.length(); i++) {
                JSONObject sniffer = sniffers.getJSONObject(i);

                if (sniffer.getString("sniffer_id").equals(SNIFFER_ID)) {
                    JSONArray devices = sniffer.getJSONArray("devices");
                    //loop through each device
                    for (int j = 0; j < devices.length(); j++) {
                        JSONObject device = devices.getJSONObject(j);
                        String newDeviceID = device.getString("device_id");
                        //if the current device is not this device and its not already subscribed:
                        if((!newDeviceID.equals(deviceID)) && (!subscriptions.contains(newDeviceID))){
                            subscriptions.add(newDeviceID);
                            String topic = "/" + newDeviceID + "/attrs/LoadAverage";
                            subscribe(topic); //subscribe to topic with new mwtt client.
                        }
                    }
                    
                }

            }
        } catch (JSONException ex) {
            Logger.getLogger(SubscribeThread.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
