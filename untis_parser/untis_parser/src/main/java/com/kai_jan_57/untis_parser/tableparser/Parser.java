package com.kai_jan_57.untis_parser.tableparser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class Parser {

    Document mDocument;

    public Parser(File htmlFile) throws IOException {
        mDocument = Jsoup.parse(htmlFile, getEncoding(), "");
    }

    public String getEncoding() {
        return StandardCharsets.UTF_8.name();
    }

    public String getTitle() {
        return mDocument.title();
    }

    public abstract List<String> getTableMetadata();

    public abstract int getTableCount();

    public abstract String getTableTitle(int tableIndex);

    public abstract List<String> getTableHeaders(int tableIndex);

    public abstract int getRowCount(int tableIndex);

    public abstract List<String> getRow(int tableIndex, int rowIndex);

}
