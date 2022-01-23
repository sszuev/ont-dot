package com.github.sszuev.ontdot.utils;

import com.github.owlcs.ontapi.jena.model.*;
import com.github.owlcs.ontapi.jena.utils.Iter;
import com.github.owlcs.ontapi.jena.vocabulary.OWL;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Partially copy-pasted from <a href='https://github.com/owlcs/ont-map'>ONT-MAP</a>
 * <p>
 * Created by @ssz on 15.01.2022.
 */
public class ModelUtils {

    /**
     * Lists all properties that are related to the given class expression.
     * The class hierarchy are not taken into account.
     * The result includes the following cases:
     * <ul>
     * <li>right part of {@code rdfs:domain}, see {@link OntClass#properties()}</li>
     * <li>the subject of {@code owl:inverseOf}, if it is in {@code rdfs:range} relation with the given {@link OntClass}</li>
     * <li>{@code owl:onProperties} and {@code owl:onProperty} relations</li>
     * </ul>
     *
     * @param ce {@link OntClass}, not {@code null}
     * @return <b>distinct</b> {@code Stream} of {@link OntProperty properties}
     * @see OntClass#properties()
     */
    public static Stream<OntProperty> properties(OntClass ce) {
        // direct domains
        Stream<OntProperty> domains = ce.properties();
        // indirect domains (ranges for inverseOf object properties):
        Stream<OntProperty> ranges = ce.getModel().statements(null, OWL.inverseOf, null)
                .filter(x -> x.getSubject().canAs(OntObjectProperty.class) && ce.hasProperty(RDFS.range, x.getObject()))
                .map(x -> x.getSubject().as(OntProperty.class));
        // on properties for restrictions
        Stream<OntProperty> onProps = Stream.empty();
        if (ce instanceof OntClass.RestrictionCE) {
            onProps = Stream.of(((OntClass.RestrictionCE<?>) ce).getProperty());
        }
        return Stream.of(domains, ranges, onProps).flatMap(Function.identity()).distinct();
    }

    /**
     * Answers a {@code Stream} over all {@code owl:propertyChainAxiom} object properties.
     *
     * @param m {@link OntModel}
     * @return {@code Stream} of {@link OntObjectProperty}s
     */
    public static Stream<OntObjectProperty> propertyChains(OntModel m) {
        return Iter.asStream(listPropertyChains(m));
    }

    /**
     * Answers an {@code ExtendedIterator} over all {@code owl:propertyChainAxiom} object properties.
     *
     * @param m {@link OntModel}
     * @return {@link ExtendedIterator} of {@link OntObjectProperty}s
     */
    public static ExtendedIterator<OntObjectProperty> listPropertyChains(OntModel m) {
        return m.listStatements(null, OWL.propertyChainAxiom, (RDFNode) null)
                .mapWith(Statement::getSubject)
                .filterKeep(s -> s.canAs(OntObjectProperty.class))
                .mapWith(s -> s.as(OntObjectProperty.class));
    }

    /**
     * Answers {@code true} if the left argument has a {@code owl:propertyChainAxiom} list
     * that contains the right argument in the first position.
     *
     * @param superProperty {@link OntObjectProperty}, not {@code null}
     * @param candidate     {@link OntObjectProperty}
     * @return boolean
     */
    public static boolean isHeadOfPropertyChain(OntObjectProperty superProperty, OntObjectProperty candidate) {
        return superProperty.propertyChains()
                .map(OntList::first)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .anyMatch(p -> Objects.equals(p, candidate));
    }

    /**
     * Answers {@code true} iff entity has explicit declaration in graph.
     * Built-ins are not declared usually.
     *
     * @param e {@link OntEntity}
     * @return {@code boolean}
     */
    public static boolean isDeclared(OntEntity e) {
        try (Stream<OntStatement> s = e.spec()) {
            return s.findFirst().isPresent();
        }
    }

}
