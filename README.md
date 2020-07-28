<p align="center"> 
<img src="https://raw.githubusercontent.com/jszczepankiewicz/s3-direct-upload/master/docs/s3-direct-upload-logo.png" alt="Logo" width="600"/>
</p>
S3 direct upload is java library to safely upload files directly to AWS s3 using form uploads without sharing with http client aws credentials. It helps creating forms without exposing sensitive data in url.


![GitHub](https://img.shields.io/github/license/jszczepankiewicz/s3-direct-upload?label=business%20friendly%20license&style=for-the-badge)
![Travis (.org)](https://img.shields.io/travis/jszczepankiewicz/s3-direct-upload?label=travis%20ci%20build&style=for-the-badge)

## Prerequisites
- at least java 8
- aws_access_key_id / aws_secret_access_key for user / role that has write priviledges to S3 bucket where files should be stored provided explicitely (see examples)
- or configured named profile file so that library can load the credentials directly from it

## Examples
### Requests without restrictions
```java

//	method 1 (assumes AWS credentials configured using named profiles)
PresignedPostRequestForm form = new PresignedPostRequestForm.Builder(REGION,BUCKET, "sample2.jpg")
                .withCredentialsFromAwsNamedProfile("myprofile")
                .withExpiresInSeconds(30)
                .build();
				
//	method 2 (assumes providing AWS accesskey / secret access key i.e. taken from assumesRole from sts service (recommended in production)
PresignedPostRequestForm form = new PresignedPostRequestForm.Builder(REGION,BUCKET, "sample2.jpg")
                .withAwsCredentials("PUT_HERE_ACCESS_KEY", "PUT_HERE_SECRET_ACESS_KEY")
                .withExpiresInSeconds(30)
                .build();
```
### Full end to end tests using example http client to upload file directly to S3 and named aws profiles
```java
package com.jszczepankiewicz.s3.presigned;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.logging.Logger.getLogger;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * End to end tests disabled by default. If you want to execute them make sure you properly configured:
 * - roles on AWS3 for upload
 * - named profile with corresponding credentials (below code assumes existence of 'upload_presigned' named profile in ~/.aws/credentials
 */
//@Disabled
public class PresignedPostRequestFormEndToEndTest {

    private final static Logger LOG = getLogger(PresignedPostRequestFormEndToEndTest.class.getName());

    private static final String BUCKET = "your-bucket-name";
    private static final String REGION = "eu-central-1";
    private static final String NAMED_PROFILE = "upload_presigned";
    private static final int S1_SIZE = 59114;
    private static final String S1_FILENAME = "sample1.jpg";
    private static final String S1_CONTENT_TYPE = "image/jpeg";
    private static final int S2_SIZE_BYTES = 1;
    private static final String S2_CONTENT_TYPE = "text/plain";

    private String getS2Keyname(){
        return UUID.randomUUID().toString() + ".txt";
    }

    @Test
    void shouldUploadTextFileWithConditionOfContentLength(){

        //  given
        String key = getS2Keyname();
        PresignedPostRequestForm form = new PresignedPostRequestForm.Builder(REGION,BUCKET, key)
                .withCredentialsFromAwsNamedProfile(NAMED_PROFILE)
                .withExpiresInSeconds(30)
                .withConditions(new Conditions.Builder().ofContentLengthBetween(S2_SIZE_BYTES, S2_SIZE_BYTES + 100).build())
                .build();

        dumpOutcome(form);
        //  when
        HttpFormClient.Response response = HttpFormClient.uploadForm(form.getUrl(), form.getFormFields(),
                getFileStream2("example.txt"), key, S2_CONTENT_TYPE);

        //  then
        assertThat(response.getHttpCode()).isEqualTo(HTTP_NO_CONTENT);
    }

    @Test
    void shouldUploadTextFileWithConditionOfExactContentType(){
        //  given
        String key = getS2Keyname();
        PresignedPostRequestForm form = new PresignedPostRequestForm.Builder(REGION,BUCKET, key)
                .withCredentialsFromAwsNamedProfile(NAMED_PROFILE)
                .withExpiresInSeconds(30)
                .withConditions(new Conditions.Builder().ofExactContentType(S2_CONTENT_TYPE).build())
                .build();

        dumpOutcome(form);
        //  when
        HttpFormClient.Response response = HttpFormClient.uploadForm(form.getUrl(), form.getFormFields(),
                getFileStream2("example.txt"), key, S2_CONTENT_TYPE);

        //  then
        assertThat(response.getHttpCode()).isEqualTo(HTTP_NO_CONTENT);
    }

    @Test
    void shouldUploadImageWithoutConditions() {
        //System.setProperty("javax.net.debug","all");

        //  given
        PresignedPostRequestForm form = new PresignedPostRequestForm.Builder(REGION,BUCKET, "sample2.jpg")
                .withCredentialsFromAwsNamedProfile(NAMED_PROFILE)
                .withExpiresInSeconds(30)
                .build();

        //dumpOutcome(form);

        //  when
        HttpFormClient.Response response = HttpFormClient.uploadForm(form.getUrl(), form.getFormFields(),
                getFileStream("sample1.jpg"), S1_FILENAME, S1_CONTENT_TYPE);

        //  then
        assertThat(response.getHttpCode()).isEqualTo(HTTP_NO_CONTENT);



    }

    private InputStream getFileStream2(String filename) {
        InputStream is = null;
        try {
            is = new FileInputStream(new File("c:\\temp\\example.txt"));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
        return is;
    }

    private InputStream getFileStream(String filename) {
        InputStream is = null;
        try {
            is = new FileInputStream(new File("c:\\dev\\sample1.jpg"));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
        return is;
    }

    private void dumpOutcome(PresignedPostRequestForm form){
        LOG.info("Dumping form: >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LOG.info("url: " + form.getUrl());
        for (Map.Entry<String, String> pair : form.getFormFields().entrySet()) {
            LOG.info("\t[" + pair.getKey() + "] => " + pair.getValue());
        }
        LOG.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

    }


}

```
## Troubleshooting
1. Getting http 403 when uploading object
1.1 Check your user has enough permissions to putObject (adjust bucket, key, region, body and profile):
```
aws s3api put-object --bucket my_bucket --key examplefile.jpg --body c:\examplefile.jpg  --profile upload_presigned  --region eu-central-1
```
