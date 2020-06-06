package com.jszczepankiewicz.s3.presigned;

import com.amazonaws.auth.SdkClock;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.time.Instant.now;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.logging.Logger.getLogger;


/**
 * Result of createPresignedPost
 * https://boto3.amazonaws.com/v1/documentation/api/latest/guide/s3-presigned-urls.html#generating-a-presigned-url-to-upload-a-file
 */
public class PresignedPostRequestForm {

    private final static Logger LOG = getLogger(PresignedPostRequestForm.class.getName());

    private final String url;
    private final Map<String, String> formFields;


    private PresignedPostRequestForm(Builder builder) {

        this.url = builder.url;
        this.formFields = builder.fields;

    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getFormFields() {
        return formFields;
    }

    public static class Builder {
        private String url;
        private Map<String, String> fields;
        private String region;
        private String bucket;
        private String key;
        private int expiresInSeconds = 60 * 60; // 1hr by default
        private Optional<Conditions> conditions = empty();
        private String awsSecretAccessKey;
        private String awsAccessKeyId;

        public Builder(String region, String bucket, String key) {
            this.bucket = bucket;
            this.key = key;
            this.region = region;
        }

        public Builder withCredentialsFromAwsNamedProfile(String profileName){
            ProfileCredentialsProvider credsProvider = new ProfileCredentialsProvider(profileName);
            this.awsAccessKeyId = credsProvider.getCredentials().getAWSAccessKeyId();
            this.awsSecretAccessKey = credsProvider.getCredentials().getAWSSecretKey();
            return this;
        }

        public Builder withAwsCredentials(String awsAccessKeyId, String awsSecretAccessKey){
            this.awsAccessKeyId = awsAccessKeyId;
            this.awsSecretAccessKey = awsSecretAccessKey;
            return this;
        }

        public Builder withConditions(Conditions conditions) {
            this.conditions = of(conditions);
            return this;
        }

        public Builder withExpiresInSeconds(int seconds) {
            this.expiresInSeconds = seconds;
            return this;
        }

        public PresignedPostRequestForm build() {

            if(awsAccessKeyId == null || awsSecretAccessKey == null){
                throw new IllegalArgumentException("Credentials required by not provided");
            }

            Conditions cond = conditions.orElseGet(() -> new Conditions.Builder().build());

            Instant requestNow = now();
            Duration gap = Duration.ofSeconds(expiresInSeconds);
            Instant expireAt = requestNow.plus(gap);

            LOG.info(format("Generating request at %s, expiration: %s, region: %s, bucket: %s, key: %s", requestNow, expireAt, region, bucket, key));

            this.url = format("https://%s.s3.%s.amazonaws.com", bucket, region);
            FormProvider form = new SignatureDecorator(new UploadRequest(requestNow, awsAccessKeyId, region, key), cond, expireAt, awsSecretAccessKey, bucket);

            this.fields = form.getFields();
            return new PresignedPostRequestForm(this);
        }
    }
}
