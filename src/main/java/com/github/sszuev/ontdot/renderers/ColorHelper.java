package com.github.sszuev.ontdot.renderers;

import com.github.owlcs.ontapi.jena.model.*;
import com.github.sszuev.ontdot.api.DOTSetting;
import com.github.sszuev.ontdot.api.RenderOptions;

/**
 * Created by @ssz on 16.01.2022.
 */
public class ColorHelper {

    public static String classExpressionFillcolor(RenderOptions config, OntClass clazz) {
        if (clazz.canAs(OntClass.ComponentRestrictionCE.class)) {
            return config.getString(DOTSetting.STRING_COMPONENT_RESTRICTION_COLOR);
        }
        if (clazz.canAs(OntClass.ComponentsCE.class)) {
            return config.getString(DOTSetting.STRING_COMPONENTS_CE_COLOR);
        }
        if (clazz.canAs(OntClass.ComplementOf.class)) {
            return config.getString(DOTSetting.STRING_COMPLEMENT_CE_COLOR);
        }
        // TODO:
        throw new IllegalStateException("For class " + clazz);
    }

    public static String entityColor(RenderOptions config, OntEntity node) {
        if (node.canAs(OntClass.Named.class)) {
            return config.classColor();
        }
        if (node.canAs(OntIndividual.Named.class)) {
            return config.individualColor();
        }
        if (node.canAs(OntDataRange.Named.class)) {
            return config.datatypeColor();
        }
        return propertyColor(config, node.as(OntProperty.class));
    }

    public static String propertyColor(RenderOptions config, OntProperty sub) {
        if (sub.canAs(OntAnnotationProperty.class)) {
            return config.getString(DOTSetting.STRING_ANNOTATION_PROPERTY_COLOR);
        }
        if (sub.canAs(OntDataProperty.class)) {
            return config.getString(DOTSetting.STRING_DATA_PROPERTY_COLOR);
        }
        if (sub.canAs(OntObjectProperty.class)) {
            return config.getString(DOTSetting.STRING_OBJECT_PROPERTY_COLOR);
        }
        throw new IllegalStateException();
    }
}
