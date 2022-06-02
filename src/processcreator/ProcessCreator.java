/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processcreator;

import multidevice.TestDevice;
import java.io.File;
import java.io.IOException;

/**
 * Device creation tool. Runs multiple instances of device java program.
 * @author eliseu
 */
public class ProcessCreator {
    //amount of devices to create
    private static final int N_DEVICES = 40;
    //rate between creation of each device in ms.
    private static final int SAMPLING_RATE = 5000;

    /**
     * Starts up {@value N_DEVICES} devices in an interval of {@value SAMPLING_RATE} between each device.
     * @param args the command line arguments
     * @throws java.lang.InterruptedException ON system interrupt
     * @throws java.io.IOException On error reading / writin specified program file.
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        for (int i = 0; i < N_DEVICES; i++) {
            //start jar compiled program by execute command in Runtime.
            TestDevice t = new TestDevice();
            t.run();
        }
        System.out.println("End.");
    }
}
