FROM maven:3.8.6-ibmjava-8

# install web server
RUN mkdir /usr/java
ENV JETTY_VERSION 9.4.0
ENV RELEASE_DATE v20161208
RUN wget --no-check-certificate https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-home/${JETTY_VERSION}.${RELEASE_DATE}/jetty-home-${JETTY_VERSION}.${RELEASE_DATE}.tar.gz && \
    tar -xzvf jetty-home-${JETTY_VERSION}.${RELEASE_DATE}.tar.gz && \
    rm -rf jetty-home-${JETTY_VERSION}.${RELEASE_DATE}.tar.gz && \
    mv jetty-home-${JETTY_VERSION}.${RELEASE_DATE}/ /usr/java/jetty

WORKDIR /usr/java/jetty
RUN java -jar start.jar --create-startd
RUN java -jar start.jar --add-to-start=http,deploy

# RUN mkdir /usr/java/jetty/webapps/root && mkdir /usr/java/jetty/webapps/root/WEB-INF && mkdir /usr/java/jetty/webapps/root/WEB-INF/classes
# COPY src/web.xml /usr/java/jetty/webapps/root/WEB-INF/web.xml
# COPY src/Servlet.java /usr/java/jetty/webapps/root/WEB-INF/classes/Servlet.java
# RUN javac -cp lib/servlet-api-3.1.jar webapps/root/WEB-INF/classes/Servlet.java
CMD ["java", "-jar", "start.jar"]

# CMD ["tail", "-f", "/dev/null"]