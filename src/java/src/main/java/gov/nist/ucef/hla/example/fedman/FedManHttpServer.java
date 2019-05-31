/*
 * This software is contributed as a public service by The National Institute of Standards 
 * and Technology (NIST) and is not subject to U.S. Copyright
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this 
 * software and associated documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above NIST contribution notice and this permission and disclaimer notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. THE AUTHORS OR COPYRIGHT HOLDERS SHALL
 * NOT HAVE ANY OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS.
 */
package gov.nist.ucef.hla.example.fedman;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import gov.nist.ucef.hla.base.UCEFException;
import gov.nist.ucef.hla.util.Constants;

/**
 * A simple HTTP server that responds to basic commands for the Federation Manager
 * 
 * It uses the "default" Java HTTP server which provided by both Oracle and OpenJDK JVMs, and thus
 * has the advantage of being quite compact and requiring no external dependencies.
 * 
 * If required the underlying mechanism should be able to be easily ported to something like takes
 * (https://github.com/yegor256/takes), nanoHTTPD (https://github.com/NanoHttpd/nanohttpd) or
 * similar.
 */
public class FedManHttpServer
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final Logger logger = LogManager.getLogger( FedManHttpServer.class );
	
	private static final int HTTP_200_OK = 200;
	private static final int HTTP_400_BAD_REQUEST = 404;
	private static final int HTTP_404_NOT_FOUND = 404;
	private static final int HTTP_405_METHOD_NOT_ALLOWED = 405;
	
	private static Charset UTF8 = StandardCharsets.UTF_8;
	private static String CONTENT_TYPE = "Content-Type";
	private static String TEXT_PLAIN_UTF8 = "text/plain; charset=UTF-8";
	private static String TEXT_HTML_UTF8 = "text/html; charset=UTF-8";
	private static String JSON_UTF8 = "application/json; charset=UTF-8";

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private FedManFederate fedManFederate;
	private int port;
	
	private boolean started;

	HttpServer server;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Constructor
	 * 
	 * @param fedManFederate the federation manager federate (used for callbacks from received
	 *            queries/commands
	 * @param port the port on which to laucnh the HTTP server
	 */
	public FedManHttpServer(FedManFederate fedManFederate, int port)
	{
		this.fedManFederate = fedManFederate;
		this.port = port;
		
		this.started = false;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Start the HTTP server
	 */
	public void startServer()
	{
		// don't start if we've already started!
		if( this.started )
			return;

		logger.info( "Starting federation manager HTTP server on port " + this.port + "..." );
		this.started = true;

		// create a single instance of the 404 handler that we can use repeatedly
		/*
		SimpleErrorHandler generic404Handler = new SimpleErrorHandler(
            HTTP_404_NOT_FOUND,
            "<html><head><meta charset=\"UTF-8\" /></head><body><h1>404 - Not Found</h1><pre>"+
            Constants.UCEF_LOGO+"<pre></body></html>"
        );
        */
		
		// Create a map of the contexts - mainly we do this so that we can create an
		// index page showing all the available endpoints (we cannot query this
		// information from the HttpServer after the contexts are added). The 
		// LinkedHashMap is used here because it maintains the insertion order,
		// which is mainly useful for organizing the the index page content.
		LinkedHashMap<String, HttpHandler> endpoints = new LinkedHashMap<>();
		IndexHandler indexHandler = new IndexHandler(endpoints);
		endpoints.put( "/",                        indexHandler );
		// GET requests
		endpoints.put( "/query/",                  indexHandler ); 
		endpoints.put( "/query/status/",           new StatusQueryHandler() );
		endpoints.put( "/query/start-conditions/", new StartConditionQueryHandler() );
		endpoints.put( "/query/can-start/",        new CanStartQueryHandler() );
		endpoints.put( "/query/has-started/",      new HasStartedQueryHandler() );
		endpoints.put( "/query/has-ended/",        new HasEndedQueryHandler() );
		endpoints.put( "/query/is-paused/",        new IsPausedQueryHandler() );
		endpoints.put( "/query/is-running/",       new IsRunningQueryHandler() );
		// POST requests
		endpoints.put( "/command/",                indexHandler );
		endpoints.put( "/command/start/",          new StartCommandHandler() );
		endpoints.put( "/command/pause/",          new PauseCommandHandler() );
		endpoints.put( "/command/resume/",         new ResumeCommandHandler() );
		endpoints.put( "/command/end/",            new EndCommandHandler() );
		
		ExecutorService executor = Executors.newFixedThreadPool(16);
		try
		{
			server = HttpServer.create( new InetSocketAddress( this.port ), 0 );
			server.setExecutor(executor);

			// iterate through the endpoints and link the contexts to the handlers
			for(Entry<String,HttpHandler> endpoint : endpoints.entrySet())
			{
				String context = endpoint.getKey();
				HttpHandler handler = endpoint.getValue();
				server.createContext( context, handler );
			}

			// start the server!
			server.start();
		}
		catch( Exception e )
		{
			throw new UCEFException( e, "Could not start federation manager HTTP server on port " +
			                            this.port );
		}
		logger.info( "Started federation manager HTTP server." );
	}
	
	/**
	 * Stop the HTTP server
	 */
	public void stopServer()
	{
		// nothing to do if we're not started!
		if( !this.started )
			return;
		
		logger.info( "Stopping federation manager HTTP server..." );
		try
		{
			server.stop( 0 );
		}
		catch( Exception e )
		{
			throw new UCEFException( e, "Error shutting down federation manager HTTP service." );
		}

		synchronized( server )
		{
			server.notifyAll();
		}
		
		this.started = false;
		logger.info( "Stopped federation manager HTTP server." );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////// HTTP Response Creation Utility Methods //////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Utility method to provide a plain text HTTP 200 response (most common use
	 * case)
	 * 
	 * @param httpExchange the {@link HTTPExchange} instance to use to create the response
	 * @param response the {@link String} containing the response text
	 * @throws IOException
	 */
	private void doPlainTextResponse( HttpExchange httpExchange, String response ) throws IOException
	{
		doPlainTextResponse( httpExchange, HTTP_200_OK, stringToBytes( response ) );
	}
	
	/**
	 * Utility method to provide a plain text response with the given HTTP status code
	 * 
	 * @param httpExchange the {@link HTTPExchange} instance to use to create the response
	 * @param code the HTTP status code associated with the response
	 * @param response the {@link String} containing the response text
	 * @throws IOException
	 */
	private void doPlainTextResponse( HttpExchange httpExchange, int code, String response ) throws IOException
	{
		doPlainTextResponse( httpExchange, code, stringToBytes( response ) );
	}
	
	/**
	 * Utility method to provide a plain text response with the given HTTP status code
	 * 
	 * @param httpExchange the {@link HTTPExchange} instance to use to create the response
	 * @param code the HTTP status code associated with the response
	 * @param response the {@link byte[]} containing the response text
	 * @throws IOException
	 */
	private void doPlainTextResponse( HttpExchange httpExchange, int code, byte[] response ) throws IOException
	{
		addResponseHeader( httpExchange, CONTENT_TYPE, TEXT_PLAIN_UTF8 );
		doHttpResponse(httpExchange, code, response);
	}
	
	/**
	 * Utility method to provide a JSON response with a HTTP 200 OK status code (most common use
	 * case)
	 * 
	 * @param httpExchange the {@link HTTPExchange} instance to use to create the response
	 * @param jsonMap the {@link Map} containing the JSON data structure
	 * @throws IOException
	 */
	private void doJSONResponse( HttpExchange httpExchange, Map<String,Object> jsonMap) throws IOException
	{
		doJSONResponse( httpExchange, HTTP_200_OK, jsonMap );
	}
	
	/**
	 * Utility method to provide a JSON response with the given HTTP status code
	 * 
	 * @param httpExchange the {@link HTTPExchange} instance to use to create the response
	 * @param code the HTTP status code associated with the response
	 * @param jsonMap the {@link Map} containing the JSON data structure
	 * @throws IOException
	 */
	private void doJSONResponse( HttpExchange httpExchange, int code, Map<String,Object> jsonMap) throws IOException
	{
		addResponseHeader( httpExchange, CONTENT_TYPE, JSON_UTF8 );
		doHttpResponse(httpExchange, code, stringToBytes( asJSON( jsonMap ) ));
	}
	
	/**
	 * Utility method to provide an HTML response with a HTTP 200 OK status code (most common use
	 * case)
	 * 
	 * @param httpExchange the {@link HTTPExchange} instance to use to create the response
	 * @param response the {@link String} containing the HTML
	 * @throws IOException
	 */
	private void doHTMLResponse( HttpExchange httpExchange, String response ) throws IOException
	{
		doHTMLResponse(httpExchange, HTTP_200_OK, response);
	}
	
	/**
	 * Utility method to provide an HTML response with the given HTTP status code
	 * 
	 * @param httpExchange the {@link HTTPExchange} instance to use to create the response
	 * @param code the HTTP status code associated with the response
	 * @param response the {@link String} containing the HTML
	 * @throws IOException
	 */
	private void doHTMLResponse( HttpExchange httpExchange, int code, String response ) throws IOException
	{
		addResponseHeader( httpExchange, CONTENT_TYPE, TEXT_HTML_UTF8 );
		doHttpResponse(httpExchange, code, stringToBytes(response));
	}
	
	/**
	 * Utility method to write out (i.e, send back) the HTTP response with the given HTTP status
	 * code
	 * 
	 * @param httpExchange the {@link HTTPExchange} instance to use to create the response
	 * @param code the HTTP status code associated with the response
	 * @param response the {@link byte[]} containing the response data
	 * @throws IOException
	 */
	private void doHttpResponse(HttpExchange httpExchange, int code, byte[] response ) throws IOException
	{
		httpExchange.sendResponseHeaders( code, response.length );
		OutputStream out = httpExchange.getResponseBody();
		out.write( response );
		out.close();
	}
	
	/**
	 * Utility method to add a response header to abstract away repeated code
	 * 
	 * @param httpExchange the {@link HTTPExchange} instance to use to create the response
	 * @param headerName the name of the header to add
	 * @param headerContent the content for the header
	 */
	private void addResponseHeader(HttpExchange httpExchange, String headerName, String headerContent)
	{
		httpExchange.getResponseHeaders().add( headerName, headerContent );
	}
	
	/**
	 * Utility method to add a `timestamp` (current system time in millliseconds) and `path`
	 * entries to a JSON structure intended as a query response
	 * 
	 * @param json the JSON structure to update (as a {@link Map<String, Object>} instance
	 * @param httpExchange the {@link HTTPExchange} instance containing the request (from which
	 *            the path will be extracted)
	 */
	private void addTimestampAndPath( Map<String,Object> json, HttpExchange httpExchange )
	{
		json.put( "timestamp", System.currentTimeMillis() );
		json.put( "path", strip( extractPath( httpExchange ), '/' ) );
		Map<String,String> query = extractQuery( httpExchange ) ;
		if(!query.isEmpty())
			json.put( "query", extractQuery( httpExchange ) );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// HTTP Request Inspection Methods /////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Utility method which extracts the request method (GET, POST, PUT, etc...) from an incoming
	 * request
	 * 
	 * @param httpExchange the {@link HTTPExchange} instance containing the request
	 * @return the request method
	 */
	private String extractMethod( HttpExchange httpExchange )
	{
		if( httpExchange == null )
			return "";
		return httpExchange.getRequestMethod();
	}

	/**
	 * Verifies that the method of the request is the expected request method
	 * 
	 * @param httpExchange the {@link HTTPExchange} instance containing the request
	 * @param method the method to check for (case insensitive)
	 * @return true of the request's method matches, false otherwise
	 */
	private boolean verifyMethod( HttpExchange httpExchange, String method )
	{
		if( httpExchange == null )
			return false;
		
		return extractMethod( httpExchange ).equalsIgnoreCase( method );
	}

	/**
	 * Utility method which extracts the request path (the part after the http://HOST:PORT/ and
	 * before any query parameters) from an incoming request
	 * 
	 * @param httpExchange the {@link HTTPExchange} instance containing the request
	 * @return the request path
	 */
	private String extractPath( HttpExchange httpExchange )
	{
		if( httpExchange == null )
			return "";
		return httpExchange.getRequestURI().getPath();
	}
	
	/**
	 * Utility method which extracts any query parameters from an incoming request
	 * 
	 * @param httpExchange the {@link HTTPExchange} instance containing the request
	 * @return the query parameters as key/value pairs in a {@link Map}
	 */
	private Map<String, String> extractQuery( HttpExchange httpExchange )
	{
		if( httpExchange == null )
			return Collections.emptyMap();
		
		String queryStr = httpExchange.getRequestURI().getQuery();
		if(queryStr == null)
			return Collections.emptyMap();
		
		Map<String, String> query = new HashMap<>();
		for(String group : queryStr.split( "&" ))
		{
			String[] parts = group.split( "=" );
			if(parts.length == 2)
			{
				query.put( parts[0], parts[1] );
			}
		}
		return query;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Response Encoding Utilities ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Utility method to convert a string to a byte array, ensuring UTF-8 encoding is used
	 * 
	 * @param str the {@link String} to convert to bytes
	 * @return the UTF-8 encoded bytes
	 */
	private byte[] stringToBytes(String str)
	{
		if(str == null)
			return "".getBytes( UTF8 );
		
		return str.getBytes( UTF8 );
	}
	
	/**
	 * Utility method to strip a given character off the start and end of a string
	 * 
	 * @param original the original string
	 * @param toStrip the character to strip
	 * @return the original string with matching characters stripped from the start and end of the
	 *         string
	 */
	private String strip(String original, char toStrip)
	{
		return original.replaceAll(toStrip+"$|^"+toStrip, "");
	}

	/**
	 * Extremely simple method to turn a map into a JSON string representation
	 * @return a JSON string of the map contents
	 */
	private String asJSON(Map<Object, Object> data)
	{
		List<String> jsonItems = new ArrayList<>();
		for( Entry entry : data.entrySet() )
		{
			Object key = entry.getKey();
			Object value = entry.getValue();
			if( key != null && value != null )
			{
				String keyStr = escape( key.toString() );
				String valueStr = asJSON( value );
				if( valueStr != null )
					jsonItems.add( "\"" + keyStr + "\":" + valueStr );
			}
		}
		return "{" + jsonItems.stream().collect( Collectors.joining( "," ) ) + "}";
	}
	
	/**
	 * Extremely simple method to turn a map into a JSON string representation
	 * @return a JSON string of the map contents
	 */
	private String asJSON(Object val)
	{
		if(val == null)
			return null;
		else if(val instanceof String)
			return "\""+escape(val.toString())+"\"";
		else if(val instanceof Character)
			return "\""+val.toString()+"\"";
		else if(val instanceof Boolean)
			return Boolean.toString( (Boolean)val );
		else if(val instanceof Number)
			return val.toString();
		else if(val instanceof Double)
		{
			Double dbl = (Double)val; 
			if((dbl.isInfinite() || dbl.isNaN()))
				return "null";
			return Double.toString( (Double)val );
		}
		else if(val instanceof Float)
		{
			Float flt = (Float)val; 
			if((flt.isInfinite() || flt.isNaN()))
				return "null";
			return Double.toString( (Double)val );
		}
		else if( val instanceof Collection || 
				 val instanceof String[] || val instanceof char[] ||
			     val instanceof short[] || val instanceof int[] || val instanceof long[] ||
			     val instanceof double[] || val instanceof float[] ||
			     val instanceof boolean[] ||
			     val instanceof Object[]
			    )
		{
			return "["+Stream.of(val).map( x -> asJSON(x) ).collect( Collectors.joining(",") )+"]";
		}
		else if( val instanceof Map )
		{
			return asJSON((Map)val);
		}
		return null;
	}
	
	/**
	 * Utility method to escape strings for use in a JSON string
	 * 
	 * @param s the {@link String} to be escaped
	 * @return the escaped string, sfae for use in a JSON string
	 */
	private String escape(String s)
	{
		StringBuilder sb = new StringBuilder();
		final int len = s.length();
		for( int i = 0; i < len; i++ )
		{
			char ch = s.charAt( i );
			switch( ch )
			{
				case '"':
					sb.append( "\\\"" );
					break;
				case '\\':
					sb.append( "\\\\" );
					break;
				case '\b':
					sb.append( "\\b" );
					break;
				case '\f':
					sb.append( "\\f" );
					break;
				case '\n':
					sb.append( "\\n" );
					break;
				case '\r':
					sb.append( "\\r" );
					break;
				case '\t':
					sb.append( "\\t" );
					break;
				case '/':
					sb.append( "\\/" );
					break;
				default:
					//Reference: http://www.unicode.org/versions/Unicode5.1.0/
					if( (ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F') ||
					    (ch >= '\u2000' && ch <= '\u20FF') )
					{
						String ss = Integer.toHexString( ch );
						sb.append( "\\u" );
						for( int k = 0; k < 4 - ss.length(); k++ )
						{
							sb.append( '0' );
						}
						sb.append( ss.toUpperCase() );
					}
					else
					{
						sb.append( ch );
					}
			}
		}
		return sb.toString();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// HttpHandler Implementations ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * A very simple class to provide an HTTP response with an error code and message
	 */
	private class SimpleErrorHandler implements HttpHandler
	{
		private int code;
		private String errorHTML;

		/**
		 * Constructor
		 * 
		 * @param code the HTTP code to response with
		 * @param errorHTML the error message to respond with
		 */
		public SimpleErrorHandler( int code, String errorHTML )
		{
			this.code = code;
			this.errorHTML = errorHTML;
		}

		@Override
		public void handle( HttpExchange httpExchange )
		    throws IOException
		{
			addResponseHeader( httpExchange, CONTENT_TYPE, TEXT_PLAIN_UTF8 );
			doHTMLResponse( httpExchange, this.code, errorHTML );
		}
	}
	
	/**
	 * An abstract implementation of the {@link HttpHandler} interface which accepts only certain
	 * (specified) request methods, such as GET, POST, etc
	 * 
	 * Requests with unacceptable methods will receive an HTTP 405 METHOD NOT ALLOWED response.
	 * 
	 * Requests with accepted methods will be handled by the implementing class.
	 */
	private abstract class RequestMethodHandler implements HttpHandler
	{
		private Set<String> allowedMethods;

		/**
		 * Constructor
		 * 
		 * @param allowedMethods the allowed request methods
		 */
		public RequestMethodHandler( String... allowedMethods )
		{
			this.allowedMethods =
			    Stream.of( allowedMethods ).map( m -> m.toUpperCase() ).collect(
			                                                                     Collectors.toSet() );
		}

		@Override
		public void handle( HttpExchange httpExchange )
		    throws IOException
		{
			String requestMethod = httpExchange.getRequestMethod().toUpperCase();
			boolean isValid = this.allowedMethods.contains( requestMethod );
			if( isValid )
				handleRequest( httpExchange );
			else
			{
				// do an HTTP 405 METHOD NOT ALLOWED response 
				addResponseHeader( httpExchange, CONTENT_TYPE, TEXT_HTML_UTF8 );
				addResponseHeader( httpExchange, "Allow", String.join( ",", this.allowedMethods ) );
				// construct error message saying what method was rejected, and 
				// what methods *are* accepted
				StringBuilder html = new StringBuilder();
				html.append( "<html><head><meta charset=\"UTF-8\" /></head><body>");
				html.append( "<h1>"+HTTP_405_METHOD_NOT_ALLOWED+" - Method not allowed: ");
				html.append( requestMethod.toUpperCase()+"</h1><hr>" );
				html.append( "<p>Try again with one of these allowed request methods:<ul> ");
				for(String method : this.allowedMethods)
				{
					html.append( "<li>"+method+"</li>");
				}
				html.append( "</ul></p><hr>");
				html.append( "<pre>"+ Constants.UCEF_LOGO+"<pre></body></html>" );
				doHTMLResponse( httpExchange, HTTP_405_METHOD_NOT_ALLOWED, html.toString() );
			}
		}

		/**
		 * Extending classes must implement this method to handle requests which use an allowed
		 * request method
		 * 
		 * @param httpExchange the {@link HTTPExchange} instance containing the reques and to use
		 *            to create the response
		 * @throws IOException
		 */
		public abstract void handleRequest( HttpExchange httpExchange ) throws IOException;
	}
	
	/**
	 * A abstract implementation of the {@link RequestMethodHandler} which accepts only GET
	 * requests.
	 * 
	 * Requests using other methods will receive an HTTP 405 METHOD NOT ALLOWED response.
	 */
	private abstract class GETRequestHandler extends RequestMethodHandler
	{
		public GETRequestHandler() { super("GET"); }
		
		@Override
		public abstract void handleRequest(HttpExchange httpExchange) throws IOException;
	}
	
	/**
	 * A abstract implementation of the {@link RequestMethodHandler} which accepts only POST
	 * requests
	 * 
	 * Requests using other methods will receive an HTTP 405 METHOD NOT ALLOWED response.
	 */
	private abstract class POSTRequestHandler extends RequestMethodHandler
	{
		public POSTRequestHandler() { super("POST"); }
		
		@Override
		public abstract void handleRequest(HttpExchange httpExchange) throws IOException;
	}
	
	/**
	 * A handler for index requests
	 * 
	 * Response with an HTML page listing the various endpoints with clickable links
	 */
	private class IndexHandler extends GETRequestHandler
	{
		private Map<String,HttpHandler> endpointsMap;

		public IndexHandler(Map<String, HttpHandler> endpoints)
		{
			this.endpointsMap = endpoints;
		}
		
		@Override
		public void handleRequest( HttpExchange httpExchange ) throws IOException
		{
			String path = extractPath(httpExchange);
			HttpHandler handler = this.endpointsMap.get(path); 
			
			// no handler found, or the index handler, is considered a 404 NOT FOUND error
			boolean is404 = handler == null || handler.equals( this );
			
			// compile a list of valid endpoint links
			List<String> hrefs = this.endpointsMap.entrySet()
				.stream()
				.filter(endpoint -> !(endpoint.getValue().equals(this))) // don't include index pages
				.map(endpoint -> endpoint.getKey())
				.collect( Collectors.toList() );
			
			StringBuilder html = new StringBuilder();
			html.append( "<html>");
			html.append( "<head>");
			html.append( "<meta charset=\"UTF-8\" />");
			html.append( "<style>");
			html.append( "a{text-decoration:none;}");
			html.append( "</style>");
			html.append( "</head>");
			
			html.append( "<body>" );
			if(is404)
			{
				html.append( "<h1>404 - Not Found</h1><hr>");
				html.append( "<p>Valid endpoints:</p>" );
			}
			html.append( "<ul>" );
			for(String href : hrefs )
			{
				html.append( "<li><a href=\""+href+"\">"+href+"</a></li>" );
			}
			html.append( "</ul><hr>" );
			html.append( "<pre>\r\n"+Constants.UCEF_LOGO+"\r\n</pre>" );
			html.append( "</body></html>" );
			
			String response = html.toString();
			int code = is404 ? HTTP_404_NOT_FOUND : HTTP_200_OK;
			doHTMLResponse( httpExchange, code,response );
		}
	}
	
	/**
	 * A handler for status GET requests
	 * 
	 * Response is a JSON object of the form...
	 * 
	 * {"timestamp":TIMESTAMP, "path":REQUEST_PATH, "response":LIFECYCLE_STATE}
	 * 
	 * ...where:
	 * 
	 *  - TIMESTAMP       is the system time in milliseconds at the time the request was processed
	 *  - PATH            is the URL path segment of the query
	 *  - LIFECYCLE_STATE is the label of the current lifecycle state of the federation
	 */
	private class StatusQueryHandler extends GETRequestHandler
	{
		@Override
		public void handleRequest( HttpExchange httpExchange ) throws IOException
		{
			Map<String,Object> json = new HashMap<>();
			addTimestampAndPath(json, httpExchange);
			json.put( "response", fedManFederate.getLifecycleState().getLabel() );
			doJSONResponse( httpExchange, json );
		}
	}

	/**
	 * A handler for status GET requests as to whether the simulation can start or not (i.e, have
	 * the start conditions been met yet?)
	 * 
	 * Response is a JSON object of the form...
	 * 
	 * {"timestamp":TIMESTAMP, "path":REQUEST_PATH, "response":CAN_START}
	 * 
	 * ...where:
	 * 
	 *  - TIMESTAMP       is the system time in milliseconds at the time the request was processed
	 *  - PATH            is the URL path segment of the query
	 *  - CAN_START       true if the start conditions have been met, false otherwise
	 */
	private class CanStartQueryHandler extends GETRequestHandler
	{
		@Override
		public void handleRequest( HttpExchange httpExchange ) throws IOException
		{
			Map<String,Object> json = new HashMap<>();
			addTimestampAndPath(json, httpExchange);
			json.put( "response", fedManFederate.getStartRequirements().canStart() );
			doJSONResponse( httpExchange, json );
		}
	}
	
	/**
	 * A handler which produces an HTML table summarizing the start conditions to be met for the
	 * simulation to be able to start
	 */
	private class StartConditionQueryHandler extends GETRequestHandler
	{
		@Override
		public void handleRequest( HttpExchange httpExchange ) throws IOException
		{
			FedManStartRequirements requirements = fedManFederate.getStartRequirements();
			StringBuilder html = new StringBuilder();
			html.append( "<html>");
			html.append( "<head>");
			html.append( "<meta charset=\"UTF-8\">");
			html.append( "<style>");
			html.append( "table{border-collapse:collapse;}");
			html.append( "table, tr, th, td{margin:0;padding:4px;border:1px solid black;text-align:left;}");
			html.append( "</style>");
			html.append( "</head>");
			
			html.append( "<body>" );
			html.append( "<pre>\r\n"+Constants.UCEF_LOGO+"\r\n</pre>" );
			
			html.append( "<table><thead><tr>" );
			for(String heading : FedManConstants.TABLE_HEADINGS)
				html.append("<th>"+heading+"</th>");
			html.append( "</tr></thead><tfoot /><tbody>" );
			
			// sorted federate types list	
			List<String> federateTypes = new ArrayList<>(requirements.startRequirements.keySet());
			federateTypes.sort( null );
			for( String federateType : federateTypes )
			{
				html.append("<tr>");
				html.append("<td>"+federateType+"</td>");
				html.append("<td>"+requirements.startRequirements.get( federateType )+"</td>");
				html.append("<td>"+requirements.joinedFederatesByType.getOrDefault( federateType, Collections.emptySet() ).size()+"</td>");
				html.append("</tr>");
			}
			html.append( "</tbody></table>" );
			
			html.append( "</html></body>" );
			
			doHTMLResponse( httpExchange, html.toString() );
		}
	}
	
	/**
	 * A handler for status GET requests as to whether the simulation has started
	 * 
	 * Response is a JSON object of the form...
	 * 
	 * {"timestamp":TIMESTAMP, "path":REQUEST_PATH, "response":HAS_STARTED}
	 * 
	 * ...where:
	 * 
	 *  - TIMESTAMP       is the system time in milliseconds at the time the request was processed
	 *  - PATH            is the URL path segment of the query
	 *  - HAS_STARTED     true if the simulation has started, false otherwise
	 */
	private class HasStartedQueryHandler extends GETRequestHandler
	{
		@Override
		public void handleRequest( HttpExchange httpExchange ) throws IOException
		{
			Map<String,Object> json = new HashMap<>();
			addTimestampAndPath(json, httpExchange);
			json.put( "response", fedManFederate.hasStarted() );
			doJSONResponse( httpExchange, json );
		}
	}
	
	/**
	 * A handler for status GET requests as to whether the simulation has ended
	 * 
	 * Response is a JSON object of the form...
	 * 
	 * {"timestamp":TIMESTAMP, "path":REQUEST_PATH, "response":HAS_ENDED}
	 * 
	 * ...where:
	 * 
	 *  - TIMESTAMP       is the system time in milliseconds at the time the request was processed
	 *  - PATH            is the URL path segment of the query
	 *  - HAS_ENDED       true if the simulation has ended, false otherwise
	 */
	private class HasEndedQueryHandler extends GETRequestHandler
	{
		@Override
		public void handleRequest( HttpExchange httpExchange ) throws IOException
		{
			Map<String,Object> json = new HashMap<>();
			addTimestampAndPath(json, httpExchange);
			json.put( "response", fedManFederate.hasEnded() );
			doJSONResponse( httpExchange, json );
		}
	}
	
	/**
	 * A handler for status GET requests as to whether the simulation is currently paused
	 * 
	 * Response is a JSON object of the form...
	 * 
	 * {"timestamp":TIMESTAMP, "path":REQUEST_PATH, "response":IS_PAUSED}
	 * 
	 * ...where:
	 * 
	 *  - TIMESTAMP       is the system time in milliseconds at the time the request was processed
	 *  - PATH            is the URL path segment of the query
	 *  - IS_PAUSED       true if the simulation is paused, false otherwise
	 */
	private class IsPausedQueryHandler extends GETRequestHandler
	{
		@Override
		public void handleRequest( HttpExchange httpExchange ) throws IOException
		{
			Map<String,Object> json = new HashMap<>();
			addTimestampAndPath(json, httpExchange);
			json.put( "response", fedManFederate.isPaused() );
			doJSONResponse( httpExchange, json );
		}
	}
	
	/**
	 * A handler for status GET requests as to whether the simulation is currently running
	 * 
	 * Response is a JSON object of the form...
	 * 
	 * {"timestamp":TIMESTAMP, "path":REQUEST_PATH, "response":IS_RUNNING}
	 * 
	 * ...where:
	 * 
	 *  - TIMESTAMP       is the system time in milliseconds at the time the request was processed
	 *  - PATH            is the URL path segment of the query
	 *  - IS_RUNNING      true if the simulation is running, false otherwise
	 */
	private class IsRunningQueryHandler extends GETRequestHandler
	{
		@Override
		public void handleRequest( HttpExchange httpExchange ) throws IOException
		{
			Map<String,Object> json = new HashMap<>();
			addTimestampAndPath(json, httpExchange);
			json.put( "response", fedManFederate.isRunning() );
			doJSONResponse( httpExchange, json );
		}
	}
	
	/**
	 * A handler for status POST requests to pause the simulation
	 * 
	 * Response is a JSON object of the form...
	 * 
	 * {"timestamp":TIMESTAMP, "path":REQUEST_PATH, "response":OK_OR_FAILED, 
	 *  "success":SUCCESS, "error":ERROR_MSG}
	 * 
	 * ...where:
	 * 
	 *  - TIMESTAMP       is the system time in milliseconds at the time the request was processed
	 *  - PATH            is the URL path segment of the query
	 *  - OK_OR_FAILED    "OK": if the request succeeded, "FAILED" otherwise
	 *  - SUCCESS         true: if the request succeeded, false otherwise
	 *  - ERROR_MSG       an explanatory error message in the case that the request failed,
	 *                    otherwise this key/value is absent from the JSON
	 */
	private class PauseCommandHandler extends POSTRequestHandler
	{
		@Override
		public void handleRequest( HttpExchange httpExchange ) throws IOException
		{
			int code = HTTP_200_OK;
			String error = "";
			
			if(!fedManFederate.hasStarted())
			{
				code = HTTP_400_BAD_REQUEST;
				error = "Simulation has not yet started";
			}
			else if(fedManFederate.isPaused())
			{
				code = HTTP_400_BAD_REQUEST;
				error = "Simulation is already paused";
			}
			else if(fedManFederate.hasEnded())
			{
				code = HTTP_400_BAD_REQUEST;
				error = "Simulation has already ended";
			}
			else
			{
				fedManFederate.requestSimPause();
			}
			
			boolean isOK = code == HTTP_200_OK;
			Map<String,Object> json = new HashMap<>();
			addTimestampAndPath(json, httpExchange);
			json.put( "response", isOK ? "OK" : "FAILED" );
			json.put( "success", isOK );
			if( !isOK )
				json.put( "error", error );
			doJSONResponse( httpExchange, code, json );
		}
	}
	
	/**
	 * A handler for status POST requests to resume the simulation
	 * 
	 * Response is a JSON object of the form...
	 * 
	 * {"timestamp":TIMESTAMP, "path":REQUEST_PATH, "response":OK_OR_FAILED, 
	 *  "success":SUCCESS, "error":ERROR_MSG}
	 * 
	 * ...where:
	 * 
	 *  - TIMESTAMP       is the system time in milliseconds at the time the request was processed
	 *  - PATH            is the URL path segment of the query
	 *  - OK_OR_FAILED    "OK": if the request succeeded, "FAILED" otherwise
	 *  - SUCCESS         true: if the request succeeded, false otherwise
	 *  - ERROR_MSG       an explanatory error message in the case that the request failed,
	 *                    otherwise this key/value is absent from the JSON
	 */
	private class ResumeCommandHandler extends POSTRequestHandler
	{
		@Override
		public void handleRequest( HttpExchange httpExchange ) throws IOException
		{
			int code = HTTP_200_OK;
			String error = "";
			
			if(!fedManFederate.hasStarted())
			{
				code = HTTP_400_BAD_REQUEST;
				error = "Simulation has not yet started";
			}
			else if(fedManFederate.isRunning())
			{
				code = HTTP_400_BAD_REQUEST;
				error = "Simulation is not paused";
			}
			else if(fedManFederate.hasEnded())
			{
				code = HTTP_400_BAD_REQUEST;
				error = "Simulation has already ended";
			}
			else
			{
				fedManFederate.requestSimResume();
			}
			
			boolean isOK = code == HTTP_200_OK;
			Map<String,Object> json = new HashMap<>();
			addTimestampAndPath(json, httpExchange);
			json.put( "response", isOK ? "OK" : "FAILED" );
			json.put( "success", isOK );
			if( !isOK )
				json.put( "error", error );
			doJSONResponse( httpExchange, code, json );
		}
	}
	
	/**
	 * A handler for status POST requests to start the simulation
	 * 
	 * Response is a JSON object of the form...
	 * 
	 * {"timestamp":TIMESTAMP, "path":REQUEST_PATH, "response":OK_OR_FAILED, 
	 *  "success":SUCCESS, "error":ERROR_MSG}
	 * 
	 * ...where:
	 * 
	 *  - TIMESTAMP       is the system time in milliseconds at the time the request was processed
	 *  - PATH            is the URL path segment of the query
	 *  - OK_OR_FAILED    "OK": if the request succeeded, "FAILED" otherwise
	 *  - SUCCESS         true: if the request succeeded, false otherwise
	 *  - ERROR_MSG       an explanatory error message in the case that the request failed,
	 *                    otherwise this key/value is absent from the JSON
	 */
	private class StartCommandHandler extends POSTRequestHandler
	{
		@Override
		public void handleRequest( HttpExchange httpExchange ) throws IOException
		{
			int code = HTTP_200_OK;
			String error = "";
			
			if( !fedManFederate.canStart() )
			{
				code = HTTP_400_BAD_REQUEST;
				error = "Simulation start conditions have not yet been met";
			}
			else if(fedManFederate.hasStarted())
			{
				code = HTTP_400_BAD_REQUEST;
				error = "Simulation has already started";
			}
			else if( fedManFederate.hasEnded() )
			{
				code = HTTP_400_BAD_REQUEST;
				error = "Simulation has already ended";
			}
			else
			{
				fedManFederate.requestSimStart();
			}
			
			boolean isOK = code == HTTP_200_OK;
			Map<String,Object> json = new HashMap<>();
			addTimestampAndPath(json, httpExchange);
			json.put( "response", isOK ? "OK" : "FAILED" );
			json.put( "success", isOK );
			if( !isOK )
				json.put( "error", error );
			doJSONResponse( httpExchange, code, json );
		}
	}
	
	/**
	 * A handler for status POST requests to end the simulation
	 * 
	 * Response is a JSON object of the form...
	 * 
	 * {"timestamp":TIMESTAMP, "path":REQUEST_PATH, "response":OK_OR_FAILED, 
	 *  "success":SUCCESS, "error":ERROR_MSG}
	 * 
	 * ...where:
	 * 
	 *  - TIMESTAMP       is the system time in milliseconds at the time the request was processed
	 *  - PATH            is the URL path segment of the query
	 *  - OK_OR_FAILED    "OK": if the request succeeded, "FAILED" otherwise
	 *  - SUCCESS         true: if the request succeeded, false otherwise
	 *  - ERROR_MSG       an explanatory error message in the case that the request failed,
	 *                    otherwise this key/value is absent from the JSON
	 */
	private class EndCommandHandler extends POSTRequestHandler
	{
		@Override
		public void handleRequest( HttpExchange httpExchange ) throws IOException
		{
			int code = HTTP_200_OK;
			String error = "OK";
			
			if(!fedManFederate.hasStarted())
			{
				code = HTTP_400_BAD_REQUEST;
				error = "Simulation has not yet started";
			}
			else if( fedManFederate.hasEnded() )
			{
				code = HTTP_400_BAD_REQUEST;
				error = "Simulation has already ended";
			}
			else
			{
				fedManFederate.requestSimEnd();
			}
			
			boolean isOK = code == HTTP_200_OK;
			Map<String,Object> json = new HashMap<>();
			addTimestampAndPath(json, httpExchange);
			json.put( "response", isOK ? "OK" : "FAILED" );
			json.put( "success", isOK );
			if( !isOK )
				json.put( "error", error );
			doJSONResponse( httpExchange, code, json );
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Test code only
	 * @param args
	 */
	public static void main(String[] args)
	{
		String[] testArgs = {
		    "--federation", "TheFederation",		                 
		    "--require", "FedABC,1",                 
		    "--require", "FedXYZ,2",                 
		}; 
		FedManHttpServer testing = new FedManHttpServer(new FedManFederate(testArgs), 8080);
		testing.startServer();
	}
}
