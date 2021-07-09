FROM ibmcom/websphere-liberty:20.0.0.11-full-java8-ibmjava-ubi

USER root

LABEL name="voice-agent-tester" \
      vendor="IBM" \
      version="1.0.7.0" \
      release="1.0.7.0" \
      summary="Voice Agent Tester microservice" \
      description="A microservice for running manual, automated and load tests against Voice Gateway and/or Voice Agent deployments."

RUN yum update --disableplugin=subscription-manager -y \
    && yum install --disableplugin=subscription-manager curl vim iputils tzdata glibc-locale-source -y \
    && yum autoremove -y \
    && yum clean all \
    && rm -rf /var/cache/yum \
    && rm -rf /tmp/* /var/tmp/* \
    && rpm -e --allmatches gpg-pubkey-*

RUN installUtility install --acceptLicense /config/server.xml

# Set security policy to unlimited
RUN sed -i 's/#crypto.policy=unlimited/crypto.policy=unlimited/' /opt/ibm/java/jre/lib/security/java.security \
    && sed -i 's/jdk.tls.disabledAlgorithms=.* \\/jdk.tls.disabledAlgorithms=SSLv3, RC4, DES, CBC, MD5withRSA, DHE, DH keySize < 1024, DESede, TLSv1,\\/' /opt/ibm/java/jre/lib/security/java.security

# Generate locales
RUN localedef -f UTF-8 -i en_US en_US.UTF-8 \
  && localedef -f UTF-8 -i de_DE de_DE.UTF-8 \
  && localedef -f UTF-8 -i es_ES es_ES.UTF-8 \
  && localedef -f UTF-8 -i fr_FR fr_FR.UTF-8 \
  && localedef -f UTF-8 -i it_IT it_IT.UTF-8 \
  && localedef -f UTF-8 -i ja_JP ja_JP.UTF-8 \
  && localedef -f UTF-8 -i pt_BR pt_BR.UTF-8 \
  && localedef -f GB18030 -i zh_CN zh_CN.GB18030 \
  && localedef -f UTF-8 -i zh_CN zh_CN.UTF-8 \
  && localedef -f UTF-8 -i zh_TW zh_TW.UTF-8

COPY build/libs/va-tester-api.war /config/apps/va-tester-api.war
COPY ["src/main/wlp/server.xml", "src/main/wlp/jvm.options", "/config/"]
COPY src/main/resources/cloudant /config/resources/cloudant
COPY src/main/wlp/security/server-security-incl.xml /config/resources/includeConfig/
COPY LICENSE /licenses

# Set default language to English
ENV LANG=en_US.UTF-8 \
    LC_ALL=en_US.UTF-8 \
    LANGUAGE=en_US:en \

    #Connections
    HTTP_PORT=9080 \
    HTTPS_PORT=9443 \
    HTTP_HOST="*" \

    # Logging
    LOG_LEVEL=info \
    LOG_MAX_FILE_SIZE=100 \
    LOG_MAX_FILES=5 \

    # Set default SSL settings for Liberty #NEEDED FOR HTTPS AND CONNECTION TO CLOUDANT
    SSL_TRUST_PASSPHRASE=changeit \
    SSL_TRUST_STORE_FILE=/opt/ibm/java/jre/lib/security/cacerts \
    SSL_TRUST_FILE_TYPE=JKS \
    SSL_KEY_PASSPHRASE=changeit \
    SSL_KEY_STORE_FILE=/sslconf/key.jks \
    SSL_KEY_FILE_TYPE=JKS \

    # Security defaults
    SERVER_REGISTRY_INCLUDE_PATH=/config/resources/includeConfig/server-security-incl.xml \
    ROLE_NAME_ADMINISTRATOR=Administrator \
    ROLE_NAME_EDITOR=Editor \
    ROLE_NAME_OPERATOR=Operator \
    ROLE_NAME_VIEWER=Viewer \

    REST_ADMIN_USERNAME=defaultAdmin \
    REST_ADMIN_PASSWORD=defaultPassword \

    TESTER_WEBHOOK_USERNAME=defaultUser \
    TESTER_WEBHOOK_PASSWORD=defaultPass \

    # Defaults for validation
    CACHE_TIME_TO_LIVE=10 \
    CACHE_IMPLEMENTATION=dynacache \
    TESTER_WEBHOOK_URI=null \
    CLOUDANT_URL=null \
    CLOUDANT_USERNAME=null \
    CLOUDANT_PASSWORD=null \
    CLOUDANT_ACCOUNT=null \
    CLOUDANT_APIKEY=null \
    CALLER_VOICE_GATEWAY_URI=null \
    CALLER_VOICE_GATEWAY_USERNAME=null \
    CALLER_VOICE_GATEWAY_PASSWORD=null \
    CLOUDANT_DATABASE_NAME=null

USER default