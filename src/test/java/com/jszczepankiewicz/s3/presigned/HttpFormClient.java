package com.jszczepankiewicz.s3.presigned;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Collections.emptyList;

public class HttpFormClient {
    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private HttpURLConnection httpConn;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter writer;

    public HttpFormClient(String requestURL)
            throws IOException {
        this.charset = "UTF-8";

        // creates a unique boundary based on time stamp
        boundary = "===" + System.currentTimeMillis() + "===";
        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);    // indicates POST method
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, this.charset),
                true);
    }

    public void addFormField(String name, String value) {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
                .append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=" + charset).append(
                LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }

    public void addFilePart(String fieldName, InputStream uploadFile, String fileName, String contentType)
            throws IOException {

        writer.append("--" + boundary).append(LINE_FEED);
        writer.append(
                "Content-Disposition: form-data; name=\"" + fieldName
                        + "\"; filename=\"" + fileName + "\"")
                .append(LINE_FEED);
        writer.append(
                "Content-Type: "
                        + contentType)
                .append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        InputStream inputStream = uploadFile;
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();
        writer.append(LINE_FEED);
        writer.flush();
    }

    public void addHeaderField(String name, String value) {
        writer.append(name + ": " + value).append(LINE_FEED);
        writer.flush();
    }

    public Response submit() throws IOException {
        List<String> response = new ArrayList<String>();
        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();

        // checks server's status code first
        int status = httpConn.getResponseCode();

        if(status == HTTP_NO_CONTENT){
            return new Response(emptyList(), status);
        }

        StringBuilder resp = new StringBuilder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                httpConn.getInputStream()));
        String line = null;
        while ((line = reader.readLine()) != null) {
            resp.append(line);
            response.add(line);
        }
        reader.close();
        httpConn.disconnect();
        if(status!= HTTP_OK){
            throw new IOException("Unsuccessfull response from upload: status: [" + status + "] and response:\n" + resp.toString() + "\n===================================");
        }

        return new Response(response, status);
    }

    static class Response {
        private List<String> responseLines;
        private int httpCode;

        public Response(List<String> responseLines, int httpCode) {
            this.responseLines = responseLines;
            this.httpCode = httpCode;
        }

        public Response(int httpCode) {
            this.httpCode = httpCode;
            this.responseLines = emptyList();
        }

        public List<String> getResponseLines() {
            return responseLines;
        }

        public int getHttpCode() {
            return httpCode;
        }
    }

    static Response uploadForm(String url, Map<String, String> fields, InputStream file, String fileName, String contentType) {
        try {
            HttpFormClient form = new HttpFormClient(url);

            for (Map.Entry<String, String> pair : fields.entrySet()) {
                String k = pair.getKey();
                String v = pair.getValue();
                form.addFormField(pair.getKey(), pair.getValue());
            }

            form.addFilePart("file", file, fileName, contentType);
            Response response = form.submit();

            return response;
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected IOException: ", e);
        }
    }
}