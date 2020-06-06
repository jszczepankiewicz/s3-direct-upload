package com.jszczepankiewicz.s3.presigned;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;
import static java.util.Locale.US;

public class UploadRequest implements FormProvider{

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd")
            .withLocale(US)
            .withZone(UTC);
    private final static DateTimeFormatter AWS_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'000000X")
            .withLocale(US)
            .withZone(UTC);

    private Instant date;
    private String awsAccessKeyId;
    private String region;
    private String key;
    private String awsDate;

    UploadRequest(Instant date, String awsAccessKeyId, String region, String key) {
        this.date = date;
        this.region = region;
        this.awsAccessKeyId = awsAccessKeyId;
        this.key = key;
    }

    String getAwsDate() {
        return awsDate;
    }

    public Map<String, String> getFields() {
        Map<String, String> fields = new HashMap<>();
        fields.put("key", key);
        fields.put("x-amz-algorithm", "AWS4-HMAC-SHA256");
        this.awsDate = toAwsDate(date);
        fields.put("x-amz-date", awsDate);
        fields.put("x-amz-credential", String.format("%s/%s/%s/s3/aws4_request", awsAccessKeyId, DATE_FORMAT.format(date), region));
        return fields;
    }


    String toAwsDate(Instant instant) {
        return AWS_DATE_FORMATTER.format(instant);
    }

    String getRegion(){
        return region;
    }


}
