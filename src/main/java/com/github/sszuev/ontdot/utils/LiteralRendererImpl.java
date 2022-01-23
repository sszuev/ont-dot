package com.github.sszuev.ontdot.utils;

import com.github.owlcs.ontapi.jena.vocabulary.XSD;
import com.github.sszuev.ontdot.api.LiteralRenderer;
import com.github.sszuev.ontdot.api.RenderOptions;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.shared.PrefixMapping;

/**
 * Created by @ssz on 22.01.2022.
 */
public class LiteralRendererImpl implements LiteralRenderer {
    @Override
    public String print(Literal value, RenderOptions config, PrefixMapping pm) {
        return ModelUtils.print(value, true, pm);
    }

    @Override
    public String printNonNegativeInteger(long value, RenderOptions config, PrefixMapping pm) {
        if (value <= 0) {
            throw new IllegalArgumentException();
        }
        return ModelUtils.printLiteral(String.valueOf(value), false, pm, XSD.nonNegativeInteger.getURI(), null);
    }
}
