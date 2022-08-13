FROM java:8

COPY *.jar /app.jar

VOLUME ["/previewPdf","/video","/log"]

CMD ["--server.port=9091"]

EXPOSE 8081

ENTRYPOINT ["java","-jar","/app.jar"]

