package com.github.sszuev.ontdot.renderers;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Objects;

/**
 * Created by @ssz on 15.01.2022.
 */
abstract class BaseDOTRenderer implements Closeable {

    protected final Writer wr;

    protected BaseDOTRenderer(Writer wr) {
        this.wr = Objects.requireNonNull(wr);
    }

    protected void write(String s) {
        try {
            wr.write(s);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected void beginLinkDetails() {
        write("[");
    }

    protected void endLinkDetails() {
        write("]");
    }

    protected void writeSemicolon() {
        write(";");
    }

    protected void writeComma() {
        write(",");
    }

    protected void writeDoubleQuotedText(String txt) {
        write("\"");
        write(txt);
        write("\"");
    }

    protected void beginDetailsLabel() {
        write("label=<");
    }

    protected void endDetailsLabel() {
        write(">");
    }

    protected void writeNewLine() {
        write("\n");
    }

    protected void writeTab(int n) {
        for (int i = 0; i < n; i++) {
            writeTab();
        }
    }

    protected void writeTab() {
        write(" ");
    }

    protected void beginTable(int tab) {
        writeTab(tab);
        write("<table border='0' cellborder='1' cellspacing='0'>");
        writeNewLine();
    }

    protected void endTable(int tab) {
        endTag("table", tab);
    }

    protected void beginTag(String tag, int tab) {
        beginUnclosedTag(tag, tab);
        write(">");
        writeNewLine();
    }

    protected void beginUnclosedTag(String tag, int tab) {
        writeTab(tab);
        write("<");
        write(tag);
    }

    protected void endTag(String tag, int tab) {
        writeTab(tab);
        write("</");
        write(tag);
        write(">");
        writeNewLine();
    }

    protected void writeTableHeader(int tab, String header, String headerBackgroundColor, int colSpan) {
        writeTab(tab);
        write("<th port=\"header\">\n");

        beginUnclosedTag("td", tab + 1);
        if (colSpan > 0) {
            write(" colspan=");
            writeDoubleQuotedText(String.valueOf(colSpan));
        }
        if (headerBackgroundColor != null) {
            write(" bgcolor=");
            writeDoubleQuotedText(headerBackgroundColor);
        }
        write(">");
        write(header);

        endTag("td", 0);
        endTag("th", tab);
    }

    protected void writeTextCell(String txt, int tab) {
        writeTextCell(txt, tab, null);
    }

    protected void writeTextCell(String txt, int tab, String backgroundColor) {
        writeTab(tab);
        write("<td");
        if (backgroundColor != null) {
            write(" bgcolor=");
            writeDoubleQuotedText(backgroundColor);
        }
        write(">");
        write(txt);
        write("</td>\n");
    }

    protected void writeSingleCellRow(String txt, int tab, String backgroundColor) {
        beginTag("tr", tab);
        writeTextCell(txt, tab + 1, backgroundColor);
        endTag("tr", tab);
    }

    @Override
    public void close() throws IOException {
        wr.close();
    }
}
