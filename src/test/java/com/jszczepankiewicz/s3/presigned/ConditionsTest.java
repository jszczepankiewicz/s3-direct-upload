package com.jszczepankiewicz.s3.presigned;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.Instant.parse;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ConditionsTest {

    private static final Instant expiration = parse("2020-05-21T19:40:10.041288400Z");
    private static final String BUCKET = "somebucket";

    @Test
    void shouldBuildPolicy() {

        //  given
        Conditions conditions = new Conditions.Builder().ofExactContentType("image/jpeg").ofContentLengthBetween(10, 1024 * 1024).build();

        //  when
        String policyPlain = conditions.asJsonPolicy(expiration, standardFields(), BUCKET);
        String policyEncoded = conditions.asBase64JsonPolicy(expiration, standardFields(), BUCKET);

        //  then
        assertThat(policyPlain).isEqualTo(standardPlainPolicy());
        assertThat(policyEncoded).isEqualTo(standardEncodedPolicy());
    }

    private Map<String, String> standardFields() {
        Map<String, String> map = Stream.of(new String[][]{
                {"key", "userfiles/image.jpg"},
                {"some-random-field", "this should not be ignored"},
                {"policy", "thisispolicyfieldthatshouldbeignored"},
                {"x-ignore-abc", "to be ignored"},
                {"file", "to be ignored"},
                {"x-amz-signature", "to be ignored"},
        }).collect(toMap(data -> data[0], data -> data[1]));
        return map;
    }

    private String standardEncodedPolicy(){
        return "ewogICJleHBpcmF0aW9uIjogIjIwMjAtMDUtMjFUMTk6NDA6MTAuMDAwWiIsCiAgImNvbmRpdGlvbnMiOiBbCiAgIFsiY29udGVudC1sZW5ndGgtcmFuZ2UiLDEwLDEwNDg1NzZdLAogICBbImVxIiwgIiRDb250ZW50LVR5cGUiLCAiaW1hZ2UvanBlZyJdLAogICB7InNvbWUtcmFuZG9tLWZpZWxkIjogInRoaXMgc2hvdWxkIG5vdCBiZSBpZ25vcmVkIn0sCiAgIHsia2V5IjogInVzZXJmaWxlcy9pbWFnZS5qcGcifSwKICAgeyJidWNrZXQiOiAic29tZWJ1Y2tldCJ9LAogIF0KfQ==";
    }

    private String standardPlainPolicy() {
        return "{\n" +
                "  \"expiration\": \"2020-05-21T19:40:10.000Z\",\n" +
                "  \"conditions\": [\n" +
                "   [\"content-length-range\",10,1048576],\n" +
                "   [\"eq\", \"$Content-Type\", \"image/jpeg\"],\n" +
                "   {\"some-random-field\": \"this should not be ignored\"},\n" +
                "   {\"key\": \"userfiles/image.jpg\"},\n" +
                "   {\"bucket\": \"somebucket\"},\n" +
                "  ]\n" +
                "}";
    }

}