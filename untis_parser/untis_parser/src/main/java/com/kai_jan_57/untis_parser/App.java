package com.kai_jan_57.untis_parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.kai_jan_57.untis_parser.tableparser.Untis;

/**
 * Hello world!
 *
 */
public class App {
    private static final int INDENT = 2;

    public static void main(String[] args) {

        try {
            File file = new File("/media/kai_jan_57/ECF45251F4521E60/Users/Kai_Jan_57/Documents/Apps/OpenDSBmobile/untis_parser/Untis 2018 Vertretungsplan.htm");
            Untis document = new Untis(file);

            // initialize
            for (int i = 0; i < document.getTableCount(); i++) {
                List<List<String>> tableContent = new ArrayList<>();
                tableContent.add(document.getTableHeaders(i));
                for (int j = 0; j < document.getRowCount(i); j++) {
                    tableContent.add(document.getRow(i, j));
                }
                List<Integer> maxWidth = new ArrayList<>();
                for (int row = 0; row < tableContent.size(); row++) {
                    for (int column = 0; column < tableContent.get(row).size(); column++) {
                        if (maxWidth.size() - 1 < column) {
                            maxWidth.add(tableContent.get(row).get(column).length() + INDENT);
                        } else {
                            maxWidth.set(column, Math.max(maxWidth.get(column),
                                    tableContent.get(row).get(column).length() + INDENT));
                        }
                    }
                }

                int fullWidth = 1;
                for (int width : maxWidth) {
                    fullWidth += width + 1;
                }

                printCentered(document.getTitle(), fullWidth);
                printCentered(repeat(document.getTitle().length(), ' '), fullWidth);
                System.out.println();
                for (String information : document.getTableMetadata()) {
                    printRightBound(information, fullWidth);
                }

                System.out.println();
                printCentered(document.getTableTitle(i), fullWidth);
                printCentered(repeat(document.getTableTitle(i).length(), '-'), fullWidth);

                // print
                for (int row = 0; row < tableContent.size(); row++) {
                    for (int column = 0; column < maxWidth.size(); column++) {
                        System.out.print('|' + repeat(maxWidth.get(column), row == 1 ? '=' : '-'));
                    }
                    System.out.println('|');
                    for (int column = 0; column < maxWidth.size(); column++) {
                        if (column < tableContent.get(row).size()) {
                            String field = tableContent.get(row).get(column);
                            field = repeat((maxWidth.get(column) - field.length()) / 2, ' ') + field;
                            System.out.print('|' + field);
                            System.out.print(repeat(maxWidth.get(column) - field.length(), ' '));
                        } else {
                            System.out.print('|' + repeat(maxWidth.get(column), ' '));
                        }
                    }
                    System.out.println('|');
                }
                for (int column = 0; column < maxWidth.size(); column++) {
                    System.out.print('|' + repeat(maxWidth.get(column), '-'));
                }
                System.out.println('|');
            }
        } catch (IOException ioe) {
            System.out.println(ioe.getStackTrace());
        }
    }

    public static void printRightBound(String text, int lineWidth) {
        System.out.println(repeat(lineWidth - text.length(), ' ') + text);
    }

    public static void printCentered(String text, int lineWidth) {
        System.out.println(repeat((lineWidth - text.length())/2, ' ') + text);
    }

    public static String repeat(int count, char character) {
        char[] buffer = new char[count];
        Arrays.fill(buffer, character);
        return new String(buffer);
    }
}
