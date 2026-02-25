# ViewerHub Gateway
ViewerHub gateway is used to handle different type of authentication in order for viewers to get authentified when requesting data from the pacs.

ViewerHub Gateway handles basic authentication and oAuth2 (client credential and authorization code flow).

## Configuration

The configuration of the application is defined in the files application-gateway.yml and application-oidc-gtw.yml.
These files are directly located under the "resources" folder but they can be moved in the viewer-hub config-server. 

### application-gateway.yml

This file contains the routes which will redirect requests to the pacs.

Viewers will call the gateway with a certain path (in our example /DCM4CHEE-LOCAL or /ORTHANC-LOCAL) and depending on this path the gateway will apply some filters concerning the authentication.

Available filters: 
- for OAuth2: OAuth2ClientCredentialToken and OAuth2AuthorizationCodeToken
- for basic authentication: BasicAuthentication


###  application-oidc-gtw.yml

This file contains the definition of the oidc configuration to connect to in order to retrieve the token.

## Run configuration

- Configure the run configuration and add in VM options the following properties:
```
    -Duser.timezone=UTC
    -DENVIRONMENT=local
    -DEUREKA_CLIENT_SERVICE_URL_DEFAULT_ZONE=http://localhost:8761/eureka
    -DREGION=local
    -DDATACENTER=local
    -Dserver.port=8088
    -Dmanagement.server.port=19002
    -DBACKEND_URI=http://localhost:8088
    -DCONFIGSERVER_URI=http://localhost:8888  
```
- Then clean/install + run...
