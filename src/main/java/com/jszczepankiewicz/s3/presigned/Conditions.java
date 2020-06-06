package com.jszczepankiewicz.s3.presigned;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static java.time.ZoneId.systemDefault;
import static java.util.Locale.US;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.logging.Logger.getLogger;

public class Conditions {

    private final static Logger LOG = getLogger(Conditions.class.getName());
    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.000X")
            .withLocale(US)
            .withZone(ZoneOffset.UTC);

    private final Optional<String> exactContentType;
    private final Optional<Integer> minSizeInBytes;
    private final Optional<Integer> maxSizeInBytes;
    private boolean convertConditionsToFields;

    private Conditions(Builder builder) {
        this.exactContentType = builder.exactContentType;
        this.minSizeInBytes = builder.minSizeInBytes;
        this.maxSizeInBytes = builder.maxSizeInBytes;
        this.convertConditionsToFields = builder.convertConditionsToFields;
    }

    public boolean isConvertConditionsToFields() {
        return convertConditionsToFields;
    }

    public Optional<String> getExactContentType() {
        return exactContentType;
    }

    private String esc(String value) {
        return value;
    }

    String asJsonPolicy(Instant expiration, Map<String, String> fields, String bucketName) {
        StringBuilder json = new StringBuilder();
        json.append("{\n  \"expiration\": \"");
        json.append(FORMATTER.format(expiration));
        json.append("\",\n" + "  \"conditions\": [");

        if (minSizeInBytes.isPresent()) {
            json.append("\n   [\"content-length-range\"");
            json.append(",");
            json.append(minSizeInBytes.get());
            json.append(",");
            json.append(maxSizeInBytes.get());
            json.append("],");
        }

        if (exactContentType.isPresent()) {
            json.append("\n   [\"eq\", \"$Content-Type\", \"");
            json.append(esc(exactContentType.get()));
            json.append("\"],");
        }


        for (Map.Entry<String, String> pair : fields.entrySet()) {
            String k = pair.getKey();
            String v = pair.getValue();

            if (k.equalsIgnoreCase("x-amz-signature") || k.equalsIgnoreCase("file") || k.equalsIgnoreCase("policy") || k.toLowerCase().startsWith("x-ignore-")) {
                //  omitting specific fields according with https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-HTTPPOSTConstructPolicy.html
                continue;
            }

            json.append("\n   {\"");
            json.append(esc(k));
            json.append("\": \"");
            json.append(esc(v));
            json.append("\"},");
        }

        json.append("\n   {\"");
        json.append("bucket");
        json.append("\": \"");
        json.append(esc(bucketName));
        json.append("\"},");

        json.append("\n  ]\n}");
        String policy = json.toString();
        LOG.info("Built policy: \n" + policy);
        return policy;
    }

    public String asBase64JsonPolicy(Instant expiration, Map<String, String> fields, String bucketName) {
        try {
            return Base64.getEncoder().encodeToString(asJsonPolicy(expiration, fields, bucketName).getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unexpected lack of support for utf-8", e);
        }
    }

    public static class Builder {

        private Optional<String> exactContentType = empty();
        private Optional<Integer> minSizeInBytes = empty();
        private Optional<Integer> maxSizeInBytes = empty();

        private boolean convertConditionsToFields = true;

        public Builder doIncludeConditionsInFields(boolean doIt) {
            this.convertConditionsToFields = doIt;
            return this;
        }

        public Builder ofExactContentType(String mimeType) {
            this.exactContentType = of(mimeType);
            return this;
        }

        public Builder ofContentLengthBetween(int minSizeInBytes, int maxSizeInBytes){
            this.minSizeInBytes = of(minSizeInBytes);
            this.maxSizeInBytes = of(maxSizeInBytes);
            return this;
        }

        public Conditions build() {
            return new Conditions(this);
        }
    }
}
