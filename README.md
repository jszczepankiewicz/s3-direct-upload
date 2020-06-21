![GitHub](https://img.shields.io/github/license/jszczepankiewicz/s3-direct-upload?label=business%20friendly%20license&style=for-the-badge)
![Travis (.org)](https://img.shields.io/travis/jszczepankiewicz/s3-direct-upload?label=travis%20ci%20build&style=for-the-badge)
<p align="center"> 
<img src="https://raw.githubusercontent.com/jszczepankiewicz/s3-direct-upload/master/docs/s3-direct-upload-logo.png" alt="Logo" width="600"/>
</p>
S3 direct upload is java library to safely upload files directly to AWS s3 using form uploads without sharing with http client aws credentials. It helps creating forms without exposing sensitive data in url.

## Prerequisites
- at least java 8
- aws_access_key_id / aws_secret_access_key for user / role that has write priviledges to S3 bucket where files should be stored provided explicitely (see examples)
- or configured named profile file so that library can load the credentials directly from it

## Examples
### No restrictions
```java

//	method 1 (assumes AWS credentials configured using named profiles)
PresignedPostRequestForm form = new PresignedPostRequestForm.Builder(REGION,BUCKET, "sample2.jpg")
                .withCredentialsFromAwsNamedProfile("myprofile")
                .withExpiresInSeconds(30)
                .build();
				
//	method 2 (assumes providing AWS				
```
## Troubleshooting
1. Getting http 403 when uploading object
1.1 Check your user has enough permissions to putObject (adjust bucket, key, region, body and profile):
```
aws s3api put-object --bucket my_bucket --key examplefile.jpg --body c:\examplefile.jpg  --profile upload_presigned  --region eu-central-1
```