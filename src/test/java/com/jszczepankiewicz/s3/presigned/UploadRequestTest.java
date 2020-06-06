package com.jszczepankiewicz.s3.presigned;

import com.amazonaws.services.s3.transfer.Upload;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static java.time.Instant.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class UploadRequestTest {

    private static final Instant now = parse("2013-07-28T10:15:30.234Z");
    private static final String AWS_ACCESSKEY_ID = "accesskeysome";
    private static final String KEY = "/user/user1/photo1.jpg";
    private static final String REGION = "us-east-1";
    private static final String BUCKET = "mysuperduperbucket";

    @Test
    void shouldFormatAwsDate(){

        //  when
        UploadRequest req = standardRequest();
        String conv = req.toAwsDate(Instant.now());

        //  then
        assertThat(conv).hasSize(16);
    }

    @Test
    void shouldFormatDateFromInstant(){

        //  when
        UploadRequest req = standardRequest();

        //  then
        assertThat(req.getFields()).contains(entry("x-amz-date", "20130728T000000Z"));
    }

    @Test
    void shouldFormatCredential(){

        //  when
        UploadRequest req = standardRequest();

        //  then
        assertThat(req.getFields()).contains(entry("x-amz-credential", "accesskeysome/20130728/us-east-1/s3/aws4_request"));
    }

    private UploadRequest standardRequest(){
        return new UploadRequest(now, AWS_ACCESSKEY_ID, REGION, KEY);
    }
}