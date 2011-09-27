/**
 * 	This file is part of Kayak.
 *
 *	Kayak is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Kayak is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public License
 *	along with Kayak.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.github.kayak.core;

/**
 * Utility class that provides the Kayak classes with methods to convert
 * hex strings and byte arrays to the opposite.
 * @author Jan-Niklas Meier <dschanoeh@googlemail.com>
 *
 */
public class Util {

    static final String HEXES = "0123456789ABCDEF";
    static final String[] binary = {"0000","0001","0010","0011","0100","0101","0110","0111","1000","1001","1010","1011","1100","1101","1110","1111"};

    public static String byteToHexString(byte b) {
        StringBuilder sb = new StringBuilder(2);
        sb.append(HEXES.charAt((b & 0xF0) >> 4));
        sb.append(HEXES.charAt((b & 0x0F)));
        return sb.toString();
    }

    /**
     * Convert a hex string to a byte array.
     */
    public static byte[] hexStringToByteArray(String s) {
        if ((s.length() % 2) != 0) {
            s = "0" + s;
        }
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Returns a hex string representation of a byte array.
     * @param raw The byte array
     * @param spaces If true the bytes will be space separated
     * @return
     */
    public static String byteArrayToHexString(byte[] raw, boolean spaces) {
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
            if(spaces)
                hex.append( ' ' );
        }

        if(spaces)
            hex.setLength(hex.length()-1);

        return hex.toString();
    }

    public static String hexStringToBinaryString(String s) {
        StringBuilder sb = new StringBuilder();

        for(int i=0;i<s.length();i++) {
            char c = s.charAt(i);
            int index=0;
            if(c >= '0' && c <= '9') {
                index = c - '0';
            } else if(c >= 'a' && c <= 'f') {
                index = c - 'a' + 10;
            } else if(c >= 'A' && c <= 'F') {
                index = c - 'A' + 10;
            } else {
                return null;
            }
            sb.append(binary[index]);
        }

        return sb.toString();
    }

    public static String intToHexString(int i) {
        StringBuilder sb = new StringBuilder(10);
        while(i > 0) {
            int rest = i % 16;

            sb.append(HEXES.charAt(rest));
            i /= 16;
        }

        sb.reverse();
        return sb.toString();
    }

    public static int hexStringToInt(String s) {
        int val=0;
        int weight=1;
        for(int i=s.length()-1;i>=0;i--) {
            val += weight * Character.digit(s.charAt(i), 16);
            weight *= 16;
        }
        return val;
    }
}
