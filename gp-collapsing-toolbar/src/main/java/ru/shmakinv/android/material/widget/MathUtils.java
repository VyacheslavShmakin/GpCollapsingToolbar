package ru.shmakinv.android.material.widget;

/**
 * MathUtils
 *
 * @author: Vyacheslav Shmakin
 * @version: 02.02.2016
 */
class MathUtils {

    static int constrain(int amount, int low, int high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

    static float constrain(float amount, float low, float high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

}
