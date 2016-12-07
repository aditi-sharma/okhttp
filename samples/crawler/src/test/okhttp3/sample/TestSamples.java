package okhttp3.sample;
import org.junit.Test;

import java.io.IOException;

public class TestSamples{

    @Test
    public void crawlOkhttp() throws IOException {
        Crawler.main(new String[]{"temp", "http://square.github.io/okhttp/"});
    }

}