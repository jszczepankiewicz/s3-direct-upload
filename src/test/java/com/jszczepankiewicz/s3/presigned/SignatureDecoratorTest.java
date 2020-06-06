package com.jszczepankiewicz.s3.presigned;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SignatureDecoratorTest {

    @Test
    void shouldGenerateSignature(){

        //  when
        String signature = SignatureDecorator.calculate("some", "20200520", "SomeSecretKey", "us-east-1");

        //  then
        assertThat(signature).isEqualTo("53e2e39cf841f45fa2694cb7b52b3efa39f324321d6ef6b66fbf7b0c5cdddb3a");
    }

}