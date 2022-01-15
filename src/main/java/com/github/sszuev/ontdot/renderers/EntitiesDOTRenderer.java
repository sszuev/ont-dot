package com.github.sszuev.ontdot.renderers;

import com.github.owlcs.ontapi.jena.model.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;

import java.io.Writer;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by @ssz on 15.01.2022.
 */
public class EntitiesDOTRenderer extends GraphDOTRenderer {
    private final Set<String> filterEntities;

    private final Set<OntClass.Named> visitedClasses = new HashSet<>();
    private final Set<OntDataRange.Named> visitedDatatypes = new HashSet<>();
    private final Set<OntIndividual.Named> visitedIndividuals = new HashSet<>();
    private final Set<OntObjectProperty.Named> visitedObjectProperties = new HashSet<>();
    private final Set<OntDataProperty> visitedDataProperties = new HashSet<>();
    private final Set<OntAnnotationProperty> visitedAnnotationProperties = new HashSet<>();

    public EntitiesDOTRenderer(PrefixMapping pm, Writer w, Set<String> filterEntities) {
        super(pm, w);
        this.filterEntities = Objects.requireNonNull(filterEntities);
    }

    @Override
    public void render(OntModel ont) {
        reset();
        beginDocument();

        ont.classes().filter(this::filter).forEach(this::renderClass);
        ont.namedIndividuals().filter(this::filter).forEach(this::renderIndividual);
        ont.datatypes().filter(this::filter).forEach(this::renderDatatype);
        ont.objectProperties().filter(this::filter).forEach(this::renderProperty);
        ont.dataProperties().filter(this::filter).forEach(this::renderProperty);
        ont.annotationProperties().filter(this::filter).forEach(this::renderProperty);

        endDocument();
    }

    protected void reset() {
        visitedClasses.clear();
        visitedDatatypes.clear();
        visitedIndividuals.clear();
        visitedObjectProperties.clear();
        visitedDataProperties.clear();
        visitedAnnotationProperties.clear();
    }

    @Override
    protected void renderClass(OntClass.Named clazz) {
        if (visitedClasses.add(clazz)) {
            super.renderClass(clazz);
        }
    }

    @Override
    protected void renderDatatype(OntDataRange.Named datatype) {
        if (visitedDatatypes.add(datatype)) {
            super.renderDatatype(datatype);
        }
    }

    @Override
    protected void renderProperty(OntObjectProperty.Named property) {
        if (visitedObjectProperties.add(property)) {
            super.renderProperty(property);
        }
    }

    @Override
    protected void renderProperty(OntDataProperty property) {
        if (visitedDataProperties.add(property)) {
            super.renderProperty(property);
        }
    }

    @Override
    protected void renderProperty(OntAnnotationProperty property) {
        if (visitedAnnotationProperties.add(property)) {
            super.renderProperty(property);
        }
    }

    @Override
    protected void renderIndividual(OntIndividual.Named individual) {
        if (visitedIndividuals.add(individual)) {
            super.renderIndividual(individual);
        }
    }

    @Override
    protected void renderLinkNodes(RDFNode... nodes) {
        for (RDFNode entity : nodes) {
            if (!entity.isURIResource()) {
                continue;
            }
            if (entity instanceof OntEntity) {
                renderEntity((OntEntity) entity);
            }
        }
    }

    protected boolean filter(OntEntity e) {
        return filter(e.getURI());
    }

    protected boolean filter(String uri) {
        if (filterEntities.contains(uri)) {
            return true;
        }
        String su = pm.shortForm(uri);
        if (uri.equals(su)) {
            return false;
        }
        return filterEntities.contains(su);
    }

    @Override
    protected void renderEntity(OntEntity entity) {
        if (entity instanceof OntClass.Named && !visitedClasses.contains(entity)) {
            renderClass((OntClass.Named) entity);
        } else if (entity instanceof OntDataRange.Named && !visitedDatatypes.contains(entity)) {
            renderDatatype((OntDataRange.Named) entity);
        } else if (entity instanceof OntObjectProperty.Named && !visitedObjectProperties.contains(entity)) {
            renderProperty((OntObjectProperty.Named) entity);
        } else if (entity instanceof OntDataProperty && !visitedDataProperties.contains(entity)) {
            renderProperty((OntDataProperty) entity);
        } else if (entity instanceof OntAnnotationProperty && !visitedAnnotationProperties.contains(entity)) {
            renderProperty((OntAnnotationProperty) entity);
        } else if (entity instanceof OntIndividual.Named && !visitedIndividuals.contains(entity)) {
            renderIndividual((OntIndividual.Named) entity);
        }
    }
}
