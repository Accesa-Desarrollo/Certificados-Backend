FROM tomcat AS tomcat-server
ENV TZ=America/Montevideo

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone


COPY target/certificates.war /usr/local/tomcat/webapps
