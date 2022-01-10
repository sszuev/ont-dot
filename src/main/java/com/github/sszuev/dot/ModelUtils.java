package com.github.sszuev.dot;

import com.github.owlcs.ontapi.jena.model.OntStatement;
import org.apache.jena.rdf.model.RDFNode;

/**
 * Created by @ssz on 09.01.2022.
 */
public class ModelUtils {

    public static boolean testStatement(OntStatement statement, Class<? extends RDFNode> subjectOrObject) {
        return testStatement(statement, subjectOrObject, subjectOrObject);
    }

    public static boolean testStatement(OntStatement statement,
                                         Class<? extends RDFNode> subject,
                                         Class<? extends RDFNode> object) {
        return statement.getSubject().canAs(subject) && statement.getObject().canAs(object);
    }
}
