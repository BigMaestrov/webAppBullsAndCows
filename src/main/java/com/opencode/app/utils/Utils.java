package com.opencode.app.utils;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Класс с вспомогательными методами.
 *
 * @version 1.0
 * @author Vladimir Kizelbashev
 */
public class Utils {

    /**
     * Метод возвращает MD5 в виде строки из 32 шестнадцатеричных символов.
     * @param str Исходная строка
     * @return Строка MD5 на основе исходной
     * @throws Exception
     */
    public static String getMD5Hash(String str) throws Exception {
        MessageDigest msgDigest = MessageDigest.getInstance("MD5");
        msgDigest.reset();
        msgDigest.update(str.getBytes("UTF-8"));
        byte[] digest = msgDigest.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        String md5hash = bigInt.toString(16);
        while (md5hash.length() < 32) {
            md5hash = "0" + md5hash;
        }
        return md5hash;
    }
}
