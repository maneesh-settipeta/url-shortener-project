package com.assignment.urlshortener.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class Base62CodeGeneratorTest {

    @Test
    void generatesExpectedLengthAndAlphabet() {
        Base62CodeGenerator generator = new Base62CodeGenerator();
        String code = generator.generate(7);

        assertThat(code).hasSize(7).matches("[0-9A-Za-z]{7}");
    }

    @Test
    void producesHighUniquenessAcrossSample() {
        Base62CodeGenerator generator = new Base62CodeGenerator();
        Set<String> values = new HashSet<>();
        for (int i = 0; i < 10_000; i++) {
            values.add(generator.generate(7));
        }
        assertThat(values).hasSize(10_000);
    }
}
