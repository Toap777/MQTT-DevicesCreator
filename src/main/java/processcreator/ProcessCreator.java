/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processcreator;

import device.TestDevice;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Device creation tool. Runs multiple instances of device java program.
 * @author eliseu
 */
public class ProcessCreator {
    //amount of devices to create
    private static final int N_DEVICES = 20;
    //rate between creation of each device in ms.
    private static final int SAMPLING_RATE = 10000;

    private static List<TestDevice> devices = new ArrayList<>();

    /**
     * Starts up {@value N_DEVICES} devices in an interval of {@value SAMPLING_RATE} between each device.
     * @param args the command line arguments
     * @throws java.lang.InterruptedException ON system interrupt
     */
    public static void main(String[] args) throws InterruptedException, JSONException {
        for (int i = 0; i < N_DEVICES; i++) {
            System.out.println("Device [" + i + "]");
            //start jar compiled program by execute command in Runtime.
            TestDevice t = new TestDevice();
            Thread deviceThread = new Thread(t);
            devices.add(t);
            deviceThread.start();
            Thread.sleep(SAMPLING_RATE);
        }
        for (TestDevice device : devices) {
            device.ShouldRun = false;
        }
        System.out.println("End.");
    }
}
