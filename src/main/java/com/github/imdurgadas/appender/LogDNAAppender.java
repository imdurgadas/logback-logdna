package com.github.imdurgadas.appender;

import static com.github.imdurgadas.utils.Constants.APIKEY;
import static com.github.imdurgadas.utils.Constants.APP;
import static com.github.imdurgadas.utils.Constants.APPLICATION_JSON;
import static com.github.imdurgadas.utils.Constants.HOSTNAME;
import static com.github.imdurgadas.utils.Constants.LEVEL;
import static com.github.imdurgadas.utils.Constants.LINE;
import static com.github.imdurgadas.utils.Constants.LINES;
import static com.github.imdurgadas.utils.Constants.LOGGER;
import static com.github.imdurgadas.utils.Constants.META;
import static com.github.imdurgadas.utils.Constants.NOW;
import static com.github.imdurgadas.utils.Constants.TIMESTAMP;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Slf4j
public class LogDNAAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private static final String DEFAULT_INGEST_URL = "https://logs.logdna.com/logs/ingest";
    private final Map<String, String> headers;
    private String hostname;
    private String appName;
    private boolean includeStacktrace = true;
    private boolean sendMDC = true;
    private String logDnaUrl;
    private OkHttpClient client = new OkHttpClient();

    public LogDNAAppender() {
        getHostname();
        this.headers = new HashMap<>();
        this.headers.put("Content-Type", "application/json");
        this.headers.put("Accept", "application/json");
        this.headers.put("User-Agent", "logback-logdna appender - imdurgadas");
    }

    private static String encode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    private void getHostname() {
        try {
            this.hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            this.hostname = "localhost";
        }
    }

    @Override
    protected void append(ILoggingEvent loggingEvent) {
      /*  if (loggingEvent.getLoggerName().equals(LogDNAAppender.class.getName())) {
            return;
        }
*/
        StringBuilder loggingMessage = getLoggingMessage(loggingEvent);

        try {
            JSONObject payload = prepareJsonBody(loggingEvent, loggingMessage);

            RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSON), payload.toString());
            HttpUrl.Builder urlBuilder = HttpUrl.parse(logDnaUrl).newBuilder()
                                                .addQueryParameter(HOSTNAME, encode(this.hostname))
                                                .addQueryParameter(NOW,
                                                                   encode(String.valueOf(System.currentTimeMillis())));



            Request request = new Request.Builder().url(urlBuilder.build().toString()).headers(Headers.of(headers))
                                                   .post(body).build();

            Response response = invoke(request);
            if (!response.isSuccessful()) {
                log.error("Error posting to logDNA , ResponseCode: {} , Message: {}", response.code(),
                          response.message());
            }
        } catch (JSONException | IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private Response invoke(Request request) throws IOException {
        Call call = client.newCall(request);
        return call.execute();
    }

    private JSONObject prepareJsonBody(ILoggingEvent loggingEvent, StringBuilder sb) {
        JSONObject payload = new JSONObject();
        JSONArray lines = new JSONArray();
        payload.put(LINES, lines);

        JSONObject line = new JSONObject();
        line.put(TIMESTAMP, loggingEvent.getTimeStamp());
        line.put(LEVEL, loggingEvent.getLevel().toString());
        line.put(APP, this.appName);
        line.put(LINE, sb.toString());

        JSONObject meta = new JSONObject();
        meta.put(LOGGER, loggingEvent.getLoggerName());
        line.put(META, meta);

        if (this.sendMDC && !loggingEvent.getMDCPropertyMap().isEmpty()) {
            for (Entry<String, String> entry : loggingEvent.getMDCPropertyMap().entrySet()) {
                meta.put(entry.getKey(), entry.getValue());
            }
        }

        lines.put(line);
        return payload;
    }

    private StringBuilder getLoggingMessage(ILoggingEvent ev) {
        StringBuilder sb = new StringBuilder().append("[").append(ev.getThreadName()).append("] ")
                                              .append(ev.getLoggerName()).append(" -- ")
                                              .append(ev.getFormattedMessage());

        if (ev.getThrowableProxy() != null && this.includeStacktrace) {
            IThrowableProxy tp = ev.getThrowableProxy();
            sb.append("\n\n").append(tp.getClassName()).append(": ").append(tp.getMessage());
            for (StackTraceElementProxy ste : tp.getStackTraceElementProxyArray()) {
                sb.append("\n\t").append(ste.getSTEAsString());
            }
        }
        return sb;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setLogDnaUrl(String logDnaUrl) {
        this.logDnaUrl = logDnaUrl == null ? DEFAULT_INGEST_URL:logDnaUrl;
    }

    public void setIngestKey(String ingestKey) {
        this.headers.put(APIKEY, ingestKey);
    }

    public void setSendMDC(boolean sendMDC) {
        this.sendMDC = sendMDC;
    }

    public void setIncludeStacktrace(boolean includeStacktrace) {
        this.includeStacktrace = includeStacktrace;
    }
}
