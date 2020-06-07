![GitHub](https://img.shields.io/github/license/jszczepankiewicz/s3-direct-upload?label=business%20friendly%20license&style=for-the-badge)
![Travis (.org)](https://img.shields.io/travis/jszczepankiewicz/s3-direct-upload?label=travis%20ci%20build&style=for-the-badge)

## Examples
### No restrictions
```java
PresignedPostRequestForm form = new PresignedPostRequestForm.Builder(REGION,BUCKET, "sample2.jpg")
                .withCredentialsFromAwsNamedProfile("myprofile")
                .withExpiresInSeconds(30)
                .build();
```
## Troubleshooting
1. Getting http 403 when uploading object
1.1 Check your user has enough permissions to putObject (adjust bucket, key, region, body and profile):
```
aws s3api put-object --bucket my_bucket --key examplefile.jpg --body c:\examplefile.jpg  --profile upload_presigned  --region eu-central-1
```