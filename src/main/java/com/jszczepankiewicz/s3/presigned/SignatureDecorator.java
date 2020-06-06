package com.jszczepankiewicz.s3.presigned;

import org.apache.commons.codec.digest.HmacUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.valueOf;
import static org.apache.commons.codec.digest.HmacAlgorithms.HMAC_SHA_256;

/**
 * @link https://docs.aws.amazon.com/AmazonS3/latest/API/sig-v4-authenticating-requests.html
 */
class SignatureDecorator implements FormProvider {

    private UploadRequest request;
    private Conditions conditions;
    private Instant expireAt;
    private String awsSecretKey;
    private String bucket;

    static String calculate(String encodedPolicy, String date, String secretKey, String region) {

        byte[] dateKey = new HmacUtils(HMAC_SHA_256, "AWS4" + secretKey)
                .hmac(date);
        byte[] dateRegionKey = new HmacUtils(HMAC_SHA_256, dateKey).hmac(region);
        byte[] dateRegionServiceKey = new HmacUtils(HMAC_SHA_256, dateRegionKey).hmac("s3");
        byte[] signingKey = new HmacUtils(HMAC_SHA_256, dateRegionServiceKey).hmac("aws4_request");

        return new HmacUtils(HMAC_SHA_256, signingKey).hmacHex(encodedPolicy);
    }

    public SignatureDecorator(UploadRequest request, Conditions conditions, Instant expireAt, String awsSecretKey, String bucket) {
        this.request = request;
        this.conditions = conditions;
        this.expireAt = expireAt;
        this.awsSecretKey = awsSecretKey;
        this.bucket = bucket;
    }

    @Override
    public Map<String, String> getFields() {
        Map<String, String> fields = new HashMap(request.getFields());

        String encodedPolicy = conditions.asBase64JsonPolicy(expireAt, fields, bucket);
        String signature = calculate(encodedPolicy, request.getAwsDate().substring(0, 8), awsSecretKey, request.getRegion());

        fields.put("X-Amz-Signature", signature);
        fields.put("Policy", encodedPolicy);

        if (conditions.isConvertConditionsToFields()) {
            if (conditions.getExactContentType().isPresent()) {
                fields.put("Content-Type", conditions.getExactContentType().get());
            }

            /*if(conditions.getExactSizeInBytes().isPresent()){
                fields.put("Content-Length", valueOf(conditions.getExactSizeInBytes().get()));
            }*/
        }

        return fields;
    }
}
