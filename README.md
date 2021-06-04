## How to Build the image
1. Login to ACR using docker. Input the username & password on prompt. 
> docker login crsmq.azurecr.io
2. To build the image issue the below command. 
> sudo docker build . -t smqcontroller
3. To tag the image issue the below command. 
> sudo docker tag  smqcontroller crsmq.azurecr.io/smq/smqcontroller:v6
4. To push the image to ACR issue the below command. 
> sudo docker push crsmq.azurecr.io/smq/smqcontroller:v6

## Create an image pull secret in AKS cluster
1. Use below command to create image pull secret, which will be used in deployment configuration to pull the image from ACR. 
> kubectl create secret docker-registry 'secret-name' --docker-server='acr-servername' --docker-username='acr-user-name' --docker-password='acr-password'

## Create an Azure File Storage
1. Create an Storage Account (sasmqfilestore) in Azure Portal. Then create an azure file share (smqfilestore) in the storage account.
2. Create the directory structre as below.
> IIB/PP


> IIB/PP/iiblogs

>IIB/PP/json/

>IIB/PP/IIBFlowExtract/

>IIB/PP/IIB/PP/application.properties
2. Create the file share secret.
> kubectl create secret generic azure-secret --from-literal=azurestorageaccountname=sasmqfilestore --from-literal=azurestorageaccountkey="key-value"



## Deploy the image as a Kubernetes Job:
1. Issue below command to deploy the job.
> kubectl apply -f controller-job.yaml

## Properties File:
1. The Azure File share is used as stoarge location for hosting the logs,json,csv as well as the application.properties file. The File Share is mounted on `/home/Azureuser/` container location. Ref to yaml file. 
2. Locations:
> PropertiesFile: /home/AzureUser/IIB/PP/application.properties

> LogDir=/home/AzureUser/IIB/PP/iiblogs/

> ExtractCsvDir=/home/AzureUser/IIB/PP/IIBFlowExtract/

> FlowJsonDir=/home/AzureUser/IIB/PP/json/

3. To update `application.properties` file, navigate to Azure File Share(/IIB/PP/application.properties) in Azure Portal and upload the properties file. 


