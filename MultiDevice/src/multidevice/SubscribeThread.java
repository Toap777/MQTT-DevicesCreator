/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multidevice;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import multidevice.mqtt.GenericClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author eliseu
 */
public class SubscribeThread extends GenericClient{
    private static final String TOPIC_GET_LIST = "/getlist";
    private static final String SNIFFER_ID = "systec";
    List<String> subscritions = new LinkedList<>(); 
    String deviceID;
    
    public SubscribeThread(String deviceID) {
        this.deviceID = deviceID;
    }
    
    @Override
    public void processMessage(String message, String topic) {
        try {
            if (topic.equals(TOPIC_GET_LIST)) {
                checkList(new JSONObject(message));
            }

        } catch (JSONException ex) {
            Logger.getLogger(SubscribeThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void lostConnection() {}
    
    private void checkList(JSONObject list) {
        try {
            JSONArray sniffers = list.getJSONArray("sniffers");

            for (int i = 0; i < sniffers.length(); i++) {
                JSONObject sniffer = sniffers.getJSONObject(i);

                if (sniffer.getString("sniffer_id").equals(SNIFFER_ID)) {
                    JSONArray devices = sniffer.getJSONArray("devices");
                    
                    for (int j = 0; j < devices.length(); j++) {
                        JSONObject device = devices.getJSONObject(j);
                        String newDeviceID = device.getString("device_id");
                        
                        if((!newDeviceID.equals(deviceID)) && (!subscritions.contains(newDeviceID))){
                            subscritions.add(newDeviceID);
                            String topic = "/" + newDeviceID + "/attrs/LoadAverage";
                            subscribe(topic);
                        }
                    }
                    
                }

            }
        } catch (JSONException ex) {
            Logger.getLogger(SubscribeThread.class.getName()).log(Level.SEVERE, null, ex);
        }

    }    
    
}
