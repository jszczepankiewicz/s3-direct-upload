package com.jszczepankiewicz.aws.uploadsigned;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.junit.jupiter.api.Test;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import java.net.URL;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

import static com.amazonaws.HttpMethod.PUT;

public class PresignedTest  {

    //  adjust this to your profile name for AWS S3 client from ~/.aws/credentials
    private static final String AWS_NAMED_PROFILE_NAME = "default";
    private static final String FAKE_AWS_ACCESS_KEY_ID = "fakeaccesskeyid";
    private static final String FAKE_AWS_SECRET_KEY = "fakesecretkey";

    private static final String S3_BUCKET = "com.jszczepankiewicz.xyz";
    private static final String S3_KEY = "key.bucket-prefix";
    private static final Regions REGION = Regions.US_EAST_2;

    private AWSCredentialsProvider realCredentials;
    private AWSCredentialsProvider fakeCredentials;

    private AmazonS3 disconnectedS3Client;
    private AmazonS3 connectedS3Client;

    @Test
    void shouldGeneratePresignedUrl() {
        //  given
        AmazonS3 client = disconnectedS3Client();
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(S3_BUCKET, S3_KEY)
                .withMethod(PUT)
                .withExpiration(oneHourAhead());

        //  when
        URL url = client.generatePresignedUrl(generatePresignedUrlRequest);

        //  then
        assertThat(url).hasProtocol("https")
                .hasHost("com.jszczepankiewicz.xyz.s3.us-east-2.amazonaws.com")
                .hasPath("/key.bucket-prefix")
                .hasParameter("X-Amz-Algorithm", "AWS4-HMAC-SHA256")
                .hasParameter("X-Amz-SignedHeaders", "host")
                .hasParameter("X-Amz-Date")
                .hasParameter("X-Amz-Expires")
                .hasParameter("X-Amz-Credential")
                .hasParameter("X-Amz-Signature");
        String fullquery = url.getQuery();
        assertThat(fullquery.substring(fullquery.indexOf("X-Amz-Credential=") + "X-Amz-Credential=".length())).startsWith(FAKE_AWS_ACCESS_KEY_ID);
    }

    private Date oneHourAhead() {
        java.util.Date expiration = new java.util.Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 60;
        expiration.setTime(expTimeMillis);
        return expiration;
    }

    private AWSCredentialsProvider realAWSCredentials() {
        if (realCredentials == null) {
            realCredentials = new ProfileCredentialsProvider(AWS_NAMED_PROFILE_NAME);
        }
        return realCredentials;
    }

    private AWSCredentialsProvider fakeAWSCredentials() {
        if (fakeCredentials == null) {
            AWSCredentials creds = new BasicAWSCredentials(FAKE_AWS_ACCESS_KEY_ID, FAKE_AWS_SECRET_KEY);
            fakeCredentials = new AWSStaticCredentialsProvider(creds);
        }
        return fakeCredentials;
    }

    private AmazonS3 disconnectedS3Client() {

        if (disconnectedS3Client == null) {
            disconnectedS3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(fakeAWSCredentials())
                    .withRegion(REGION)
                    .build();
        }

        return disconnectedS3Client;
    }

    private AmazonS3 connectedS3Client() {

        if (connectedS3Client == null) {
            connectedS3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(realAWSCredentials())
                    .withRegion(REGION)
                    .build();
        }

        return connectedS3Client;
    }
}
