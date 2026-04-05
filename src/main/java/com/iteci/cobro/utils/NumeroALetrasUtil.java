package com.iteci.cobro.utils;

import java.math.BigDecimal;
import java.text.Normalizer;

public class NumeroALetrasUtil {

    private static final String[] UNIDADES = {
        "", "uno", "dos", "tres", "cuatro", "cinco",
        "seis", "siete", "ocho", "nueve", "diez",
        "once", "doce", "trece", "catorce", "quince",
        "dieciseis", "diecisiete", "dieciocho", "diecinueve"
    };

    private static final String[] DECENAS = {
        "", "", "veinte", "treinta", "cuarenta", "cincuenta",
        "sesenta", "setenta", "ochenta", "noventa"
    };

    private static final String[] CENTENAS = {
        "", "cien", "doscientos", "trescientos", "cuatrocientos",
        "quinientos", "seiscientos", "setecientos", "ochocientos",
        "novecientos"
    };

    public static String convertirMontoEnLetras(BigDecimal monto) {
        if (monto == null) return "";

        long parteEntera = monto.longValue();
        int centavos = monto.remainder(BigDecimal.ONE)
                            .movePointRight(2)
                            .intValue();

        String letras = convertirNumero(parteEntera).trim();

        if (parteEntera == 1) {
            letras += " peso";
        } else {
            letras += " pesos";
        }

        letras += String.format(" %02d/100 M.N.", centavos);

        // Capitalize first letter
        return letras.substring(0, 1).toUpperCase() + letras.substring(1);
    }

    private static String convertirNumero(long n) {
        if (n < 20) {
            return UNIDADES[(int)n];
        } else if (n < 100) {
            int d = (int)n / 10;
            int r = (int)n % 10;
            if (n < 30) return "veinti" + convertirNumero(r);
            return DECENAS[d] + (r > 0 ? " y " + convertirNumero(r) : "");
        } else if (n < 1000) {
            int c = (int)n / 100;
            int r = (int)n % 100;
            if (n == 100) return "cien";
            return CENTENAS[c] + (r > 0 ? " " + convertirNumero(r) : "");
        } else if (n < 1_000_000) {
            long miles = n / 1000;
            long r = n % 1000;
            if (miles == 1)
                return "mil" + (r > 0 ? " " + convertirNumero(r) : "");
            return convertirNumero(miles) + " mil" + (r > 0 ? " " + convertirNumero(r) : "");
        } else {
            long millones = n / 1_000_000;
            long r = n % 1_000_000;
            if (millones == 1)
                return "un millón" + (r > 0 ? " " + convertirNumero(r) : "");
            return convertirNumero(millones) + " millones" + (r > 0 ? " " + convertirNumero(r) : "");
        }
    }

    public static String changeWordString(String name){
        if (name == null || name.length() == 0) return "X";
         // 1. Remove accents first
        name = Normalizer.normalize(name, Normalizer.Form.NFD);
        name = name.replaceAll("\\p{M}", ""); // removes diacritical marks

        String[] parts = name.trim().split("\\s+"); // handles multiple spaces safely
        String resultaString = "";
        for(String names : parts){
            resultaString += names.toUpperCase().charAt(0) 
                + names.substring(1).toLowerCase() 
                + " ";
        }
        resultaString = resultaString.trim();
        return resultaString;
    }
}
