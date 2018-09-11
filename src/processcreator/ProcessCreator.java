/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processcreator;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author eliseu
 */
public class ProcessCreator {
    private static final int N_DEVICES = 20;
    private static final int SAMPLING_RATE = 10000;

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        // TODO code application logic here
        
        String path = new File("").getAbsolutePath();
        String execLine = "java -jar " + path + "/MultiDevice/dist/MultiDevice.jar ";
        
        for (int i = 0; i < N_DEVICES; i++) {
            Runtime.getRuntime().exec(execLine);
            
            System.out.println("Device number: " + i);
            Thread.sleep(SAMPLING_RATE);
        }
        System.out.println("End.");
    }
    
}
