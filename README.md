[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

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