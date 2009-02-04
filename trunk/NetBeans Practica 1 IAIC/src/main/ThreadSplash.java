/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package main;

import vista.Splash;

public class ThreadSplash extends Thread {
        public ThreadSplash() {

        }
        public void run() {

            Splash sp = new Splash();
            sp.setVisible(true);

            for(int i = 0;i<9;i++){

                try {
                     ThreadSplash.sleep(500);
                }
                catch(InterruptedException e) {
                }

                sp.avanza();
            }
            
            /*sp.setVisible(false);
            sp.setVisible(true);

            try {
                 ThreadSplash.sleep(4000);
            }
            catch(InterruptedException e) {
            }

            //vista.getFrame().setVisible(true);
            //vista.getFrame().setAlwaysOnTop(true);
            */sp.setVisible(false);



            //sp.setVisible(false);*/
        }
    }