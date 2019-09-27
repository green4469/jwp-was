package server.http.io;

import server.http.request.HttpRequest;
import server.http.request.HttpRequestLine;
import server.http.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import was.utils.IOUtils;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpResponse.class);

    public static HttpRequest parse(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        HttpRequestLine httpRequestLine = parseStartLine(br);
        Map<String, String> headers = parseHeaders(br);
        Map<String, String> body = new HashMap<>();
        Map<String, String> cookies = new HashMap<>();
        if (headers.containsKey("Content-Length")) {
            body = parseBody(br, Integer.valueOf(headers.get("Content-Length")));
        }
        if (headers.containsKey("Cookie") && !headers.get("Cookie").equals("")) {
            cookies = parseCookies(headers.get("Cookie"));
        }
        return new HttpRequest(httpRequestLine, headers, cookies, body);
    }

    private static Map<String, String> parseCookies(String cookieString) {
        String[] cookies = cookieString.split("; ");
        return parseMultiValueString(cookies);
    }

    private static HttpRequestLine parseStartLine(BufferedReader br) throws IOException {
        String startLine = br.readLine();
        String[] splitStartLine = startLine.split(" ");
        String[] splitPath = splitStartLine[1].split("\\?");
        String method = splitStartLine[0];
        String path = splitPath[0];
        String httpVersion = splitStartLine[2];
        Map<String, String> params = new HashMap<>();

        if (splitPath.length > 1) {
            String queryString = splitPath[1];
            String[] queryParams = URLDecoder.decode(queryString, StandardCharsets.UTF_8.toString()).split("&");
            for (String queryParam : queryParams) {
                String[] pair = queryParam.split("=");
                params.put(pair[0], pair[1]);
            }
        }
        return new HttpRequestLine(method, path, httpVersion, params);
    }

    private static Map<String, String> parseHeaders(BufferedReader br) throws IOException {
        Map<String, String> headers = new HashMap<>();

        String line;
        while (!"".equals(line = br.readLine()) && line != null) {
            String[] splitHeader = line.split(": ");
            String key = splitHeader[0];

            if (splitHeader.length > 1) {
                String value = splitHeader[1];
                headers.put(key, value);
            } else {
                headers.put(key, "");
            }
        }
        return headers;
    }

    private static Map<String, String> parseBody(BufferedReader br, Integer contentLength) throws IOException {
        String requestBody = IOUtils.readData(br, contentLength);
        String decodedBody = URLDecoder.decode(requestBody, StandardCharsets.UTF_8.toString());
        String[] bodyPairs = decodedBody.split("&");
        return parseMultiValueString(bodyPairs);
    }

    private static Map<String, String> parseMultiValueString(String[] bodyPairs) {
        Map<String, String> body = new HashMap<>();
        for (String bodyPair : bodyPairs) {
            String[] pair = bodyPair.split("=");
            body.put(pair[0], pair[1]);
        }
        return body;
    }

    public static void send(OutputStream out, HttpResponse httpResponse) {
        try (DataOutputStream dos = new DataOutputStream(out)) {
            writeStartLine(dos, httpResponse);
            writeHeader(dos, httpResponse);
            writeBody(dos, httpResponse);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private static void writeStartLine(final DataOutputStream dos, HttpResponse response) throws IOException {
        dos.writeBytes(String.format("%s %s %s\n", response.getHttpVersion(),
                response.getHttpStatus().getCode(), response.getHttpStatus().getPhrase()));
    }

    private static void writeHeader(final DataOutputStream dos, HttpResponse response) throws IOException {
        Map<String, String> headers = response.getHeaders();
        for (String key : headers.keySet()) {
            dos.writeBytes(String.format("%s: %s\n", key, headers.get(key)));
        }
        if (response.hasCookies()) {
            writeCookies(dos, response);
        }
        dos.writeBytes("\n");
    }

    private static void writeCookies(DataOutputStream dos, HttpResponse response) throws IOException {
        Map<String, String> cookies = response.getCookies();
        List<String> formattedCookies = new ArrayList<>();
        for (String key : cookies.keySet()) {
            formattedCookies.add(String.format("%s=%s", key, cookies.get(key)));
        }
        formattedCookies.add(String.format("%s=%s", "Path", "/"));
        dos.writeBytes(String.format("%s: %s\n", "Set-Cookie", String.join("; ", formattedCookies)));
    }

    private static void writeBody(final DataOutputStream dos, HttpResponse response) throws IOException {
        byte[] body = response.getBody();
        if (body != null) {
            dos.write(body, 0, body.length);
        }
    }
}
