package sh.okx.webdeals.util;

public class NumberUtil {
    public static int roundUp(int num, int toTheNearest) {
        return ((num + (toTheNearest - 1)) / toTheNearest) * toTheNearest;
    }
}
