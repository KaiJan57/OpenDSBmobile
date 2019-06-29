package com.kai_jan_57.untis_parser.tableparser;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Untis extends Parser {

    private static final String TABLE_METADATA_CLASS = "mon_head";
    private static final String TABLE_TITLE_CLASS = "mon_title";
    private static final String TABLE_CLASS = "mon_list";

    public Untis(File htmlFile) throws IOException {
        super(htmlFile);
    }

    @Override
    public String getEncoding() {
        return "windows-1252";
    }

    @Override
    public List<String> getTableMetadata() {
        List<TextNode> textNodes = mDocument.getElementsByClass(TABLE_METADATA_CLASS).first().getElementsByTag("p").first().textNodes();
        List<String> result = new ArrayList<>();
        for (TextNode textNode : textNodes) {
            result.add(textNode.text().trim());
        }
        return result;
    }

    @Override
    public int getTableCount() {
        return mDocument.getElementsByClass(TABLE_CLASS).size();
    }

    @Override
    public String getTableTitle(int tableIndex) {
        return mDocument.getElementsByClass(TABLE_TITLE_CLASS).get(tableIndex).text().trim();
    }

    private Element getTableElement(int tableIndex) {
        return mDocument.getElementsByClass(TABLE_CLASS).get(tableIndex);
    }

    @Override
    public List<String> getTableHeaders(int tableIndex) {
        List<String> result = new ArrayList<>();
        for (Element element : getTableElement(tableIndex).getElementsByTag("tr")) {
            Elements headers = element.getElementsByTag("th");
            if (!headers.isEmpty()) {
                for (Element header : headers) {
                    result.add(header.text().trim());
                }
                break;
            }
        }
        return result;
    }

    @Override
    public int getRowCount(int tableIndex) {
        return getTableElement(tableIndex).getElementsByTag("tr").size() - 1;
    }

    @Override
    public List<String> getRow(int tableIndex, int rowIndex) {
        List<String> result = new ArrayList<>();
        for (Element column : getTableElement(tableIndex).getElementsByTag("tr").get(rowIndex + 1).getElementsByTag("td")) {
            result.add(column.text().trim());
        }
        return result;
    }
}
