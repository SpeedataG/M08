
//package Gpio;
package com.cnnk.IdentifyCard;

import android.util.Log;

public class Gpio
{
	static public native int GPIO_open( String sPort);  
	static public native int GPIO_close(int fd);
	static public native int GPIO_pullHigh(int fd, int gpio);
	static public native int GPIO_pullLow(int fd, int gpio);
	
	
    static {
        try {
        	System.loadLibrary("gpiojni");
        } catch (UnsatisfiedLinkError e) {
            Log.d("GtTermb", "guoteng termb library not found!");
        }
    }
}
