package com.github.sszuev.dot;

import com.github.owlcs.ontapi.jena.model.*;
import com.github.owlcs.ontapi.jena.utils.OntModels;
import com.github.owlcs.ontapi.jena.vocabulary.OWL;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDFS;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Created by @ssz on 09.01.2022.
 */
public class DOTRenderer {
    private static final String CLASS_COLOR = "orangered";
    private static final String INDIVIDUAL_COLOR = "deeppink";
    private static final String OBJECT_PROPERTY_COLOR = "cyan4";
    private static final String DATA_PROPERTY_COLOR = "forestgreen";
    private static final String ANNOTATION_PROPERTY_COLOR = "chartreuse3";

    private static final String COMPONENT_RESTRICTION_COLOR = "yellow1";
    private static final String COMPONENTS_CE_COLOR = "yellow2";
    private static final String COMPLEMENT_CE_COLOR = "yellow3";

    private final Writer wr;
    private final PrefixMapping pm;

    private final AtomicLong nodeCounter = new AtomicLong();
    private final Map<Node, Long> nodeIds = new HashMap<>();

    protected DOTRenderer(PrefixMapping pm, Writer w) {
        this.pm = Objects.requireNonNull(pm);
        this.wr = Objects.requireNonNull(w);
    }

    /**
     * Draws the specified {@link OntModel OWL Graph} to the destination represented by {@link Writer}.
     *
     * @param model  {@link OntModel}
     * @param writer {@link Writer}
     */
    public static void draw(OntModel model, Writer writer) {
        new DOTRenderer(model, writer).render(model);
    }

    /**
     * Draws the specified {@link OntModel OWL Graph} as a {@code String}.
     *
     * @param model {@link OntModel}
     * @return {@code String}
     */
    public static String drawAsString(OntModel model) {
        StringWriter sw = new StringWriter();
        draw(model, sw);
        return sw.toString();
    }

    private static String fillColor(String color) {
        return "style=filled,fillcolor=" + color;
    }

    private static String color(String color) {
        return "color=" + color;
    }

    private static String printTable(String name) {
        return String.format("\t\t<table border='0' cellborder='1' cellspacing='0'>\n" +
                "\t\t\t<tr>\n" +
                "\t\t\t\t<td>%s</td>\n" +
                "\t\t\t</tr>\n" +
                "\t\t</table>", name);
    }

    private static String printTable(String header, String headerColor, String first, String second) {
        StringBuilder sb = new StringBuilder("\t\t<table border='0' cellborder='1' cellspacing='0'>\n");
        if (header != null) {
            sb.append("\t\t\t<th port=\"header\">\n");
            sb.append("\t\t\t\t<td colspan='2'");
            if (headerColor != null) {
                sb.append(" bgcolor=\"").append(headerColor).append("\"");
            }
            sb.append(">").append(header).append("</td>\n");
            sb.append("\t\t\t</th>\n");
        }
        sb.append("\t\t\t<tr>\n");
        sb.append("\t\t\t\t<td>").append(first).append("</td>\n");
        sb.append("\t\t\t\t<td>").append(second).append("</td>\n");
        sb.append("\t\t\t</tr>\n");
        sb.append("\t\t</table>");
        return sb.toString();
    }

    private static String printTable(String header, String headerColor, List<String> components) {
        StringBuilder sb = new StringBuilder("\t\t<table border='0' cellborder='1' cellspacing='0'>\n");
        if (header != null) {
            sb.append("\t\t\t<th port=\"header\">\n");
            sb.append("\t\t\t\t<td");
            if (headerColor != null) {
                sb.append(" bgcolor=\"").append(headerColor).append("\"");
            }
            sb.append(">").append(header).append("</td>\n");
            sb.append("\t\t\t</th>\n");
        }
        for (String s : components) {
            sb.append("\t\t\t<tr>\n");
            sb.append("\t\t\t\t<td>").append(s).append("</td>\n");
            sb.append("\t\t\t</tr>\n");
        }
        sb.append("\t\t</table>");
        return sb.toString();
    }

    public void render(OntModel ont) {
        beginDocument();

        ont.classes().forEach(this::writeClass);
        ont.namedIndividuals().forEach(this::writeIndividual);
        ont.objectProperties().forEach(this::writeProperty);
        ont.dataProperties().forEach(this::writeProperty);
        ont.annotationProperties().forEach(this::writeProperty);

        ont.ontObjects(OntClass.ComponentRestrictionCE.class).forEach(this::writeCE);
        ont.ontObjects(OntClass.ComponentsCE.class).forEach(this::writeCE);
        ont.ontObjects(OntClass.ComplementOf.class).forEach(this::writeCE);

        ont.statements(null, RDFS.subClassOf, null).filter(s -> ModelUtils.testStatement(s, OntClass.class))
                .forEach(s -> writeSubClassOfLink(s.getSubject(), s.getResource()));
        ont.statements(null, OWL.equivalentClass, null).filter(s -> ModelUtils.testStatement(s, OntClass.class))
                .forEach(s -> writeEquivalentClassLinks(s.getSubject(), s.getResource()));
        ont.statements(null, RDFS.subPropertyOf, null).filter(s -> ModelUtils.testStatement(s, OntProperty.class))
                .forEach(s -> writeSubPropertyOfLinks(s.getSubject(), s.getResource()));

        ont.individuals().forEach(i -> i.classes().forEach(t -> writeIndividualTypeLinks(i, t)));

        ont.ontObjects(OntClass.ComponentRestrictionCE.class).forEach(this::writeCELinks);
        ont.ontObjects(OntClass.ComponentsCE.class).forEach(this::writeCELinks);
        ont.ontObjects(OntClass.ComplementOf.class).forEach(this::writeCELinks);

        endDocument();
    }

    protected void beginDocument() {
        write("digraph OWL {\n" +
                "    rankdir=\"LR\";\n" +
                "    node[shape=plaintext];\n");
    }

    protected void endDocument() {
        write("\n}\n");
    }

    protected void write(String s) {
        try {
            wr.write(s);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected void printf(String s, Object... args) {
        write(String.format(s, args));
    }

    protected void writeLink(RDFNode from, RDFNode to) {
        writeNode(from);
        write("->");
        writeNode(to);
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

    protected void writeNode(RDFNode node) {
        write("n" + id(node));
    }

    protected void writeLink(RDFNode from, RDFNode to, String color) {
        writeLink(from, to);
        if (color != null) {
            beginLinkDetails();
            write("color=");
            write(color);
            endLinkDetails();
        }
        writeSemicolon();
    }

    protected void writeClass(OntClass.Named clazz) {
        printf("n%d[%s, label=<\n%s\n\t>];\n", id(clazz), fillColor(CLASS_COLOR), printTable(uri(clazz)));
    }

    private void writeIndividual(OntIndividual.Named individual) {
        printf("n%d[%s, label=<\n%s\n\t>];\n", id(individual), fillColor(INDIVIDUAL_COLOR), printTable(uri(individual)));
    }

    private void writeProperty(OntObjectProperty.Named property) {
        printf("n%d[%s, label=<\n%s\n\t>];\n", id(property), fillColor(OBJECT_PROPERTY_COLOR), printTable(uri(property)));
    }

    private void writeProperty(OntDataProperty uri) {
        printf("n%d[%s, label=<\n%s\n\t>];\n", id(uri), fillColor(DATA_PROPERTY_COLOR), printTable(uri(uri)));
    }

    private void writeProperty(OntAnnotationProperty uri) {
        printf("n%d[%s, label=<\n%s\n\t>];\n", id(uri), fillColor(ANNOTATION_PROPERTY_COLOR), printTable(uri(uri)));
    }

    private void writeCE(OntClass.ComponentRestrictionCE<?, ?> ce) {
        OntRealProperty p = ce.getProperty();
        RDFNode v = ce.getValue();
        String header = OntModels.getOntType(ce).getSimpleName();
        printf("n%d[%s,%s,label=<\n%s\n\t>];\n",
                id(ce), color(CLASS_COLOR), fillColor(COMPONENT_RESTRICTION_COLOR),
                printTable(header, CLASS_COLOR, rdfNodeToString(v), rdfNodeToString(p)));
    }

    private void writeCE(OntClass.ComplementOf node) {
        printf("n%d[%s,%s,label=<\n%s\n\t>];\n",
                id(node), color(CLASS_COLOR), fillColor(COMPLEMENT_CE_COLOR),
                printTable("ComplementOf(" + rdfNodeToString(node.getValue()) + ")"));
    }

    private void writeCE(OntClass.ComponentsCE<?> ce) {
        List<String> components = ce.getList().members().map(x -> (RDFNode) x)
                .map(this::rdfNodeToString)
                .collect(Collectors.toList());
        String header = OntModels.getOntType(ce).getSimpleName();
        printf("n%d[%s,%s,label=<\n%s\n\t>];\n",
                id(ce), color(CLASS_COLOR), fillColor(COMPONENTS_CE_COLOR),
                printTable(header, CLASS_COLOR, components));
    }

    private String rdfNodeToString(RDFNode node) {
        if (node.isURIResource()) {
            return uri(node.asNode());
        }
        return "n" + id(node);
    }

    private String uri(Resource uri) {
        if (!uri.isURIResource()) {
            throw new IllegalArgumentException();
        }
        return uri(uri.asNode());
    }

    private String uri(Node node) {
        return node.toString(pm, false);
    }

    private Long id(RDFNode node) {
        return id(node.asNode());
    }

    private Long id(Node node) {
        return nodeIds.computeIfAbsent(node, x -> nodeCounter.incrementAndGet());
    }

    protected void writeSubClassOfLink(Resource sub, Resource sup) {
        writeLink(sub, sup, CLASS_COLOR);
    }

    protected void writeSubPropertyOfLinks(Resource sub, Resource sup) {
        String color = null;
        if (sub.canAs(OntAnnotationProperty.class)) {
            color = ANNOTATION_PROPERTY_COLOR;
        }
        if (color == null && sub.canAs(OntDataProperty.class)) {
            color = DATA_PROPERTY_COLOR;
        }
        if (color == null && sub.canAs(OntObjectProperty.class)) {
            color = OBJECT_PROPERTY_COLOR;
        }
        writeLink(sub, sup, color);
    }

    protected void writeEquivalentClassLinks(Resource clazz, Resource eq) {
        writeLink(clazz, eq);
        beginLinkDetails();
        write("dir=both");
        writeComma();
        write("color=");
        write(CLASS_COLOR);
        endLinkDetails();
        writeSemicolon();
    }

    protected void writeIndividualTypeLinks(OntIndividual i, OntClass t) {
        writeLink(i, t, INDIVIDUAL_COLOR);
    }

    protected void writeCELinks(OntClass.ComponentRestrictionCE<?, ?> r) {
        RDFNode v = r.getValue();
        OntRealProperty p = r.getProperty();
        writeLink(r, v, v.canAs(OntClass.class) ? CLASS_COLOR : null);
        writeLink(r, p, p.canAs(OntObjectProperty.class) ? OBJECT_PROPERTY_COLOR : null);
    }

    protected void writeCELinks(OntClass.ComplementOf ce) {
        writeLink(ce, ce.getValue(), CLASS_COLOR);
    }

    protected void writeCELinks(OntClass.ComponentsCE<?> ce) {
        String color = null;
        if (ce.canAs(OntClass.OneOf.class)) {
            color = INDIVIDUAL_COLOR;
        }
        if (color == null && ce.canAs(OntClass.UnionOf.class) || ce.canAs(OntClass.IntersectionOf.class)) {
            color = CLASS_COLOR;
        }
        List<RDFNode> members = ce.getList().members().collect(Collectors.toList());
        for (RDFNode m : members) {
            writeLink(ce, m, color);
        }
    }
}
