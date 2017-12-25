package namcap.dnscryptAndroidclient;

import android.support.annotation.Nullable;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * Created on 3/14/2017.
 */

class CSVReader {

    static ArrayList<String> getFieldName(final String filename) throws FileNotFoundException {
        ArrayList<String> fields=new ArrayList<>();
        Scanner scanner=null;

        try {
            scanner = new Scanner(new FileReader(filename));
            if (scanner.hasNextLine()) {
                Collections.addAll(fields, split(scanner.nextLine()));
            }
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        return fields;
    }

    static ArrayList<String[]> getData(final String filename, int skipLines,@Nullable final ArrayList<Integer> filter) throws FileNotFoundException {
        ArrayList<String[]> ret=new ArrayList<>();
        Scanner scanner=null;
        String[] row;
        ArrayList<String> rowBuilder=new ArrayList<>();
        try {
            scanner=new Scanner(new FileReader(filename));
            while (0<skipLines) {
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }
                else {
                    break;
                }
                --skipLines;
            }
            if (filter==null) {
                while (scanner.hasNextLine()) {
                    rowBuilder.clear();
                    row=split(scanner.nextLine());
                    for (String i : row) {
                        rowBuilder.add(i.trim());
                    }
                    ret.add(rowBuilder.toArray(new String[0]));
                }
            }
            else {
                while (scanner.hasNextLine()) {
                    rowBuilder.clear();
                    row=split(scanner.nextLine());
                    for (int i : filter) {
                        if (0 <= i && i < row.length) {
                            rowBuilder.add(row[i].trim());
                        }
                    }
                    ret.add(rowBuilder.toArray(new String[0]));
                }
            }
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        return ret;
    }

    private static String[] split(final String str) {
        ArrayList<String> tokens=new ArrayList<>();
        boolean quoted=false;

        int k=0,i;
        for (i=0;i<str.length();++i) {
            switch (str.charAt(i)) {
                case '"':
                    quoted=(!quoted);
                    break;
                case ',':
                    if (! quoted) {
                        tokens.add(str.substring(k,i));
                        k=i+1;
                    }
                    break;
            }
        }
        if (k<str.length()) {
            tokens.add(str.substring(k,i));
        }
        else {
            tokens.add("");
        }

        return tokens.toArray(new String[0]);
    }

}
