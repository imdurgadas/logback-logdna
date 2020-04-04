# Logback appended for LogDNA

LogDNA is a hosted logging platform: https://logdna.com.
This library provides an asynchronous appender for logback, allowing to send your logs to the LogDNA platform (LogDNA + LogDNA service provided by Cloud providers).
Library also allows you to send stacktrace such that it's visible in one go rather than different lines.
In addition , it supports Mapped Diagnostic Context(MDC) too where you can pass additional metadata seamlessly and the same can be used to filter logs on the LogDNA dashboard.


### How To Use it 

Add this dependency to your pom.xml

```
<dependency>
  <groupId>com.github.imdurgadas</groupId>
  <artifactId>logback-logdna</artifactId>
  <version>1.0</version>
</dependency>
```

Logback uses XML file in known locations with the most common being `classpath:/logback.xml  | src/main/resources/logback.xml`

Copy the content to your logback.xml:

    <?xml version="1.0" encoding="UTF-8"?>
    <configuration>
      <appender name="logDNA-http" class="com.github.imdurgadas.appender.LogDNAAppender">
        <appName>LogDNA-Logback-Aoo</appName>
        <includeStacktrace>true</includeStacktrace>
        <ingestKey>${INGESTION_KEY}</ingestKey>
        <logDnaUrl>${LOGDNA_URL}</logDnaUrl>
      </appender>
    
      <appender name="logDNA" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="logDNA-http"/>
      </appender>
    
    
      <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
          <charset>utf8</charset>
          <pattern>%-30(%d{HH:mm:ss.SSS} [%thread]) %-5level %m%n</pattern>
        </encoder>
      </appender>
    
      <root level="DEBUG">
        <appender-ref ref="console"/>
        <appender-ref ref="logDNA"/>
      </root>
    </configuration>
    

 ### Configuration
* Set up your INGESTION_KEY (api key) via environment variables.
* Set up the LOGDNA_URL (api url) the same way.
 `https://logs.logdna.com/logs/ingest`
 `https://logs.us-south.logging.cloud.ibm.com/logs/ingest`
* You can toggle including stacktrace using the `<includeStacktrace>true</includeStacktrace>` to either true or false
* Passing MDC as easy as `MDC.put(key,value)`.  
You can view the data on LogDNA dashboard by using the following format:  `meta.{keyname}:"value"`