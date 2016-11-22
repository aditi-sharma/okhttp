/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package okhttp3.curl;

import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static okhttp3.curl.Main.fromArgs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MainTest {
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

  @Before
  public void setUpStreams() {
    System.setOut(new PrintStream(outContent));}

  @After
  public void cleanUpStreams(){
    System.setOut(null);
  }

  @Test public void simple() {
    Request request = fromArgs("http://example.com").createRequest();
    assertEquals("GET", request.method());
    assertEquals("http://example.com/", request.url().toString());
    assertNull(request.body());
  }

  @Test public void put() throws IOException {
    Request request = fromArgs("-X", "PUT", "-d", "foo", "http://example.com").createRequest();
    assertEquals("PUT", request.method());
    assertEquals("http://example.com/", request.url().toString());
    assertEquals(3, request.body().contentLength());
  }

  @Test public void dataPost() {
    Request request = fromArgs("-d", "foo", "http://example.com").createRequest();
    RequestBody body = request.body();
    assertEquals("POST", request.method());
    assertEquals("http://example.com/", request.url().toString());
    assertEquals("application/x-www-form-urlencoded; charset=utf-8", body.contentType().toString());
    assertEquals("foo", bodyAsString(body));
  }

  @Test public void dataPut() {
    Request request = fromArgs("-d", "foo", "-X", "PUT", "http://example.com").createRequest();
    RequestBody body = request.body();
    assertEquals("PUT", request.method());
    assertEquals("http://example.com/", request.url().toString());
    assertEquals("application/x-www-form-urlencoded; charset=utf-8", body.contentType().toString());
    assertEquals("foo", bodyAsString(body));
  }

  @Test public void contentTypeHeader() {
    Request request = fromArgs("-d", "foo", "-H", "Content-Type: application/json",
        "http://example.com").createRequest();
    RequestBody body = request.body();
    assertEquals("POST", request.method());
    assertEquals("http://example.com/", request.url().toString());
    assertEquals("application/json; charset=utf-8", body.contentType().toString());
    assertEquals("foo", bodyAsString(body));
  }

  @Test public void referer() {
    Request request = fromArgs("-e", "foo", "http://example.com").createRequest();
    assertEquals("GET", request.method());
    assertEquals("http://example.com/", request.url().toString());
    assertEquals("foo", request.header("Referer"));
    assertNull(request.body());
  }

  @Test public void userAgent() {
    Request request = fromArgs("-A", "foo", "http://example.com").createRequest();
    assertEquals("GET", request.method());
    assertEquals("http://example.com/", request.url().toString());
    assertEquals("foo", request.header("User-Agent"));
    assertNull(request.body());
  }

  @Test public void headerSplitWithDate() {
    Request request = fromArgs("-H", "If-Modified-Since: Mon, 18 Aug 2014 15:16:06 GMT",
        "http://example.com").createRequest();
    assertEquals("Mon, 18 Aug 2014 15:16:06 GMT", request.header("If-Modified-Since"));
  }

  @Test public void sampleRequestProtocols(){
    fromArgs("-d", "Sample Test", "--frames", "true", "-i", "true", "-X", "POST", "-V", "true", "-H", "Content-Type: application/json", "-H", "If-Modified-Since: Mon, 18 Aug 2014 15:16:06 GMT", "http://example.com").run();
    assertTrue(outContent.toString().contains("Protocols: http/1.0, http/1.1, spdy/3.1, h2"));
  }

  @Test public void sampleInsecureRequest(){
    fromArgs("-d", "Sample Test", "--frames", "true", "-i", "true", "-X", "POST", "-k", "true", "-H", "Content-Type: application/json", "-H", "If-Modified-Since: Mon, 18 Aug 2014 15:16:06 GMT", "http://example.com").run();
    assertTrue(outContent.toString().contains("HTTP/1.1 304 Not Modified"));
  }

  private static String bodyAsString(RequestBody body) {
    try {
      Buffer buffer = new Buffer();
      body.writeTo(buffer);
      return buffer.readString(body.contentType().charset());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
