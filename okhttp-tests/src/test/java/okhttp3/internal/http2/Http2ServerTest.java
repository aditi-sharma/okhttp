package okhttp3.internal.http2;


import okio.BufferedSource;
import org.junit.Test;

import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Aditi on 12/5/16.
 */
public class Http2ServerTest {

    private final MockHttp2Peer peer = new MockHttp2Peer();
    String currentDir = System.getProperty("user.dir");
    File file = new File(currentDir);
    SSLSocketFactory sslFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
    Http2Server server = new Http2Server(file, sslFactory);

    @Test (expected = Exception.class)
    public void http2ServerRequireConnectionPreface() throws IOException {
        peer.acceptFrame(); // SYN_STREAM
        peer.play();

        Header header1 = new Header(":Content-type","text/html");
        Header header2 = new Header(":path","/text.html");
        Http2Connection connection =  new Http2Connection.Builder(true)
                .socket(peer.openSocket())
                .pushObserver(IGNORE).build();
        connection.start(true);
        Http2Stream stream = connection.newStream(Arrays.asList(header1, header2), true);
        server.onStream(stream);
        assertEquals("[:status: 404, :version: HTTP/1.1, content-type: text/plain]", connection.getStream(stream.getId()).getResponseHeaders().toString());
    }

    @Test public void http2Server404() throws IOException {
        peer.acceptFrame(); // SYN_STREAM
        peer.play();

        Header header1 = new Header(":Content-type","text/html");
        Header header2 = new Header(":path","/text.html");
        Http2Connection connection =  new Http2Connection.Builder(true)
                .socket(peer.openSocket())
                .pushObserver(IGNORE).build();
        connection.start(false);
        Http2Stream stream = connection.newStream(Arrays.asList(header1, header2), true);
        server.onStream(stream);
        assertEquals("[:status: 404, :version: HTTP/1.1, content-type: text/plain]", connection.getStream(stream.getId()).getResponseHeaders().toString());
    }

    @Test public void http2ServerServeFile() throws IOException {
        peer.acceptFrame(); // SYN_STREAM
        peer.play();

        Header header2 = new Header(":path","/fuzzingserver-expected.txt");
        Http2Connection connection =  new Http2Connection.Builder(true)
                .socket(peer.openSocket())
                .pushObserver(IGNORE).build();
        connection.start(false);
        Http2Stream stream = connection.newStream(Arrays.asList(header2), true);
        server.onStream(stream);
        assertEquals("[:status: 200, :version: HTTP/1.1, content-type: text/plain]", connection.getStream(stream.getId()).getResponseHeaders().toString());
    }

    @Test public void http2ServerServeDirectory() throws IOException {
        peer.acceptFrame(); // SYN_STREAM
        peer.play();

        Header header2 = new Header(":path","/target");
        Http2Connection connection =  new Http2Connection.Builder(true)
                .socket(peer.openSocket())
                .pushObserver(IGNORE).build();
        connection.start(false);
        Http2Stream stream = connection.newStream(Arrays.asList(header2), true);
        server.onStream(stream);
        assertEquals("[:status: 200, :version: HTTP/1.1, content-type: text/html; charset=UTF-8]", connection.getStream(stream.getId()).getResponseHeaders().toString());
    }

    static final PushObserver IGNORE = new PushObserver() {

        @Override public boolean onRequest(int streamId, List<Header> requestHeaders) {
            return false;
        }

        @Override public boolean onHeaders(int streamId, List<Header> responseHeaders, boolean last) {
            return false;
        }

        @Override public boolean onData(int streamId, BufferedSource source, int byteCount,
                                        boolean last) throws IOException {
            source.skip(byteCount);
            return false;
        }

        @Override public void onReset(int streamId, ErrorCode errorCode) {
        }
    };
}
