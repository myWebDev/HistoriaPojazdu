package tk.v3l0c1r4pt0r.cepik;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import tk.v3l0c1r4pt0r.cepik.CarReport.EntryNotFoundException;
import tk.v3l0c1r4pt0r.cepik.CarReport.WrongCaptchaException;
import android.content.Context;
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
	
	private transient Context context = null;
	
	public enum Field
	{
		Rej, Vin, Date, Captcha
	}
	
	public class ReportNotGeneratedException extends Exception
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 6951871210320962719L;
		
	}
	
	public class InvalidInputException extends Exception
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 2215118885828047337L;
		public Field field;
		
		public InvalidInputException(Field f)
		{
			this.field = f;
		}
		
	}
	
	public WebService(Context con) 
			throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, 
			KeyManagementException
	{
		this.context = con;
		
		URL url = new URL(mainUrl);
		HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
		urlConnection.setRequestProperty("User-Agent", userAgent);
		
		try {
			try
			{
				urlConnection.connect();
			}
			catch(SSLHandshakeException e)
			{
				urlConnection.setSSLSocketFactory(getSocketFactory());
				urlConnection.connect();
			}
			/*InputStream is = urlConnection.getInputStream();
			byte[] buf = new byte[1024];
			int len = 0;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while((len = is.read(buf)) != -1)
			{
				baos.write(buf, 0, len);
			}
			String content = baos.toString();
			content.toString();*/
			List<String> cookies;
			cookies = urlConnection.getHeaderFields().get("Set-Cookie");
			if(cookies == null)
				cookies = urlConnection.getHeaderFields().get("set-cookie");
			cookie = "";
			for(String c : cookies)
			{
				cookie += c.substring(0, c.indexOf(';')) + "; ";
			}
		}
		catch(IOException e)
		{
			e.getCause().printStackTrace();
			throw e;
		}
	    finally {
	    	urlConnection.disconnect();
	    }
	}
	
	private byte[] getResponse(URL url) 
			throws IOException, KeyManagementException, CertificateException, KeyStoreException, 
			NoSuchAlgorithmException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
		urlConnection.setRequestProperty("User-Agent", userAgent);
		urlConnection.setRequestProperty("Cookie", cookie);
		
		try {
			try
			{
				urlConnection.connect();
			}
			catch(SSLHandshakeException e)
			{
				urlConnection.setSSLSocketFactory(getSocketFactory());
				urlConnection.connect();
			}
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
	
	private byte[] getResponse(URL url, String postData) 
			throws IOException, KeyManagementException, CertificateException, KeyStoreException, 
			NoSuchAlgorithmException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
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
			try
			{
				urlConnection.connect();
			}
			catch(SSLHandshakeException e)
			{
				urlConnection.setSSLSocketFactory(getSocketFactory());
				urlConnection.connect();
			}
			String redirect = urlConnection.getHeaderField("Location");
			if(redirect != null)	//truth before ICS
			{
				urlConnection.disconnect();
				
				urlConnection = (HttpsURLConnection) (new URL(redirect)).openConnection();
				urlConnection.setRequestProperty("User-Agent", userAgent);
				urlConnection.setRequestProperty("Cookie", cookie);
				urlConnection.setRequestMethod("POST");
				urlConnection.setDoInput(true);
				urlConnection.setDoOutput(true);

				OutputStream os2 = urlConnection.getOutputStream();
				BufferedWriter writer2 = new BufferedWriter(
				        new OutputStreamWriter(os2, "UTF-8"));
				writer2.write(postData);
				writer2.flush();
				writer2.close();
				os2.close();
				try
				{
					urlConnection.connect();
				}
				catch(SSLHandshakeException e)
				{
					urlConnection.setSSLSocketFactory(getSocketFactory());
					urlConnection.connect();
				}
			}
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
	
	private WebFile getResponseAsFile(URL url, String postData) 
			throws IOException, KeyManagementException, CertificateException, KeyStoreException, 
			NoSuchAlgorithmException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
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
		
		String fileName;
		
		try {
			try
			{
				urlConnection.connect();
			}
			catch(SSLHandshakeException e)
			{
				urlConnection.setSSLSocketFactory(getSocketFactory());
				urlConnection.connect();
			}
			InputStream is = urlConnection.getInputStream();
			int len = 0;
			while ((len = is.read(buffer)) != -1) 
			{
				baos.write(buffer, 0, len);
			}
			String disposition = urlConnection.getHeaderField("Content-Disposition");
			fileName = disposition.substring(disposition.indexOf("filename=")+9);
		}
		catch(IOException e)
		{
			throw e;
		}
	    finally {
	    	urlConnection.disconnect();
	    }
		return new WebFile(fileName,baos.toByteArray());
	}
	
	public Bitmap getCaptcha() 
			throws MalformedURLException, IOException, KeyManagementException, CertificateException, 
			KeyStoreException, NoSuchAlgorithmException
	{
		byte[] response = getResponse(new URL(captchaUrl));
		Bitmap bmp = BitmapFactory.decodeByteArray(response, 0, response.length);
		return bmp;
	}
	
	public CarReport getReport(String nrRejestracyjny, String vin, String dataRejestracji, String captcha) 
			throws MalformedURLException, IOException, EntryNotFoundException, WrongCaptchaException, 
			InvalidInputException, KeyManagementException, CertificateException, KeyStoreException, 
			NoSuchAlgorithmException
	{
		if(nrRejestracyjny.length() == 0)
		{
			throw new InvalidInputException(Field.Rej);
		}
		else if(nrRejestracyjny.equals("TEST"))
		{
			//przykładowy raport
			byte[] response = getResponse(new URL("https://historiapojazdu.gov.pl/historia-pojazdu-web/przykladowy-raport.xhtml"));
			String reportStr = new String(response);
			//get javaxState
			CarReport cr = new CarReport(nrRejestracyjny, reportStr);
			Document doc = Jsoup.parse(reportStr);
			javaxState = doc.getElementById("javax.faces.ViewState").attributes().get("value");
			return cr;
		}
		else if(vin.length() < 17)
		{
			throw new InvalidInputException(Field.Vin);
		}
		else if(dataRejestracji.length() < 10)
		{
			throw new InvalidInputException(Field.Date);
		}
		else if(captcha.length() == 0)
		{
			throw new InvalidInputException(Field.Captcha);
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
			CarReport cr = new CarReport(nrRejestracyjny, reportStr);
			Document doc = Jsoup.parse(reportStr);
			javaxState = doc.getElementById("javax.faces.ViewState").attributes().get("value");
			return cr;
		}
	}
	
	public WebFile getReportPdf() 
			throws MalformedURLException, IOException, ReportNotGeneratedException, 
			KeyManagementException, CertificateException, KeyStoreException, NoSuchAlgorithmException
	{
		if(javaxState != null && javaxState != "")
		{
			String post = 
					    "formularz=formularz&"
					  + "javax.faces.ViewState="+javaxState+"&"
					  + "pobierzRaportPdf=pobierzRaportPdf";
			WebFile response = getResponseAsFile(new URL(pdfUrl), post);
			return response;
		}
		else
			throw new ReportNotGeneratedException();
	}
	
	private SSLSocketFactory getSocketFactory() 
			throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, 
			KeyManagementException
	{
		
		// Create a KeyStore containing our trusted CAs
		String keyStoreType = KeyStore.getDefaultType();
		KeyStore keyStore = KeyStore.getInstance(keyStoreType);
		keyStore.load(null, null);
		keyStore.setCertificateEntry("ca", getCertFromRes(R.raw.historiapojazdu));
//		keyStore.setCertificateEntry("burp", getCertFromRes(R.raw.burp));
		
		// Create a TrustManager that trusts the CAs in our KeyStore
		String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
		tmf.init(keyStore);
		
		// Create an SSLContext that uses our TrustManager
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, tmf.getTrustManagers(), null);
		
		return context.getSocketFactory();
		
	}
	
	private Certificate getCertFromRes(int res) throws CertificateException, IOException
	{
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		InputStream caInput = context.getResources().openRawResource(res);
		Certificate ca;
		try {
		ca = cf.generateCertificate(caInput);
		} finally {
		caInput.close();
		}
		
		return ca;
	}

}
