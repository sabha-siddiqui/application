package com.easyfitness.utils;

import com.easyfitness.enums.Unit;
import com.easyfitness.enums.WeightUnit;

public class UnitConverter {

    public UnitConverter() {
    }

    static public float weightConverter(float pWeight, WeightUnit pUnitIn, WeightUnit pUnitOut) {
        return weightConverter(pWeight, pUnitIn.toUnit(), pUnitOut.toUnit());
    }

    static public float weightConverter(float pWeight, Unit pUnitIn, Unit pUnitOut) {
        switch (pUnitIn) {
            case KG:
                switch (pUnitOut) {
                    case LBS:
                        return KgtoLbs(pWeight);
                    case STONES:
                        return KgtoStones(pWeight);
                    case KG:
                    default:
                        return pWeight;
                }
            case LBS:
                switch (pUnitOut) {
                    case KG:
                        return LbstoKg(pWeight);
                    case LBS:
                    default:
                        return pWeight;
                }
            case STONES:
                switch (pUnitOut) {
                    case KG:
                        return StonestoKg(pWeight);
                    case STONES:
                    default:
                        return pWeight;
                }
            default:
                return pWeight;
        }
    }

    static public float KgtoLbs(float pKg) {
        return pKg / (float) 0.45359237;
    }
    static public float KgtoStones(float pKg) {
        return pKg / (float) 6.35029;
    }

    static public float LbstoKg(float pLbs) {
        return pLbs * (float) 0.45359237;
    }
    static public float LbstoStones(float pLbs) {
        return pLbs / (float) 14;
    }

    static public float StonestoKg(float pSt) {
        return pSt * (float) 6.35029;
    }
    static public float StonestoLbs(float pSt) {
        return pSt * (float) 14;
    }

    static public float KmToMiles(float pKm) { return pKm * (float) 1.609344; }
    static public float MilesToKm(float pMiles) { return pMiles / (float) 1.609344; }

    static public float sizeConverter(float pSize, Unit pUnitIn, Unit pUnitOut) {
        switch (pUnitIn) {
            case CM:
                switch (pUnitOut) {
                    case INCH:
                        return CmtoInch(pSize);
                    default:
                        return pSize;
                }
            case INCH:
                switch (pUnitOut) {
                    case CM:
                        return InchToCm(pSize);
                    default:
                        return pSize;
                }
            default:
                return pSize;
        }
    }

    private static float InchToCm(float pSize) {
        return pSize * (float)2.54;
    }

    private static float CmtoInch(float pSize) {
        return pSize / (float)2.54;
    }

    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        if (hours > 0) {
            finalTimerString = hours + ":";
        }
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        return finalTimerString;
    }

    public int getProgressPercentage(long currentDuration, long totalDuration) {
        double percentage;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);
        percentage = (((double) currentSeconds) / totalSeconds) * 100;
        return (int) percentage;
    }
    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = totalDuration / 1000;
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);
        return currentDuration * 1000;
    }
}
