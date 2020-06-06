# Handling customer uploaded files in AWS
## The problem
Sooner of later majority of the projects in the cloud exposed to customers will need to provide functionality of allowing users to upload files that need to be processed and stored in the cloud.
Usually there are following phases when dealing with customer uploaded files:
- authenticating client app to allow upload
- receiving the file and buffering in temp storage
- doing sanity check (checking file extension, content type, size)
- optionally execute anti-malware checks

The most common approach when dealing with traditional application would be to upload the files directly through the apps with web interface using form upload or api call and streaming. This approach althrough by design quite simple has multiple challenges like: 

- it uses costly network / cpu / ram resources for transferring files
- usually majority of the enterprises would preffer to offload all possible activities (like handling uploads) to maximize ROI on going cloud
- binary uploads might trigger false positive XSS body alerts in web application firewall solution and effectively
- securing binary uploads might be challenging since it per design has to allow big payloads which might be used as HTTP DDoS attack vector
- satisfying full cycle of transferring customer upload files to target destination (usually S3) might be challenging from HA perspective



 
  
