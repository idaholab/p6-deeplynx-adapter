FROM maven:3.8.6-ibmjava-8 AS production

WORKDIR /var/app
COPY ./* /var/app/

RUN mvn install:install-file \
    -Dmaven.wagon.http.ssl.insecure=true \
    -Dfile=lib/p6ws-jaxws-client.jar \
    -DgroupId=com.primavera.ws.p6 \
    -DartifactId=p6ws \
    -Dversion=1.0 \
    -Dpackaging=jar \
    -DgeneratePom=true

RUN mvn install:install-file \
    -Dmaven.wagon.http.ssl.insecure=true \
    -Dfile=lib/cxf-manifest.jar \
    -DgroupId=com.primavera.ws.p6 \
    -DartifactId=cxf \
    -Dversion=1.0 \
    -Dpackaging=jar \
    -DgeneratePom=true

RUN mvn -Dmaven.wagon.http.ssl.insecure=true dependency:resolve

RUN mvn clean package

RUN chmod +x entrypoint.sh

ENTRYPOINT [ "./entrypoint.sh" ]