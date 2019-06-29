package com.kai_jan_57.opendsbmobile.tableparser;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Untis extends Parser {

    private static final String TABLE_METADATA_CLASS = "mon_head";
    private static final String TABLE_TITLE_CLASS = "mon_title";
    private static final String TABLE_CLASS = "mon_list";

    Untis(File htmlFile) throws IOException {
        super(htmlFile);


        List<TextNode> textNodes = mDocument.getElementsByClass(TABLE_METADATA_CLASS).first().getElementsByTag("p").first().textNodes();
        for (TextNode textNode : textNodes) {
            mDocumentMetadata.add(textNode.text().trim());
        }

        for (Element element : mDocument.getElementsByClass(TABLE_TITLE_CLASS)) {
            mTableTitles.add(element.text().trim());
        }

        for (Element element : mDocument.getElementsByClass(TABLE_CLASS)) {
            List<String> columnHeaders = new ArrayList<>();
            List<List<String>> rows = new ArrayList<>();
            for (Element table : element.getElementsByTag("tr")) {
                if (columnHeaders.isEmpty()) {
                    Elements headers = table.getElementsByTag("th");
                    if (!headers.isEmpty()) {
                        for (Element header : headers) {
                            columnHeaders.add(header.text().trim());
                        }
                    }
                    continue;
                }

                List<String> rowContent = new ArrayList<>();
                for (Element row : table.getElementsByTag("td")) {
                    rowContent.add(row.text().trim());
                }
                rows.add(rowContent);
            }
            if (!columnHeaders.isEmpty()) {
                mColumnHeaders.add(columnHeaders);
            }
            if (!rows.isEmpty()) {
                mTableContent.add(rows);
            }
        }
    }

    @Override
    public String getEncoding() {
        return StandardCharsets.ISO_8859_1.name();
    }
}
