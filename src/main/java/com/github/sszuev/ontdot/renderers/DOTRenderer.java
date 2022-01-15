package com.github.sszuev.ontdot.renderers;

import com.github.owlcs.ontapi.jena.model.*;
import com.github.owlcs.ontapi.jena.utils.OntModels;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class DOTRenderer extends BaseDOTRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DOTRenderer.class);

    private static final String CLASS_COLOR = "orangered";
    private static final String DATATYPE_COLOR = "coral4";
    private static final String INDIVIDUAL_COLOR = "deeppink"; // darkorchid4 ?
    private static final String OBJECT_PROPERTY_COLOR = "cyan4";
    private static final String DATA_PROPERTY_COLOR = "forestgreen";
    private static final String ANNOTATION_PROPERTY_COLOR = "chartreuse3";

    private static final String COMPONENT_RESTRICTION_COLOR = "yellow1";
    private static final String COMPONENTS_CE_COLOR = "yellow2";
    private static final String COMPLEMENT_CE_COLOR = "yellow3";

    protected final PrefixMapping pm;

    private final AtomicLong nodeCounter = new AtomicLong();
    private final Map<Node, Long> nodeIds = new HashMap<>();

    public DOTRenderer(PrefixMapping pm, Writer wr) {
        super(wr);
        this.pm = Objects.requireNonNull(pm);
    }

    public void render(OntModel ont) {
        beginDocument();

        ont.classes().forEach(this::renderClass);
        ont.namedIndividuals().forEach(this::renderIndividual);
        ont.datatypes().forEach(this::renderDatatype);
        ont.objectProperties().forEach(this::renderProperty);
        ont.dataProperties().forEach(this::renderProperty);
        ont.annotationProperties().forEach(this::renderProperty);

        endDocument();
    }

    protected void renderClass(OntClass.Named clazz) {
        writeClass(clazz);
        clazz.superClasses().forEach(ce -> {
            renderCE(ce);
            writeSubClassOfLink(clazz, ce);
        });
        clazz.equivalentClasses().forEach(ce -> {
            renderCE(ce);
            writeEquivalentClassLinks(clazz, ce);
        });
    }

    protected void renderDatatype(OntDataRange.Named datatype) {
        writeDatatype(datatype);
        // TODO:
    }

    protected void renderProperty(OntObjectProperty.Named property) {
        writeProperty(property);
        property.superProperties().forEach(s -> writeSubPropertyOfLinks(property, s));
    }

    protected void renderProperty(OntDataProperty property) {
        writeProperty(property);
        property.superProperties().forEach(s -> writeSubPropertyOfLinks(property, s));
    }

    protected void renderProperty(OntAnnotationProperty property) {
        writeProperty(property);
        property.superProperties().forEach(s -> writeSubPropertyOfLinks(property, s));
    }

    protected void renderIndividual(OntIndividual.Named individual) {
        writeIndividual(individual);
        individual.classes().forEach(t -> writeIndividualTypeLinks(individual, t));
    }

    protected void renderCE(OntClass clazz) {
        if (clazz.isURIResource()) {
            return;
        }
        writeCE(clazz);
        writeCELinks(clazz);
    }

    protected void beginDocument() {
        write("digraph OWL {\n" +
                "    rankdir=\"LR\";\n" +
                "    node[shape=plaintext];\n");
    }

    protected void endDocument() {
        write("\n}\n");
    }

    protected void writeLink(RDFNode from, RDFNode to) {
        writeNode(from);
        write("->");
        writeNode(to);
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
        renderLinkNodes(to);
    }

    protected void writeClass(OntClass.Named clazz) {
        writeEntity(clazz, CLASS_COLOR);
    }

    protected void writeDatatype(OntDataRange.Named datatype) {
        writeEntity(datatype, DATATYPE_COLOR);
    }

    private void writeIndividual(OntIndividual.Named individual) {
        writeEntity(individual, INDIVIDUAL_COLOR);
    }

    private void writeProperty(OntObjectProperty.Named property) {
        writeEntity(property, OBJECT_PROPERTY_COLOR);
    }

    private void writeProperty(OntDataProperty property) {
        writeEntity(property, DATA_PROPERTY_COLOR);
    }

    private void writeProperty(OntAnnotationProperty property) {
        writeEntity(property, ANNOTATION_PROPERTY_COLOR);
    }

    protected void writeEntity(OntEntity entity, String color) {
        writeNode(entity);
        beginLinkDetails();
        write("style=filled,fillcolor=");
        write(color);
        writeComma();
        beginDetailsLabel();
        writeNewLine();

        beginTable(0);
        beginTag("tr", 1);
        writeTextCell(uri(entity), 2);
        endTag("tr", 1);
        endTable(0);

        endDetailsLabel();
        writeNewLine();
        endLinkDetails();
        writeSemicolon();
    }

    protected void writeCE(OntClass ce) {
        String color = supportedExpressionColor(ce);
        if (color == null) {
            return;
        }
        writeNode(ce);
        beginLinkDetails();
        write("color=");
        write(CLASS_COLOR);
        writeComma();
        write("style=filled,fillcolor=");
        write(color);
        writeComma();
        beginDetailsLabel();
        writeNewLine();

        writeNodeTable(ce, 0);

        endDetailsLabel();
        writeNewLine();
        endLinkDetails();
        writeSemicolon();
    }


    protected void writeNodeTable(RDFNode node, int tab) {
        if (node.canAs(OntClass.ComponentRestrictionCE.class)) {
            writeCETable(node.as(OntClass.ComponentRestrictionCE.class), tab);
        } else if (node.canAs(OntClass.ComponentsCE.class)) {
            writeCETable(node.as(OntClass.ComponentsCE.class), tab);
        } else if (node.canAs(OntClass.ComplementOf.class)) {
            writeCETable(node.as(OntClass.ComplementOf.class), tab);
        } else {
            throw new IllegalArgumentException("For node " + node);
        }
    }

    protected void writeNodeCell(RDFNode node, int tab) {
        if (canWriteTable(node)) {
            beginTag("td", tab);
            writeNodeTable(node, tab + 1);
            endTag("td", tab);
        } else {
            writeTextCell(rdfNodeToString(node), tab);
        }
    }

    protected boolean canWriteTable(RDFNode node) {
        return node.isAnon() && supportedExpressionColor(node) != null;
    }

    protected String supportedExpressionColor(RDFNode clazz) {
        if (clazz.canAs(OntClass.ComponentRestrictionCE.class)) {
            return COMPONENT_RESTRICTION_COLOR;
        }
        if (clazz.canAs(OntClass.ComponentsCE.class)) {
            return COMPONENTS_CE_COLOR;
        }
        if (clazz.canAs(OntClass.ComplementOf.class)) {
            return COMPLEMENT_CE_COLOR;
        }
        //TODO:
        LOGGER.error("Unsupported class expression: {}", clazz);
        return null;
    }

    protected void writeCETable(OntClass.ComponentRestrictionCE<?, ?> ce, int tab) {
        String header = getOntHeader(ce);
        OntRealProperty first = ce.getProperty();
        RDFNode second = ce.getValue();

        beginTable(tab);
        writeNewLine();

        writeTableHeader(tab + 1, header, CLASS_COLOR, 2);

        beginTag("tr", tab + 1);
        // first cell:
        writeTextCell(rdfNodeToString(first), tab + 2);
        // second cell:
        writeNodeCell(second, tab + 2);
        endTag("tr", tab + 1);

        endTable(tab);
    }

    protected void writeCETable(OntClass.ComponentsCE<?> ce, int tab) {
        String header = getOntHeader(ce);

        beginTable(tab);
        writeNewLine();

        writeTableHeader(tab + 1, header, CLASS_COLOR, -1);

        ce.getList().members().map(x -> (RDFNode) x).forEach(node -> {
            beginTag("tr", tab + 1);
            writeNodeCell(node, tab + 2);
            endTag("tr", tab + 1);
        });

        endTable(tab);
    }

    protected void writeCETable(OntClass.ComplementOf ce, int tab) {
        String header = getOntHeader(ce);
        OntClass value = ce.getValue();

        beginTable(tab);
        writeNewLine();

        writeTableHeader(tab + 1, header, CLASS_COLOR, 2);

        beginTag("tr", tab + 1);
        writeNodeCell(value, tab + 2);
        endTag("tr", tab + 1);

        endTable(tab);
    }

    protected String getOntHeader(OntObject obj) {
        return OntModels.getOntType(obj).getSimpleName();
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

    protected void writeEquivalentClassLinks(Resource left, Resource right) {
        writeLink(left, right);
        beginLinkDetails();
        write("dir=both");
        writeComma();
        write("color=");
        write(CLASS_COLOR);
        endLinkDetails();
        writeSemicolon();
        renderLinkNodes(right);
    }

    protected void writeIndividualTypeLinks(OntIndividual i, OntClass t) {
        writeLink(i, t, INDIVIDUAL_COLOR);
    }

    protected void writeCELinks(OntClass clazz) {
        if (clazz.canAs(OntClass.ComponentRestrictionCE.class)) {
            writeCELinks(clazz.as(OntClass.ComponentRestrictionCE.class));
        } else if (clazz.canAs(OntClass.ComponentsCE.class)) {
            writeCELinks(clazz.as(OntClass.ComponentsCE.class));
        } else if (clazz.canAs(OntClass.ComplementOf.class)) {
            writeCELinks(clazz.as(OntClass.ComplementOf.class));
        }
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

    protected void renderLinkNodes(RDFNode... nodes) {
    }
}
