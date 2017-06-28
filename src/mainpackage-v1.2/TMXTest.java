package mainpackage;

import java.io.IOException;
import java.net.MalformedURLException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class TMXTest {

	public TMXTest() {
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);
		WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER);  
		
		webClient.getOptions().setThrowExceptionOnScriptError(false); // disable exceptions
		
		//webClient.getOptions().setJavaScriptEnabled(false);   // disable JavaScript
        
        //String url = "http://web.tmxmoney.com/quote.php?qm_symbol=RY&locale=EN";
        String url = "http://web.tmxmoney.com/research.php?qm_symbol=RY";
        
        HtmlPage page = null;
        
		try {
			page = webClient.getPage(url);
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        webClient.waitForBackgroundJavaScript(30 * 1000);
        String pageAsXml = page.asXml();
        //System.out.println(pageAsXml);  // page in XML format

        // JSoup imported objects 
        Document doc = Jsoup.parse(pageAsXml);
        System.out.println(doc.outerHtml()); // page in HTML format
        
        webClient.close();

	}

}
