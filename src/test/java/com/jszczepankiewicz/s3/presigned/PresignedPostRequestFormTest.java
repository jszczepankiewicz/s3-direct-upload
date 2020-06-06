package com.jszczepankiewicz.s3.presigned;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.*;

/**
 * https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-HTTPPOSTForms.html
 */
class PresignedPostRequestFormTest {

    private static final String AWS_ACCESSKEY_ID = "accesskeysome";
    private static final String AWS_SECRET_ACCESS_KEY = "SECRETACCESS";
    private static final String REGION = "east-1";
    private static final String BUCKET = "mysuperduperbucket";
    private static final String KEY = "somekey";

    @Test
    void shouldConstructFormUrlWithVirtualHostFromBucketName(){

        //  when
        PresignedPostRequestForm form = standard();

        //  then
        assertThat(form.getUrl()).isEqualTo("https://mysuperduperbucket.s3.east-1.amazonaws.com");
    }

    @Test
    void shouldContainV4SigningAlgorithm(){

        //  when
        PresignedPostRequestForm form = standard();

        //  then
        assertThat(form.getFormFields()).contains(entry("x-amz-algorithm", "AWS4-HMAC-SHA256"));
    }

    @Test
    void shouldContainDate(){

        //  when
        PresignedPostRequestForm form = standard();

        //  then
        assertThat(form.getFormFields()).containsKey("x-amz-date");
    }

    @Test
    void shouldContainKey(){

        //  when
        PresignedPostRequestForm form = standard();

        //  then
        assertThat(form.getFormFields()).contains(entry("key", KEY));
    }

    @Test
    void shouldContainCredentials(){

        //  when
        PresignedPostRequestForm form = standard();

        //  then
        assertThat(form.getFormFields()).containsKey("x-amz-credential");
    }

    @Test
    void shouldContainPolicyAndSignature(){

        //  when
        PresignedPostRequestForm form = standard();

        //  then
        assertThat(form.getFormFields()).containsKey("X-Amz-Signature").containsKey("Policy");
    }

    private PresignedPostRequestForm standard(){
        return new PresignedPostRequestForm.Builder(REGION, BUCKET, KEY).withAwsCredentials(AWS_ACCESSKEY_ID, AWS_SECRET_ACCESS_KEY).build();
    }


}