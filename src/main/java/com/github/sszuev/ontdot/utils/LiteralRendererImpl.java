package com.github.sszuev.ontdot.utils;

import com.github.owlcs.ontapi.jena.vocabulary.RDF;
import com.github.owlcs.ontapi.jena.vocabulary.XSD;
import com.github.sszuev.ontdot.api.DOTSetting;
import com.github.sszuev.ontdot.api.LiteralOptions;
import com.github.sszuev.ontdot.api.LiteralRenderer;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.shared.PrefixMapping;

/**
 * Created by @ssz on 22.01.2022.
 */
public class LiteralRendererImpl implements LiteralRenderer {
    private static final double SINGLE_ROW_FACTOR = 1.1;
    private static final String LAST_TRUNCATED_LINE_ENDS = "...";
    private static final String DOT_NEW_LINE_SEPARATOR = "<br/>";

    @Override
    public String print(Literal value, LiteralOptions config, PrefixMapping pm) {
        String txt = value.getLexicalForm();
        boolean isString = isStringLiteral(value);
        if (isString) {
            txt = format(txt, config);
        }
        return printLiteral(txt, isString, pm, value.getDatatypeURI(), value.getLanguage());
    }

    @Override
    public String printNonNegativeInteger(long value, LiteralOptions config, PrefixMapping pm) {
        if (value <= 0) {
            throw new IllegalArgumentException();
        }
        return printLiteral(String.valueOf(value), false, pm, XSD.nonNegativeInteger.getURI(), null);
    }

    public static String printLiteral(String text, boolean quoting, PrefixMapping pm, String type, String lang) {
        StringBuilder res = new StringBuilder();
        if (quoting) {
            res.append('"');
        }
        res.append(text.replace("\"", "\\\""));
        if (quoting) {
            res.append('"');
        }
        if (lang != null && !lang.equals("")) {
            res.append("@").append(lang);
        } else if (type != null) {
            if (!XSD.xstring.getURI().equals(type)) {
                res.append("^^").append(pm.shortForm(type));
            }
        }
        return res.toString();
    }

    public static boolean isStringLiteral(Literal value) {
        String datatype = value.getDatatypeURI();
        if (datatype == null) {
            return true;
        }
        if (value.getLanguage() != null) {
            return true;
        }
        return XSD.xstring.getURI().equals(datatype) || RDF.PlainLiteral.getURI().equals(datatype);
    }

    public String format(String txt, LiteralOptions config) {
        int rowLength = config.getInteger(DOTSetting.INT_LITERAL_ROW_LENGTH);
        int maxRows = config.getInteger(DOTSetting.INT_LITERAL_ROWS_NUM);
        return String.join(DOT_NEW_LINE_SEPARATOR, split(normalize(txt), rowLength, maxRows));
    }

    public static String normalize(String txt) {
        return txt.replace("\n", " ").replaceAll("\\s{2,}", " ").trim();
    }

    public static String[] split(String txt, int rowLength, int maxRows) {
        return split(txt, LAST_TRUNCATED_LINE_ENDS, rowLength, maxRows);
    }

    public static String[] split(String txt, String lastTruncatedLineEnds, int rowLength, int maxRows) {
        if (rowLength < lastTruncatedLineEnds.length()) {
            throw new IllegalArgumentException("Should not be less than " + lastTruncatedLineEnds.length());
        }
        if (maxRows < 1) {
            throw new IllegalArgumentException();
        }
        if (txt.length() <= rowLength * SINGLE_ROW_FACTOR) {
            return new String[]{txt};
        }
        int rows = txt.length() / rowLength;
        if (txt.length() % rowLength != 0) {
            rows++;
        }
        int from = 0;
        String[] res = new String[Math.min(rows, maxRows)];
        for (int i = 0; i < res.length; i++) {
            int to = Math.min(from + rowLength, txt.length());
            res[i] = txt.substring(from, to);
            from = to;
        }
        if (rows > maxRows) {
            String last = res[res.length - 1];
            if (last.length() + lastTruncatedLineEnds.length() > rowLength) {
                last = last.substring(0, rowLength - lastTruncatedLineEnds.length());
            }
            res[res.length - 1] = last + lastTruncatedLineEnds;
        }
        return res;
    }
}
