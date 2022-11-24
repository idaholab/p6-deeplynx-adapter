FROM maven:3.8.6-ibmjava-8

RUN mkdir /var/app
COPY ./* /var/app
WORKDIR /var/app

RUN mvn -Dmaven.wagon.http.ssl.insecure=true dependency:resolve
RUN mvn install:install-file -Dmaven.wagon.http.ssl.insecure=true -Dfile=lib/p6ws-jaxws-client.jar -DgroupId=com.primavera.ws.p6 -DartifactId=p6ws -Dversion=1.0 -Dpackaging=jar
RUN mvn install:install-file -Dmaven.wagon.http.ssl.insecure=true -Dfile=lib/cxf-manifest.jar -DgroupId=com.primavera.ws.p6 -DartifactId=cxf -Dversion=1.0 -Dpackaging=jar

CMD ["mvn", "-Dmaven.wagon.http.ssl.insecure=true", "spring-boot:run"]