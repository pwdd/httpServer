package com.github.pwdd.HTTPServer.requestHandlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class RequestParser implements IRequestParser {
  private String CRLF = "\r\n";

  public HashMap<String, String> requestMap(BufferedReader request) throws IOException {
    synchronized (this) {
      HashMap<String, String> headerMap = new HashMap<>();
      String requestString = bufToString(request);
      String[] requestArray = stringToStringArray(requestString);
      parseFirstLine(headerMap, requestArray[0]);
      String[] restOfRequest = Arrays.copyOfRange(requestArray, 1, requestArray.length);

      for (String line : restOfRequest) {
        String[] splitLine = line.split(": ");
        headerMap.put(splitLine[0], splitLine[1]);
      }

      return headerMap;
    }
  }

  private String bufToString(BufferedReader buf) throws IOException {
    int contentLength = 0;
    String contentLengthKey = "Content-Length: ";
    StringBuilder request = new StringBuilder();
    String line;

    while ((line = buf.readLine()) != null && !line.equals("")) {
      request.append(line).append(CRLF);

      if (line.contains(contentLengthKey)) {
        contentLength = Integer.parseInt(line.substring(contentLengthKey.length()));
      }
    }
    if (contentLength > 0) {
      getBody(buf, request, contentLength);
    }
    return request.toString();
  }

  private void getBody(BufferedReader buf, StringBuilder base, int size) throws IOException {
    char[] body = new char[size];
    buf.read(body);
    base.append("Body: ").append(new String(body)).append(CRLF);
  }

  private String[] stringToStringArray(String in) {
    return in.split("\\r\\n");
  }

  private void parseFirstLine(HashMap<String, String> map, String firstLine) {
    String[] firstLineList = firstLine.split("\\s");

    map.put("Method", firstLineList[0]);
    map.put("URI", firstLineList[1]);
    map.put("Protocol", firstLineList[2]);
  }
}
