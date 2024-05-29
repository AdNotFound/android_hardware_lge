package org.lineageos.settings.device.dac.utils;

import android.media.AudioSystem;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;

import vendor.lge.hardware.audio.dac.control.V2_0.Feature;
import vendor.lge.hardware.audio.dac.control.V2_0.FeatureStates;
import vendor.lge.hardware.audio.dac.control.V2_0.IDacControl;
import org.lineageos.hardware.util.FileUtils;
import java.util.ArrayList;


public class QuadDAC {

    private static final String TAG = "QuadDAC";

    private QuadDAC() {}

    private static IDacControl dac;

    private static ArrayList<Integer> dac_features;

    public static boolean dac_service_available = false;

    public static void initialize() throws RemoteException {
        dac = IDacControl.getService(true);
        dac_features = dac.getSupportedFeatures();
        dac_service_available = true;
    }

    public static void enable(IDacControl dac)
    {
        try {
            dac.setHifiDacState(true);
            
            int digital_filter = getDigitalFilter(dac);
            int sound_preset = getSoundPreset(dac);
            int left_balance = getLeftBalance(dac);
            int right_balance = getRightBalance(dac);
            int mode = getDACMode(dac);
            int avc_vol = getAVCVolume(dac);

            setDACMode(dac, mode);
            setLeftBalance(dac, left_balance);
            setRightBalance(dac, right_balance);
            setDigitalFilter(dac, digital_filter);
            setSoundPreset(dac, sound_preset);
            setAVCVolume(dac, avc_vol);

             // Sound presets are disabled on the open-source audio HAL.
            if(dac_features.contains(Feature.CustomFilter)) {
                int custom_filter_shape = getCustomFilterShape();
                int custom_filter_symmetry = getCustomFilterSymmetry();
                int[] custom_filter_coefficients = new int[14];
                int i;
                for(i = 0; i < 14; i++) {
                    custom_filter_coefficients[i] = getCustomFilterCoeff(i);
                }
                setCustomFilterShape(custom_filter_shape);
                setCustomFilterSymmetry(custom_filter_symmetry);
                for(i = 0; i < 14; i++) {
                    setCustomFilterCoeff(i, custom_filter_coefficients[i]);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void disable(IDacControl dac) throws RemoteException
    {
        dac.setHifiDacState(false);
    }

    public static ArrayList<Integer> getSupportedFeatures() {
        return dac_features;
    }

    public static FeatureStates getSupportedFeatureValues(int feature) throws RemoteException
    {
        return dac.getSupportedFeatureValues(feature);
    }
    
    public static void setDACMode(IDacControl dac, int mode) throws RemoteException
    {
        dac.setFeatureValue(Feature.HifiMode, mode);
    }

    public static int getDACMode(IDacControl dac) throws RemoteException
    {
        return dac.getFeatureValue(Feature.HifiMode);
    }

    public static void setAVCVolume(IDacControl dac, int avc_volume) throws RemoteException
    {
        dac.setFeatureValue(Feature.AVCVolume, avc_volume);
    }

    public static int getAVCVolume(IDacControl dac) throws RemoteException
    {
        return dac.getFeatureValue(Feature.AVCVolume);
    }

    public static void setDigitalFilter(IDacControl dac, int filter) throws RemoteException
    {
        dac.setFeatureValue(Feature.DigitalFilter, filter);
        if(dac_features.contains(Feature.CustomFilter) && filter == 3) {/* Custom filter */
            /*
            * If it's a custom filter, we need to apply its settings. Any of the functions
            * below should suffice since it'll load all settings from memory by parsing its
            * data.
            */
            setCustomFilterShape(getCustomFilterShape());
        }
    }

    public static int getDigitalFilter(IDacControl dac) throws RemoteException
    {
        return dac.getFeatureValue(Feature.DigitalFilter);
    }

    public static void setSoundPreset(IDacControl dac, int preset) throws RemoteException
    {
        if(dac_features.contains(Feature.SoundPreset))
            dac.setFeatureValue(Feature.SoundPreset, preset);
    }

    public static int getSoundPreset(IDacControl dac) throws RemoteException
    {
        if(!dac_features.contains(Feature.SoundPreset))
            return 0;
        return dac.getFeatureValue(Feature.SoundPreset);
    }

    public static void setLeftBalance(IDacControl dac, int balance) throws RemoteException
    {
        dac.setFeatureValue(Feature.BalanceLeft, balance);
    }

    public static int getLeftBalance(IDacControl dac) throws RemoteException
    {
        return dac.getFeatureValue(Feature.BalanceLeft);
    }

    public static void setRightBalance(IDacControl dac, int balance) throws RemoteException
    {
        dac.setFeatureValue(Feature.BalanceRight, balance);
    }

    public static int getRightBalance(IDacControl dac) throws RemoteException
    {
        return dac.getFeatureValue(Feature.BalanceRight);
    }

    public static boolean isEnabled(IDacControl dac) throws RemoteException
    {
        return dac.getHifiDacState();
    }

    public static boolean setCustomFilterShape(int shape) throws RemoteException
    {
        return dac.setCustomFilterShape(shape);
    }

    public static int getCustomFilterShape() throws RemoteException
    {
        return dac.getCustomFilterShape();
    }

    public static boolean setCustomFilterSymmetry(int symmetry) throws RemoteException
    {
        return dac.setCustomFilterSymmetry(symmetry);
    }

    public static int getCustomFilterSymmetry() throws RemoteException
    {
        return dac.getCustomFilterSymmetry();
    }

    public static boolean setCustomFilterCoeff(int coeffIndex, int coeff_val) throws RemoteException
    {
        return dac.setCustomFilterCoeff(coeffIndex, coeff_val);
    }

    public static int getCustomFilterCoeff(int coeffIndex) throws RemoteException
    {
        return dac.getCustomFilterCoeff(coeffIndex);
    }

    public static void resetCustomFilterCoeffs() throws RemoteException
    {
        dac.resetCustomFilterCoeffs();
    }
}
