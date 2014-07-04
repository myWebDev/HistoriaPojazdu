package tk.v3l0c1r4pt0r.cepik;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import tk.v3l0c1r4pt0r.cepik.CarReport.EntryNotFoundException;
import tk.v3l0c1r4pt0r.cepik.CarReport.WrongCaptchaException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class WebService implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8796560971836763585L;
	private String cookie;
	private String javaxState;
	private static String mainUrl = "https://historiapojazdu.gov.pl/historia-pojazdu-web/index.xhtml";
	private static String pdfUrl = "https://historiapojazdu.gov.pl/historia-pojazdu-web/historiaPojazdu.xhtml";
	private static String captchaUrl = "https://historiapojazdu.gov.pl/historia-pojazdu-web/captcha";
	private static String userAgent = "Mozilla/5.0 (X11; Linux x86_64; rv:30.0) Gecko/20100101 Firefox/30.0";
	
	public class ReportNotGeneratedException extends Exception
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 6951871210320962719L;
		
	}
	
	public WebService() throws IOException
	{
		URL url = new URL(mainUrl);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setRequestProperty("User-Agent", userAgent);

		try {
			urlConnection.connect();
			List<String> cookies = urlConnection.getHeaderFields().get("Set-Cookie");
			cookie = "";
			for(String c : cookies)
			{
				cookie += c.substring(0, c.indexOf(';')) + "; ";
			}
		}
		catch(IOException e)
		{
			throw e;
		}
	    finally {
	    	urlConnection.disconnect();
	    }
	}
	
	private byte[] getResponse(URL url) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setRequestProperty("User-Agent", userAgent);
		urlConnection.setRequestProperty("Cookie", cookie);

		try {
			urlConnection.connect();
			InputStream is = urlConnection.getInputStream();
			int len = 0;
			while ((len = is.read(buffer)) != -1) 
			{
				baos.write(buffer, 0, len);
			}
			
		}
		catch(IOException e)
		{
			throw e;
		}
	    finally {
	    	urlConnection.disconnect();
	    }
		return baos.toByteArray();
	}
	
	private byte[] getResponse(URL url, String postData) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setRequestProperty("User-Agent", userAgent);
		urlConnection.setRequestProperty("Cookie", cookie);
		urlConnection.setRequestMethod("POST");
		urlConnection.setDoInput(true);
		urlConnection.setDoOutput(true);

		OutputStream os = urlConnection.getOutputStream();
		BufferedWriter writer = new BufferedWriter(
		        new OutputStreamWriter(os, "UTF-8"));
		writer.write(postData);
		writer.flush();
		writer.close();
		os.close();

		try {
			urlConnection.connect();
			InputStream is = urlConnection.getInputStream();
			int len = 0;
			while ((len = is.read(buffer)) != -1) 
			{
				baos.write(buffer, 0, len);
			}
			
		}
		catch(IOException e)
		{
			throw e;
		}
	    finally {
	    	urlConnection.disconnect();
	    }
		return baos.toByteArray();
	}
	
	public Bitmap getCaptcha() throws MalformedURLException, IOException
	{
		byte[] response = getResponse(new URL(captchaUrl));
		Bitmap bmp = BitmapFactory.decodeByteArray(response, 0, response.length);
		return bmp;
	}
	
	public CarReport getReport(String nrRejestracyjny, String vin, String dataRejestracji, String captcha) 
			throws MalformedURLException, IOException, EntryNotFoundException, WrongCaptchaException
	{
		if(
    			nrRejestracyjny.length() == 0 ||
    			vin.length() < 17 ||
    			dataRejestracji.length() < 10 ||
    			captcha.length() == 0
    			)
		{
			//przykładowy raport
			//TODO: skasować
			String target = "https://historiapojazdu.gov.pl/historia-pojazdu-web/przykladowy-raport.xhtml";
			byte[] response = getResponse(new URL(target));
			return new CarReport(nrRejestracyjny, new String(response));
		}
		else
		{
			//prawdziwy raport
			String post = 
					  "formularz=formularz&"
					+ "rej="+nrRejestracyjny+"&"
					+ "vin="+vin+"&"
					+ "data="+dataRejestracji+"&"
					+ "captchaAnswer="+captcha+"&"
					+ "btnSprawdz=Sprawd%C5%BA+pojazd+%C2%BB&"
					+ "com.sun.faces.StatelessPostback=value";
			byte[] response = getResponse(new URL(mainUrl), post);
			String reportStr = new String(response);
			//get javaxState
			Document doc = Jsoup.parse(reportStr);
			javaxState = doc.getElementById("javax.faces.ViewState").attributes().get("value");
			return new CarReport(nrRejestracyjny, reportStr);
		}
	}
	
	public PdfDescriptor getReportPdf() throws MalformedURLException, IOException, ReportNotGeneratedException
	{
		if(javaxState != null && javaxState != "")
		{
			String post = 
					    "formularz=formularz&"
					  + "javax.faces.ViewState="+javaxState+"&"
					  + "pobierzRaportPdf=pobierzRaportPdf";
			byte[] response = getResponse(new URL(pdfUrl), post);
			return new PdfDescriptor("raport.pdf",response);//TODO: weź nazwę z serwera
		}
		else
			throw new ReportNotGeneratedException();
	}

}
