package com.github.sszuev.ontdot.renderers;

import com.github.owlcs.ontapi.jena.model.*;
import com.github.owlcs.ontapi.jena.utils.OntModels;
import com.github.owlcs.ontapi.jena.vocabulary.XSD;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
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
public class GraphDOTRenderer extends BaseDOTRenderer implements DOTRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphDOTRenderer.class);

    private static final String CLASS_COLOR = "#CFA500";
    private static final String DATATYPE_COLOR = "#AD3B45";
    private static final String INDIVIDUAL_COLOR = "#874B82"; // darkorchid4 ?
    private static final String OBJECT_PROPERTY_COLOR = "#0079BA";
    private static final String DATA_PROPERTY_COLOR = "#38A14A";
    private static final String ANNOTATION_PROPERTY_COLOR = "#D17A00";
    private static final String LITERAL_COLOR = "gray";

    private static final String COMPONENT_RESTRICTION_COLOR = "yellow1";
    private static final String COMPONENTS_CE_COLOR = "yellow2";
    private static final String COMPLEMENT_CE_COLOR = "yellow3";

    protected final PrefixMapping pm;

    private final AtomicLong nodeCounter = new AtomicLong();
    private final Map<Node, Long> nodeIds = new HashMap<>();

    public GraphDOTRenderer(PrefixMapping pm, Writer wr) {
        super(wr);
        this.pm = Objects.requireNonNull(pm);
    }

    @Override
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

    protected void renderEntity(OntEntity v) {
        if (v.canAs(OntClass.class)) {
            renderClass(v.as(OntClass.Named.class));
        } else if (v.canAs(OntDataRange.Named.class)) {
            renderDatatype(v.as(OntDataRange.Named.class));
        } else if (v.canAs(OntIndividual.Named.class)) {
            renderIndividual(v.as(OntIndividual.Named.class));
        } else if (v.canAs(OntObjectProperty.Named.class)) {
            renderProperty(v.as(OntObjectProperty.Named.class));
        } else if (v.canAs(OntDataProperty.class)) {
            renderProperty(v.as(OntDataProperty.class));
        } else if (v.canAs(OntAnnotationProperty.class)) {
            renderProperty(v.as(OntAnnotationProperty.class));
        }
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
        if (!supportedExpression(clazz)) {
            return;
        }
        writeCE(clazz);
        writeCELinks(null, clazz);
    }

    protected void beginDocument() {
        write("digraph OWL {\n" +
                " rankdir=\"LR\";\n" +
                " node[shape=plaintext];\n");
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
            writeDoubleQuotedText(color);
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
        writeDoubleQuotedText(color);
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
        String color = classExpressionColor(ce);
        if (color == null) {
            return;
        }
        writeNode(ce);
        beginLinkDetails();
        write("color=");
        writeDoubleQuotedText(CLASS_COLOR);
        writeComma();
        write("style=filled,fillcolor=");
        writeDoubleQuotedText(color);
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
        if (node.isLiteral()) {
            writeLiteralCell(node.asLiteral(), tab);
            return;
        }
        if (canWriteTable(node)) {
            beginTag("td", tab);
            writeNodeTable(node, tab + 1);
            endTag("td", tab);
        } else {
            writeTextCell(rdfNodeToString(node), tab);
        }
    }

    protected void writeLiteralCell(Literal node, int tab) {
        beginUnclosedTag("td", tab);
        write(" bgcolor=");
        writeDoubleQuotedText(LITERAL_COLOR);
        write(">");
        write(ModelUtils.print(node, true, pm));
        endTag("td", 0);
    }

    protected void writeLiteralCell(int nonNegativeInt, int tab) {
        beginUnclosedTag("td", tab);
        write(" bgcolor=");
        writeDoubleQuotedText(LITERAL_COLOR);
        write(">");
        write(ModelUtils.printLiteral(String.valueOf(nonNegativeInt), false, pm, XSD.nonNegativeInteger.getURI(), null));
        endTag("td", 0);
    }

    protected boolean canWriteTable(RDFNode node) {
        return node.isAnon() && supportedExpression(node);
    }

    protected boolean supportedExpression(RDFNode clazz) {
        if (clazz.canAs(OntClass.ComponentRestrictionCE.class)) {
            return true;
        }
        if (clazz.canAs(OntClass.ComponentsCE.class)) {
            return true;
        }
        if (clazz.canAs(OntClass.ComplementOf.class)) {
            return true;
        }
        //TODO:
        LOGGER.error("Unsupported class expression: {}", clazz);
        return false;
    }

    protected String classExpressionColor(OntClass clazz) {
        if (clazz.canAs(OntClass.ComponentRestrictionCE.class)) {
            return COMPONENT_RESTRICTION_COLOR;
        }
        if (clazz.canAs(OntClass.ComponentsCE.class)) {
            return COMPONENTS_CE_COLOR;
        }
        if (clazz.canAs(OntClass.ComplementOf.class)) {
            return COMPLEMENT_CE_COLOR;
        }
        throw new IllegalStateException("For class " + clazz);
    }

    protected void writeCETable(OntClass.ComponentRestrictionCE<?, ?> ce, int tab) {
        String header = getOntHeader(ce);
        OntRealProperty first = ce.getProperty();
        RDFNode second = ce.getValue();

        beginTable(tab);

        writeTableHeader(tab + 1, header, CLASS_COLOR, ce instanceof OntClass.CardinalityRestrictionCE ? 3 : 2);

        beginTag("tr", tab + 1);
        // first cell (todo: handle anon object property):
        writeTextCell(rdfNodeToString(first), tab + 2);

        // second cell
        if (ce instanceof OntClass.CardinalityRestrictionCE) {
            int q = ((OntClass.CardinalityRestrictionCE<?, ?>) ce).getCardinality();
            writeLiteralCell(q, tab + 2);
        }

        // last cell:
        writeNodeCell(second, tab + 2);
        endTag("tr", tab + 1);

        endTable(tab);
    }

    protected void writeCETable(OntClass.ComponentsCE<?> ce, int tab) {
        String header = getOntHeader(ce);

        beginTable(tab);

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
        writeDoubleQuotedText(CLASS_COLOR);
        endLinkDetails();
        writeSemicolon();
        renderLinkNodes(right);
    }

    protected void writeIndividualTypeLinks(OntIndividual i, OntClass t) {
        writeLink(i, t, INDIVIDUAL_COLOR);
    }

    protected void writeCELinks(RDFNode from, OntClass clazz) {
        if (clazz.canAs(OntClass.ComponentRestrictionCE.class)) {
            writeCELinks(from, clazz.as(OntClass.ComponentRestrictionCE.class));
        } else if (clazz.canAs(OntClass.ComponentsCE.class)) {
            writeCELinks(from, clazz.as(OntClass.ComponentsCE.class));
        } else if (clazz.canAs(OntClass.ComplementOf.class)) {
            writeCELinks(from, clazz.as(OntClass.ComplementOf.class));
        } else {
            throw new IllegalStateException("For class=" + clazz);
        }
    }

    protected void writeCELinks(RDFNode from, OntClass.ComponentRestrictionCE<?, ?> ce) {
        if (from == null) {
            from = ce;
        }
        RDFNode v = ce.getValue();
        if (!v.isLiteral()) {
            if (v.isAnon() && v.canAs(OntClass.class)) {
                writeCELinks(from, v.as(OntClass.class));
            } else {
                writeLink(from, v, v.canAs(OntClass.class) ? CLASS_COLOR : null);
            }
        }

        OntRealProperty p = ce.getProperty();
        if (p.isAnon()) {
            // TODO: handle this case
            LOGGER.error("Not supported {}", p);
        }
        writeLink(from, p, p.canAs(OntObjectProperty.class) ? OBJECT_PROPERTY_COLOR : null);

        renderBuiltinEntity(v);
        renderBuiltinEntity(p);
    }

    private void renderBuiltinEntity(RDFNode e) {
        if (!e.isURIResource()) {
            return;
        }
        renderBuiltinEntity(e.as(OntEntity.class));
    }

    protected void renderBuiltinEntity(OntEntity e) {
        if (!e.isBuiltIn() || ModelUtils.isDeclared(e)) {
            return;
        }
        renderEntity(e);
    }

    protected void writeCELinks(RDFNode from, OntClass.ComplementOf ce) {
        if (from == null) {
            from = ce;
        }
        OntClass v = ce.getValue();
        if (v.isAnon()) {
            writeCELinks(from, v);
        } else {
            writeLink(from, v, CLASS_COLOR);
        }
    }

    protected void writeCELinks(RDFNode from, OntClass.ComponentsCE<?> ce) {
        if (from == null) {
            from = ce;
        }
        String color = null;
        if (ce.canAs(OntClass.OneOf.class)) {
            color = INDIVIDUAL_COLOR;
        }
        if (color == null && ce.canAs(OntClass.UnionOf.class) || ce.canAs(OntClass.IntersectionOf.class)) {
            color = CLASS_COLOR;
        }
        List<RDFNode> members = ce.getList().members().collect(Collectors.toList());
        for (RDFNode m : members) {
            if (m.isAnon() && m.canAs(OntClass.class)) {
                writeCELinks(from, m.as(OntClass.class));
            } else {
                writeLink(from, m, color);
            }
        }
    }

    protected void renderLinkNodes(RDFNode... nodes) {
    }
}
