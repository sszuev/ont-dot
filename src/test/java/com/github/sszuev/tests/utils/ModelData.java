package com.github.sszuev.tests.utils;

import com.github.owlcs.ontapi.OWLAdapter;
import com.github.owlcs.ontapi.OntFormat;
import com.github.owlcs.ontapi.Ontology;
import com.github.owlcs.ontapi.OntologyManager;
import com.github.owlcs.ontapi.jena.model.OntClass;
import com.github.owlcs.ontapi.jena.model.OntDataProperty;
import com.github.owlcs.ontapi.jena.model.OntIndividual;
import com.github.owlcs.ontapi.jena.model.OntModel;
import org.junit.jupiter.api.Assertions;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.PriorityCollection;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

/**
 * Collection of ontology resources for tests.
 * Created by @ssz on 19.04.2020.
 */
@SuppressWarnings("unused")
public enum ModelData {
    PIZZA("/ontapi/pizza.ttl", "http://www.co-ode.org/ontologies/pizza/pizza.owl"),
    FAMILY("/ontapi/family.ttl", "http://www.co-ode.org/roberts/family-tree.owl"),
    PEOPLE("/ontapi/people.ttl", "http://owl.man.ac.uk/2006/07/sssw/people"),
    CAMERA("/ontapi/camera.ttl", "http://www.xfront.com/owl/ontologies/camera/"),
    KOALA("/ontapi/koala.ttl", "http://protege.stanford.edu/plugins/owl/owl-library/koala.owl"),
    TRAVEL("/ontapi/travel.ttl", "http://www.owl-ontologies.com/travel.owl"),
    WINE("/ontapi/wine.ttl", "http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine") {
        @Override
        public OWLOntology fetch(OWLOntologyManager manager) {
            return load(manager, FOOD, this);
        }
    },
    FOOD("/ontapi/food.ttl", "http://www.w3.org/TR/2003/PR-owl-guide-20031209/food") {
        @Override
        public OWLOntology fetch(OWLOntologyManager manager) {
            return load(manager, WINE, this);
        }
    },
    NCBITAXON_CUT("/ontapi/ncbitaxon2.ttl", "http://purl.bioontology.org/ontology/NCBITAXON/") {
        @Override
        public String getNS() {
            return getURI();
        }
    },
    HP_CUT("/ontapi/hp-cut.ttl", "http://purl.obolibrary.org/obo/hp.owl"),
    FAMILY_PEOPLE_UNION(null, null) {
        @Override
        public String getNS() {
            return "http://www.ex.org/tribe#";
        }

        @Override
        public OWLOntologyDocumentSource getDocumentSource() {
            throw new UnsupportedOperationException();
        }

        @Override
        public OWLOntology fetch(OWLOntologyManager manager) {
            return manager instanceof OntologyManager ?
                    ModelData.createFamilyPeopleModelUsingONTAPI((OntologyManager) manager, getNS()) :
                    ModelData.createFamilyPeopleModelUsingOWLAPI(manager, getNS());
        }
    },
    ;

    private final Path file;
    private final OntFormat format;
    private final String uri;

    ModelData(String file, String name) {
        this(file, OntFormat.TURTLE, name);
    }

    ModelData(String file, OntFormat format, String name) {
        this.file = file == null ? null : toPath(file);
        this.format = format;
        this.uri = name;
    }

    private static Path toPath(String file) {
        try {
            return Paths.get(Objects.requireNonNull(ModelData.class.getResource(file)).toURI()).toRealPath();
        } catch (IOException | URISyntaxException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    static OWLOntology load(OWLOntologyManager manager, ModelData... data) {
        OWLOntology res = null;
        OWLOntologyLoaderConfiguration conf = createConfig(manager);
        long before = manager.ontologies().count();
        if (!(manager instanceof OntologyManager)) { // OWL-API
            manager.setOntologyLoaderConfiguration(conf);
            PriorityCollection<OWLOntologyIRIMapper> maps = manager.getIRIMappers();
            Arrays.stream(data)
                    .map(d -> FileMap.create(IRI.create(d.getURI()), d.getDocumentSource().getDocumentIRI()))
                    .forEach(maps::add);
            try {
                res = manager.loadOntology(IRI.create(data[data.length - 1].getURI()));
            } catch (OWLOntologyCreationException e) {
                throw new AssertionError(e);
            }
        } else { // ONT-API
            for (ModelData d : data) {
                try {
                    res = manager.loadOntologyFromOntologyDocument(d.getDocumentSource(), conf);
                } catch (OWLOntologyCreationException e) {
                    throw new AssertionError(e);
                }
            }
        }
        Assertions.assertEquals(data.length + before, manager.ontologies().count());
        return res;
    }

    static OWLOntologyLoaderConfiguration createConfig(OWLOntologyManager manager) {
        OWLOntologyLoaderConfiguration conf = manager.getOntologyLoaderConfiguration();
        if (manager instanceof OntologyManager) {
            conf = OWLAdapter.get().asONT(conf).setProcessImports(false).setPerformTransformation(false);
        } else {
            conf = conf.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
        }
        return conf;
    }

    static IRI getIRI(OWLOntology ont) {
        return ont.getOntologyID().getOntologyIRI().orElseThrow(AssertionError::new);
    }

    private static Ontology createFamilyPeopleModelUsingONTAPI(OntologyManager manager, String ns) {
        Ontology family = (Ontology) load(manager, FAMILY);
        Ontology people = (Ontology) load(manager, PEOPLE);

        String familyNS = getIRI(family).getIRIString() + "#";
        String peopleNS = getIRI(people).getIRIString() + "#";

        Ontology res = manager.createOntology();
        OntModel ont = res.asGraphModel().setNsPrefix("p", peopleNS).setNsPrefix("f", familyNS).setNsPrefix("t", ns);
        ont.addImport(family.asGraphModel().addImport(people.asGraphModel()));

        OntClass foremother = ont.createOntClass(ns + "foremother");
        OntClass super_bus_company = ont.createOntClass(ns + "super_bus_company");
        super_bus_company.addSuperClass(ont.getOntClass(peopleNS + "bus_company"));
        foremother.addEquivalentClass(ont.createObjectIntersectionOf(ont.getOntClass(familyNS + "Woman"),
                ont.createObjectSomeValuesFrom(ont.getObjectProperty(familyNS + "isForemotherOf"),
                        ont.getOntClass(familyNS + "Person"))));
        OntIndividual.Named i1 = ont.getOntClass(familyNS + "Foremother").createIndividual(ns + "Eva");
        OntIndividual.Anonymous i2 = foremother.createIndividual();
        i2.addComment("This is Eva");

        OntDataProperty dp = ont.getDataProperty(familyNS + "alsoKnownAs");
        i1.addAssertion(dp, ont.createLiteral("Eve"));
        i2.addAssertion(dp, ont.createLiteral("Eve"));
        return res;
    }

    private static OWLOntology createFamilyPeopleModelUsingOWLAPI(OWLOntologyManager manager, String ns) {
        OWLOntology family = load(manager, FAMILY);
        OWLOntology people = load(manager, PEOPLE);
        OWLOntology res;
        try {
            res = manager.createOntology();
        } catch (OWLOntologyCreationException e) {
            return Assertions.fail(e);
        }
        IRI peopleIRI = getIRI(people);
        IRI familyIRI = getIRI(family);
        String familyNS = familyIRI.getIRIString() + "#";
        String peopleNS = peopleIRI.getIRIString() + "#";

        OWLDataFactory df = manager.getOWLDataFactory();
        OWLClass foremother = df.getOWLClass(ns + "foremother");
        OWLClass super_bus_company = df.getOWLClass(ns + "super_bus_company");
        OWLNamedIndividual i1 = df.getOWLNamedIndividual(ns + "Eva");
        OWLAnonymousIndividual i2 = df.getOWLAnonymousIndividual();
        OWLDataProperty dp = df.getOWLDataProperty(familyNS + "alsoKnownAs");

        manager.applyChange(new AddImport(family, df.getOWLImportsDeclaration(peopleIRI)));
        manager.applyChange(new AddImport(res, df.getOWLImportsDeclaration(familyIRI)));

        res.add(df.getOWLDeclarationAxiom(foremother));
        res.add(df.getOWLDeclarationAxiom(super_bus_company));
        res.add(df.getOWLSubClassOfAxiom(super_bus_company, df.getOWLClass(peopleNS + "bus_company")));
        res.add(df.getOWLEquivalentClassesAxiom(foremother,
                df.getOWLObjectIntersectionOf(df.getOWLClass(familyNS + "Woman"),
                        df.getOWLObjectSomeValuesFrom(df.getOWLObjectProperty(familyNS + "isForemotherOf"),
                                df.getOWLClass(familyNS + "Person")))));
        res.add(df.getOWLDeclarationAxiom(i1));
        res.add(df.getOWLClassAssertionAxiom(df.getOWLClass(familyNS + "Foremother"), i1));
        res.add(df.getOWLClassAssertionAxiom(foremother, i2));
        res.add(df.getOWLAnnotationAssertionAxiom(i2, df.getRDFSComment("This is Eva")));
        res.add(df.getOWLDataPropertyAssertionAxiom(dp, i1, df.getOWLLiteral("Eve")));
        res.add(df.getOWLDataPropertyAssertionAxiom(dp, i2, df.getOWLLiteral("Eve")));

        if (res instanceof Ontology) { // for debug
            ((Ontology) res).asGraphModel().setNsPrefix("p", peopleNS).setNsPrefix("f", familyNS).setNsPrefix("t", ns);
        }
        return res;
    }

    public OWLOntology fetch(OWLOntologyManager manager) {
        return fetch(manager, createConfig(manager));
    }

    OWLOntology fetch(OWLOntologyManager manager, OWLOntologyLoaderConfiguration conf) {
        try {
            return manager.loadOntologyFromOntologyDocument(getDocumentSource(), conf);
        } catch (OWLOntologyCreationException e) {
            throw new AssertionError(e);
        }
    }

    public OWLOntologyDocumentSource getDocumentSource() {
        return new FileDocumentSource(file.toFile(), getDocumentFormat());
    }

    public OWLDocumentFormat getDocumentFormat() {
        return format.createOwlFormat();
    }

    public String getURI() {
        return uri;
    }

    public String getNS() {
        return uri + "#";
    }

    public Path getFile() {
        return file;
    }

}
