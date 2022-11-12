FROM maven:3.8.6-ibmjava-8

RUN mkdir /var/app
COPY ./* /var/app
WORKDIR /var/app

RUN mvn -Dmaven.wagon.http.ssl.insecure=true dependency:resolve

CMD ["mvn", "-Dmaven.wagon.http.ssl.insecure=true", "spring-boot:run"]