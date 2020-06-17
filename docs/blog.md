# Handling customer uploaded files in AWS
## The problem
Sooner of later majority of the projects in the cloud exposed to customers will need to provide functionality of allowing users to upload files that need to be processed and stored in the cloud. Typical usecases are i.e. profile image or some scans of documents uploaded from clients.

Organisations usually want to achieve following goals:
- uploads allowed only from authenticated and authorized users
- strict control on type of file, maximum size
- strongly regulated organisations (i.e. banking) want also ensure risk of accepting file infected by malware is under control

Which translates usually to following flow:
- authenticating client app to allow upload
- receiving the file and buffering in temp storage
- doing sanity check (checking file extension, content type, size)
- optionally execute anti-malware checks

Example architecture might look as following:
(( image with traditional architecture ))

The most common approach when dealing with traditional application would be to upload the files directly through the apps with web interface using form upload or api call and streaming. This approach althrough by design quite simple has multiple challenges like: 

- it uses costly network / cpu / ram resources for transferring files
- usually majority of the enterprises would preffer to offload all possible activities (like handling uploads) to maximize ROI on going cloud
- binary uploads might trigger false positive XSS body alerts in web application firewall solution and effectively
- securing binary uploads might be challenging since it per design has to allow big payloads which might be used as HTTP DDoS attack vector
- satisfying full cycle of transferring customer upload files to target destination (usually S3) might be challenging from HA perspective

## Options

Any other ways we could consider for that?
### The good old school
So in the old ways before http became so dominant typical platform for file transfer was FTP / SFTP. With sftp on AWS there are two options:
- hosting traditional sftp server on client infra (ec2/eks/beanstalk)
- using managed SFTP transfer managed by AWS

Since first option does not differ from our previous design and share majority of the problems we will try to focus on the latter option and take closer look at managed sftp at AWS. 

1. Relatively new service introduced in 2018 ( https://aws.amazon.com/blogs/aws/new-aws-transfer-for-sftp-fully-managed-sftp-service-for-amazon-s3/)
2. backed by S3, all file transfered to it land in S3 bucket for convenience
2. Can support multiple authentication schemes like PKI, Active Directory, LDAP
3. By default fully DNS driven and managed by AWS without VPC
4. if traditional network security is required (IP filtering, network filtering done by client) than there are two options possible:
4.1 using EIP with traditional SG driven security https://aws.amazon.com/premiumsupport/knowledge-center/sftp-enable-elastic-ip-addresses/ that can allow also public IP whitelisting from client side
4.2 hosting sftp privately in VPC

AWS Managed SFT seems good choice for traditional b2b integrations especially when there is need to meet traditional sftp driven workflow and network security over Public Internet with modern S3 driven storage. 
But if we got clients that are relying over http(s) sessions it might not be best choice. Let's try to check the next one option.

# Direct S3 upload
So let's revise one of our requirement. Majority of the applicatoins that will be hosted on S3 most likely will rely on storing the files in S3 which is most common approach on AWS. Now if we would  just be able to upload from client directly to S3. Let's evaluate that idea!

## first attempt
So we could potentially use direct api usage and put object into s3. So why not do it? There is often one good reason. Using api requires usage of native AWS credentials which are AWSACCESSKEYID and AWSECRETKEY. If we would have mobile application we do not want to provide static credentials assigned to some IAM user in our account. We would like to have temporal credentials obtained in authenticated sessions. We got now two ways to do it. 

## Using temporal aws credentials
If we are fine with providing temporal aws credentials for our clients and let them use aws api to interact with our infra (like s3) it is possible to utilise aws sts to get credentials. Than we could expose this through user friendly rest api to our clients. Using sts and native aws credentials brings big elasticity though has following characteristics that might be considred as drawbacks:
- limited ability to enforce specific file uploads, especially if we want to avoid constant updates to s3 bucket policies
- sharing native aws credentials might be still considered as opening bigger risks than action specific narrowed credentials
- enforce clients to use aws libraries or some wrappers on that
((image))

## using presigned requests
Now let's try to focus on last method which is not requiring using aws libraries by client. Aws offers two methods for that
- presigned urls
- form uploads

### Using presigned url
Probably the simplest method is to generate so called presigned url (with aws client libraries). We calculate the url and aws client library returns url that can be used on the client side using simple POST uploads. The example flow looks like following:
((image))

Looking good from simplicity perspective though following drawbacks are present:
- not many options to define what can be uploaded 
- require sending AWSACCESSKEYID to be send in url

Now maybe the latter does not look problematic at first sight it might trigger red alert at security expects. Its due to fact that url usually is not considered as place where secret information should be present and althoguht AWSACCESSKEY is only part of credentials it still might be seen as sending secret in url. Let's focus than on the last one

### Using form uploads


 
  
