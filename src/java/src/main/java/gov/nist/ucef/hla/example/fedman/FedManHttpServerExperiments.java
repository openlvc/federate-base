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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.ws.http.HTTPBinding;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import com.sun.net.httpserver.HttpServer;

public class FedManHttpServerExperiments
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	HttpServer server;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FedManHttpServerExperiments()
	{
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void go()
	{
		ExecutorService executor = Executors.newFixedThreadPool(4);
		try
		{
			server = HttpServer.create( new InetSocketAddress( 8080 ), 0 );
			server.setExecutor(executor);

			server.createContext( "/", httpExchange -> {
				System.out.println("Sleeping..."+Thread.currentThread().getId());
				try
				{
					Thread.sleep(5*1000);
				}
				catch( InterruptedException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
    			byte response[] = "404 - Not Found".getBytes( "UTF-8" );
    			httpExchange.getResponseHeaders().add( "Content-Type", "text/plain; charset=UTF-8" );
    			httpExchange.sendResponseHeaders( 404, response.length );
    			OutputStream out = httpExchange.getResponseBody();
    			out.write( response );
    			out.close();
			} );
			/*
			server.createContext( "/", httpExchange -> {
				byte response[] = "Hello, World!".getBytes( "UTF-8" );
				httpExchange.getResponseHeaders().add( "Content-Type", "text/plain; charset=UTF-8" );
				httpExchange.sendResponseHeaders( 200, response.length );
				OutputStream out = httpExchange.getResponseBody();
				out.write( response );
				out.close();
			} );
			 */
			server.createContext( "/bananas/", httpExchange -> {
				byte response[] = "Hello, Bananas!".getBytes( "UTF-8" );
				httpExchange.getResponseHeaders().add( "Content-Type", "text/plain; charset=UTF-8" );
				httpExchange.sendResponseHeaders( 200, response.length );
				OutputStream out = httpExchange.getResponseBody();
				out.write( response );
				out.close();
			} );
			server.createContext( "/bananas/apples/", httpExchange -> {
				byte response[] = "Hello, Banana Apples!".getBytes( "UTF-8" );
				httpExchange.getResponseHeaders().add( "Content-Type", "text/plain; charset=UTF-8" );
				httpExchange.sendResponseHeaders( 200, response.length );
				OutputStream out = httpExchange.getResponseBody();
				out.write( response );
				out.close();
			} );

			server.start();
		}
		catch( Throwable tr )
		{
			tr.printStackTrace();
		}
	}
	
	private class FooHandler implements Runnable
	{
		private HttpExchange httpExchange;

		public FooHandler(HttpExchange httpExchange)
		{
			this.httpExchange = httpExchange;
		}
		
		@Override
		public void run()
		{
			try
			{
				System.out.println("Sleeping...");
				Thread.sleep(15*1000);
				
    			byte response[] = "404 - Not Found".getBytes( "UTF-8" );
    			httpExchange.getResponseHeaders().add( "Content-Type", "text/plain; charset=UTF-8" );
    			httpExchange.sendResponseHeaders( 404, response.length );
    			OutputStream out = httpExchange.getResponseBody();
    			out.write( response );
    			out.close();
			}
			catch( Exception e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void main(String[] args)
	{
		FedManHttpServerExperiments foo = new FedManHttpServerExperiments();
		foo.go();
	}
}







