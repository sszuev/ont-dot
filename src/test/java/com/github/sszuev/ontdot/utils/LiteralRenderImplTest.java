package com.github.sszuev.ontdot.utils;

import com.github.sszuev.tests.utils.ModelData;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Created by @ssz on 23.01.2022.
 */
public class LiteralRenderImplTest {

    @Test
    public void testSplit1() {
        String txt = "dufqsPztAGEChNIGFSWcHBUIIPeyFBJprAIGPxRyVVODlTEOjFWifoT" +
                "HNcvmgHTpnXTbDSBhDOsmmYBMsircZorWPMnTmPIDvBFNdVJxEFogKdqXqBzpCeMM";
        String[] actual = LiteralRendererImpl.split(txt, 42, 12);
        Assertions.assertEquals(3, actual.length);
        Assertions.assertEquals(42, actual[0].length());
        Assertions.assertEquals(42, actual[1].length());
        Assertions.assertEquals(36, actual[2].length());
        Assertions.assertEquals(txt, String.join("", actual));
    }

    @Test
    public void testSplit2() {
        String txt = "dBhWcfOugPQIsycRcxHwzSQHbnAdZzXMTltsUYextTkzkLckSahnRPT" +
                "bjlKPvOaPUdCagulNLxQsOxiseFvdRAOkSlFKqguEGTmYUQxyKrVreckyzEbwZoKW";
        String[] actual = LiteralRendererImpl.split(txt, 42, 2);
        Assertions.assertEquals(2, actual.length);
        Assertions.assertEquals(42, actual[0].length());
        Assertions.assertEquals(42, actual[1].length());
        Assertions.assertTrue(actual[1].endsWith("..."));
        Assertions.assertTrue(txt.startsWith(actual[0] + actual[1].substring(0, actual[1].length() - 3)));
    }

    @Test
    public void testSplit3() {
        String txt = "X".repeat(1200);
        String[] actual = LiteralRendererImpl.split(txt, 42, 1);
        Assertions.assertEquals(1, actual.length);
        Assertions.assertEquals(42, actual[0].length());
        Assertions.assertTrue(actual[0].endsWith("..."));
        Assertions.assertTrue(txt.startsWith(actual[0].substring(0, actual[0].length() - 3)));
    }

    @Test
    public void testSplit4() {
        String txt = RandomStringUtils.randomAlphabetic(1200);
        String[] actual = LiteralRendererImpl.split(txt, 42, 1000);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < actual.length - 1; i++) {
            String a = actual[i];
            sb.append(a);
            Assertions.assertEquals(42, a.length());
        }
        sb.append(actual[actual.length - 1]);
        Assertions.assertEquals(txt, sb.toString());
    }

    @Test
    public void testNormalize() {
        String comment = ModelData.FOOD.ont().asGraphModel().getID().getComment();
        Assumptions.assumeTrue(comment.startsWith("\n"));
        String res = LiteralRendererImpl.normalize(comment);
        Assertions.assertFalse(res.contains("\n"));
        Assertions.assertEquals(128, res.length());
    }
}
