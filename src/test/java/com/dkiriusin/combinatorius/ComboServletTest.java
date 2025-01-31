/**
 * Copyright (c) 2015 deniskir@gmail.com. All rights reserved.
 *
 * @author Denis Kiriusin
 */

package com.dkiriusin.combinatorius;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.dkiriusin.combinatorius.CombinatoriusServlet;
import com.dkiriusin.combinatorius.MimeType;
import com.dkiriusin.combinatorius.Property;
import com.dkiriusin.combinatorius.RequestDetails;


@RunWith(MockitoJUnitRunner.class)
public class ComboServletTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Rule
	public TemporaryFolder tp = new TemporaryFolder();

	@InjectMocks
	private CombinatoriusServlet servlet = new CombinatoriusServlet();
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private ServletConfig servletConfig;
	@Mock
	private ServletContext servletContext;
	@Mock
	private ThreadLocal<RequestDetails> requestDetails;
	@Mock
	private RequestDetails requestDetailsObject;
	@Mock
	private Cookie cookie;
	@Mock
	private Properties properties;

	@Before
	public void setUp() throws ServletException, IOException, URISyntaxException {
		Mockito.when(servlet.getServletContext()).thenReturn(servletContext);
		Mockito.when(servletContext.getRealPath(Mockito.anyString())).thenReturn("src/test/resources/css/file1.css");
		Mockito.when(requestDetailsObject.getMimeType()).thenReturn(MimeType.css);
		Mockito.when(requestDetailsObject.getResources()).thenReturn(TestUtils.URL);
		Mockito.when(requestDetailsObject.getThemeName()).thenReturn("test-theme");
		Mockito.when(requestDetailsObject.getVersion()).thenReturn(0L);
		Mockito.when(requestDetailsObject.getExtension()).thenReturn("css");
		Mockito.when(requestDetails.get()).thenReturn(requestDetailsObject);
		
		Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer(TestUtils.URL));
		Mockito.when(properties.getProperty(Mockito.eq(Property.CSS_DIR.getName()), Mockito.anyString())).thenReturn("src/test/resources/css");
		Mockito.when(properties.getProperty(Mockito.eq(Property.THEMES_DIR.getName()), Mockito.anyString())).thenReturn("src/test/resources/themes");
		Mockito.when(properties.getProperty(Mockito.eq(Property.CSS_CACHE_DIR.getName()), Mockito.anyString())).thenReturn("css_cache");
		Mockito.when(properties.getProperty(Mockito.eq(Property.IS_COMPRESSION_ENABLED.getName()), Mockito.anyString())).thenReturn("true");
		Mockito.when(properties.getProperty(Mockito.eq(Property.IS_YUI_COMPRESSOR_ENABLED.getName()), Mockito.anyString())).thenReturn("false");
		Mockito.when(properties.getProperty(Mockito.eq(Property.YUI_OMIT_FILES_FROM_MINIFICATION_REGEX.getName()), Mockito.anyString())).thenReturn(".*\\.min\\.(js|css)$");
		Mockito.when(properties.getProperty(Mockito.eq(Property.YUI_CSSCOMPRESSOR_LINEBREAKPOS.getName()), Mockito.anyString())).thenReturn("-1");
		Mockito.when(properties.getProperty(Mockito.eq(Property.YUI_JAVASCRIPT_COMPRESSOR_DISABLEOPTIMISATIONS.getName()), Mockito.anyString())).thenReturn("true");
		Mockito.when(properties.getProperty(Mockito.eq(Property.YUI_JAVASCRIPT_COMPRESSOR_LINEBREAK.getName()), Mockito.anyString())).thenReturn("100");
		Mockito.when(properties.getProperty(Mockito.eq(Property.YUI_JAVASCRIPT_COMPRESSOR_NOMUNGE.getName()), Mockito.anyString())).thenReturn("false");
		Mockito.when(properties.getProperty(Mockito.eq(Property.YUI_JAVASCRIPT_COMPRESSOR_PRESERVEALLSEMICOLONS.getName()), Mockito.anyString())).thenReturn("true");
		Mockito.when(properties.getProperty(Mockito.eq(Property.YUI_JAVASCRIPT_COMPRESSOR_VERBOSE.getName()), Mockito.anyString())).thenReturn("false");
	}

	@After
	public void tearDown() {
		Mockito.when(servlet.getServletContext()).thenReturn(null);
		requestDetailsObject = null;
	}

	@Test
	public void testGetFilesWithDefaultDirectoriesOnly() {
		Mockito.when(properties.getProperty(Property.CSS_DIR.getName())).thenReturn("src/test/resources/css");
		Mockito.when(requestDetailsObject.getThemeName()).thenReturn(null);
		Mockito.when(requestDetailsObject.getResources()).thenReturn(null);

		Collection<File> files = servlet.getFiles(request, requestDetailsObject);

		Assert.assertTrue("Should be at least several CSS files in test folder", files.size() > 0);
	}

	@Test
	public void testGetFilesWithThemes() {
		Mockito.when(properties.getProperty(Property.CSS_DIR.getName())).thenReturn("src/test/resources/css");
		Mockito.when(properties.getProperty(Property.THEMES_DIR.getName())).thenReturn("src/test/resources/themes");
		Mockito.when(requestDetailsObject.getResources()).thenReturn(null);

		Collection<File> files = servlet.getFiles(request, requestDetailsObject);

		Assert.assertTrue("Should be at least several CSS files in test folders including test themes",
				files.size() > 3);
	}

	@Test
	public void testGetFilesWithWrongTheme() {
		Mockito.when(requestDetailsObject.getThemeName()).thenReturn("wrong-theme");
		Mockito.when(properties.getProperty(Property.CSS_DIR.getName())).thenReturn("src/test/resources/css");
		Mockito.when(properties.getProperty(Property.THEMES_DIR.getName())).thenReturn("src/test/resources/themes");
		Mockito.when(requestDetailsObject.getResources()).thenReturn(null);

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(
				"Error getting 'wrong-theme' theme. Please make sure the theme name is correctly specified via 'theme' "
						+ "URL parameter or as 'combinatorius.theme' cookie value.");

		servlet.getFiles(request, requestDetailsObject);
	}

	@Test
	public void testGetFilesWithIcorrectResourcesDir() {
		Mockito.when(properties.getProperty(Property.CSS_DIR.getName())).thenReturn("src/test/resources/wrong_dir");

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(Matchers.containsString("Error getting files from"));

		servlet.getFiles(request, requestDetailsObject);
	}
	
	@Test
	public void testDoGetWithIcorrectResourcesURL() throws ServletException, IOException {
		Mockito.when(properties.getProperty(Property.CSS_DIR.getName())).thenReturn("src/test/resources/css");
		Mockito.when(properties.getProperty(Property.THEMES_DIR.getName())).thenReturn("src/test/resources/themes");
		servlet.doGet(request, response);
		Mockito.verify(response).sendError(Mockito.eq(HttpServletResponse.SC_BAD_REQUEST), 
				Mockito.contains("Error trying to get content:"));
	}
	
	@Test
	public void testDoGetWithNoCacheDir() throws ServletException, IOException {
		Mockito.when(properties.getProperty(Property.CSS_DIR.getName())).thenReturn("src/test/resources/css");
		Mockito.when(properties.getProperty(Property.THEMES_DIR.getName())).thenReturn("src/test/resources/themes");
		Mockito.when(properties.getProperty(Property.CSS_CACHE_DIR.getName())).thenReturn(null);
		servlet.doGet(request, response);
		Mockito.verify(response).sendError(Mockito.eq(HttpServletResponse.SC_BAD_REQUEST), 
				Mockito.contains("Error trying to get content:"));
	}
	
	@Test
	public void testDoGetWithNoCssDir() throws ServletException, IOException {
		Mockito.when(properties.getProperty(Property.CSS_DIR.getName())).thenReturn(null);

		servlet.doGet(request, response);
		
		Mockito.verify(response).sendError(Mockito.eq(HttpServletResponse.SC_BAD_REQUEST), 
				Mockito.contains("CSS directory not specified"));
	}

	@Test
	public void testGetFilesAlwaysReturnsCollection() throws IOException {
		Collection<File> files = servlet.getFiles(request, null);
		Assert.assertEquals("Should always return collection, never null", files.size(), 0);
	}

	@Test
	public void testSetResponseHeaders() {
		Mockito.when(request.getScheme()).thenReturn("non-https");
		Mockito.when(properties.getProperty(Mockito.eq(Property.IS_COMPRESSION_ENABLED.getName()), Mockito.anyString())).thenReturn("true");
		Mockito.when(properties.getProperty(Mockito.eq(Property.S_MAXAGE.getName()), Mockito.anyString())).thenReturn("31536000");
		Mockito.when(properties.getProperty(Mockito.eq(Property.MAX_AGE.getName()), Mockito.anyString())).thenReturn("31536000");

		CombinatoriusServlet.setResponseHeaders(request, response, "test_etag", 127151112L, 128);

		Mockito.verify(response).setContentType(requestDetails.get().getMimeType().getName());
		Mockito.verify(response).setHeader("Etag", "test_etag");
		Mockito.verify(response).setCharacterEncoding("UTF-8");
		Mockito.verify(response).setDateHeader(Mockito.eq("Expires"), Mockito.anyLong());
		Mockito.verify(response).setHeader("Cache-Control", "private, max-age=31536000");
		Mockito.verify(response).setDateHeader(Mockito.eq("Last-Modified"), Mockito.anyLong());
		Mockito.verify(response).setContentLength(Mockito.anyInt());
	}

	@Test
	public void testSetResponseHeadersHTTPS() {
		Mockito.when(request.getScheme()).thenReturn("https");
		Mockito.when(properties.getProperty(Mockito.eq(Property.IS_COMPRESSION_ENABLED.getName()), Mockito.anyString())).thenReturn("true");
		Mockito.when(properties.getProperty(Mockito.eq(Property.S_MAXAGE.getName()), Mockito.anyString())).thenReturn("31536000");
		Mockito.when(properties.getProperty(Mockito.eq(Property.MAX_AGE.getName()), Mockito.anyString())).thenReturn("31536000");

		CombinatoriusServlet.setResponseHeaders(request, response, "test_etag", 127151112L, 128);

		Mockito.verify(response).setContentType(requestDetails.get().getMimeType().getName());
		Mockito.verify(response).setHeader("Etag", "test_etag");
		Mockito.verify(response).setCharacterEncoding("UTF-8");
		Mockito.verify(response).setDateHeader(Mockito.eq("Expires"), Mockito.anyLong());
		Mockito.verify(response).setHeader("Cache-Control", "public, s-maxage=31536000, max-age=31536000");
		Mockito.verify(response).setDateHeader(Mockito.eq("Last-Modified"), Mockito.anyLong());
		Mockito.verify(response).setContentLength(Mockito.anyInt());
	}

	@Test
	public void testSetConditionalResponseHeaders() {
		CombinatoriusServlet.setConditionalResponseHeaders(request, response);
		Mockito.verify(response).setStatus(304);
		Mockito.verify(response).setHeader("Content-Length", "0");
		Mockito.verify(response).setHeader("Last-Modified", request.getHeader("If-Modified-Since"));
	}

	@Test
	public void testGetThemeName() {
		Mockito.when(cookie.getName()).thenReturn(CombinatoriusServlet.combinatoriusTheme);
		Mockito.when(cookie.getValue()).thenReturn("green");
		Mockito.when(cookie.getDomain()).thenReturn("localhost");
		Mockito.when(request.getCookies()).thenReturn(new Cookie[] { cookie });
		Mockito.when(request.getServerName()).thenReturn("localhost");

		String themeName = servlet.getThemeName(request, requestDetails.get());

		Assert.assertEquals("green", themeName);
	}
}
