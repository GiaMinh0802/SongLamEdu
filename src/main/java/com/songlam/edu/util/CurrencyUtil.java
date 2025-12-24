package com.songlam.edu.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class CurrencyUtil {

    public static String formatBigDecimal(BigDecimal number) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');

        DecimalFormat df = new DecimalFormat("#,###", symbols);
        df.setMaximumFractionDigits(0);

        return df.format(number) + "đ";
    }

    public static String convertText(BigDecimal amount) {
        if (amount == null) return "";

        long value = amount.longValue();

        if (value == 0) return "không đồng";

        String[] scale = {"", " nghìn", " triệu", " tỷ"};

        StringBuilder result = new StringBuilder();
        int scaleIndex = 0;

        while (value > 0) {
            int triple = (int) (value % 1000);

            if (triple != 0) {
                String part = readTriple(triple);
                result.insert(0, part + scale[scaleIndex] + " ");
            }

            value /= 1000;
            scaleIndex++;
        }

        String finalResult = result.toString().trim();
        finalResult = finalResult.substring(0, 1).toUpperCase() + finalResult.substring(1) + " đồng";

        return finalResult.replaceAll("\\s+", " ");
    }

    private static final String[] units = {
            "", "một", "hai", "ba", "bốn", "năm",
            "sáu", "bảy", "tám", "chín"
    };

    private static String readTriple(int number) {
        int hundred = number / 100;
        int tenUnit = number % 100;
        int ten = tenUnit / 10;
        int unit = tenUnit % 10;

        StringBuilder sb = new StringBuilder();

        if (hundred > 0) {
            sb.append(units[hundred]).append(" trăm");
            if (ten == 0 && unit > 0) {
                sb.append(" lẻ");
            }
        }

        if (ten > 1) {
            sb.append(" ").append(units[ten]).append(" mươi");
            if (unit == 1) {
                sb.append(" mốt");
            } else if (unit == 5) {
                sb.append(" lăm");
            } else if (unit > 1) {
                sb.append(" ").append(units[unit]);
            }
        } else if (ten == 1) {
            sb.append(" mười");
            if (unit == 5) {
                sb.append(" lăm");
            } else if (unit > 0) {
                sb.append(" ").append(units[unit]);
            }
        } else if (ten == 0 && unit > 0 && hundred == 0) {
            sb.append(units[unit]);
        } else if (ten == 0 && unit > 0) {
            sb.append(" ").append(units[unit]);
        }

        return sb.toString().trim();
    }

}
