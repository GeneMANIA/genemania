/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2010 University of Toronto.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * HttpRetriever
 * Created Oct 31, 2008
 * @author Ovi Comes
 */
package org.genemania.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

public class HttpRetriever {

	
	// __[constructors]________________________________________________________
	public HttpRetriever() {
	}
	
	// __[public interface]____________________________________________________
	public String getContent(String url, String fromToken, String toToken) {
		String ret;
		String page = fetchPage(url);
		ret = scrapContent(page, fromToken, toToken);
		return ret;
	}
	
	public String post(String url, Hashtable<String, String> params) {
		String ret = "";
		try {
			HttpClient client = new HttpClient();
	        HttpMethodBase method = new PostMethod(url);
	        Enumeration<String> paramNames = params.keys();
	        while(paramNames.hasMoreElements()) {
	        	String nextParamName = paramNames.nextElement();
	        	String nextParamValue = params.get(nextParamName);
	            ((PostMethod)(method)).addParameter(nextParamName, nextParamValue);
	        }
	        int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				System.out.println("HttpRetriever error: " + method.getStatusLine());
			} else {
				byte[] responseBody = method.getResponseBody();
				method.releaseConnection();
				ret = new String(responseBody);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	// __[private helpers]_____________________________________________________
	private String fetchPage(String url) {
		String ret = "";
		try {
			HttpClient client = new HttpClient();
			HttpMethod method = new GetMethod(url);
			int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				System.out.println("HttpRetriever error: " + method.getStatusLine());
			} else {
				byte[] responseBody = method.getResponseBody();
				method.releaseConnection();
				ret = new String(responseBody);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	private String scrapContent(String content, String fromToken, String toToken) {
		String ret = "";
		int fromIndex = content.indexOf(fromToken);
		if(fromIndex > 0) {
			fromIndex += fromToken.length();
			int toIndex = content.indexOf(toToken, fromIndex);
			if(toIndex > 0) {
				ret = content.substring(fromIndex, toIndex);
			}
		}
		return ret;
	}
	
	public static void main(String[] args) {
		StringBuffer result = new StringBuffer(); 
		HttpRetriever r = new HttpRetriever();
		List<String> titles = getTitles();
		for(String title:titles) {
System.out.println("processing " + title);		
			String escTitle = StringUtils.replace(title, " ", "%20");		
			String url = "http://www.imdb.com/find?q='" + escTitle + "'";
			String from = "<h5>Plot:</h5>";
			String to = "<a class=\"tn15more inline\"";
			String fragment = r.getContent(url, from, to);
			if(StringUtils.isEmpty(fragment)) {
				String from1 = "(new Image()).src='/rg/find-title-1/title_popular/images/b.gif?link=";
				String to1 = "/';\">";// + title;
				String url1 = "http://www.imdb.com" + r.getContent(url, from1, to1);
				from = "<h5>Plot:</h5>";
				to = "<a class=\"tn15more inline\"";
				fragment = r.getContent(url1, from, to);
			}
			result.append(title + ":" + fragment + "\n");
		}
System.out.println("done.");		
System.out.println("============================================================");
System.out.println(result.toString());		
System.out.println("============================================================");
	}

	private static List<String> getTitles() {
		List<String> ret = new ArrayList<String>();
//		ret.add("12 Monkeys");
//		ret.add("A Few Good Men");
//		ret.add("A League of Their Own");
//		ret.add("Animal House");
//		ret.add("Apocalypse Now");
//		ret.add("Apollo 13");
//		ret.add("As Good As It Gets");
//		ret.add("Attack of the Clones");
//		ret.add("Austin Powers International Man of Mystery");
//		ret.add("Austin Powers Spy who Shagged Me");
//		ret.add("Austin Powers as Goldmember");
//		ret.add("Babel");
//		ret.add("Batman");
//		ret.add("Batman Forever");
//		ret.add("Be Kind Rewind");
//		ret.add("Beauty Shop");
//		ret.add("Benjamin Button");
//		ret.add("Bull Durham");
//		ret.add("Burn After Reading");
//		ret.add("Caddyshack");
//		ret.add("Charlie's Angels");
//		ret.add("Chicago");
//		ret.add("Cold Mountain");
//		ret.add("Dazed and Confused");
//		ret.add("Dead Man Walking");
		ret.add("Dick Tracy");
//		ret.add("Dirty Rotten Scoundrels");
//		ret.add("Easy Rider");
//		ret.add("Everyone Says I Love You");
//		ret.add("Field of Dreams");
//		ret.add("Fight Club");
		ret.add("Flatliners");
//		ret.add("Footloose");
//		ret.add("Forrest Gump");
//		ret.add("GI Jane");
//		ret.add("Get Smart");
//		ret.add("Ghost");
//		ret.add("Ghostbusters");
//		ret.add("Groundhog Day");
//		ret.add("High Fidelity");
//		ret.add("Hook");
//		ret.add("I Heart Huckabees");
//		ret.add("Interview with the Vampire");
//		ret.add("JFK");
//		ret.add("Jerry Maguire");
//		ret.add("Kalifornia");
//		ret.add("Kramer vs Kramer");
//		ret.add("Little Shop of Horrors");
//		ret.add("Lost in Translation");
//		ret.add("Mars Attacks");
//		ret.add("Matrix");
//		ret.add("Matrix Reloaded");
//		ret.add("Matrix Revolutions");
		ret.add("Meet the Fockers");
//		ret.add("Meet the Parents");
		ret.add("Men in Black");
//		ret.add("Men in Black II");
//		ret.add("Milk");
//		ret.add("Miss Congeniality");
//		ret.add("Monsters Ball");
//		ret.add("Mr and Mrs Smith");
//		ret.add("Murder in the First");
//		ret.add("Mystic River");
//		ret.add("Natural Born Killers");
//		ret.add("No Country for Old Men");
//		ret.add("Oceans 11");
//		ret.add("Oceans 13");
//		ret.add("Pink Panther");
//		ret.add("Pink Panther 2");
//		ret.add("Planes Trains and Automobiles");
//		ret.add("Pretty Woman");
		ret.add("Primal Fear");
//		ret.add("Rain Man");
//		ret.add("Revenge of the Sith");
//		ret.add("Royal Tenenbaums");
//		ret.add("Saving Private Ryan");
//		ret.add("St Elmos Fire");
//		ret.add("Steel Magnolias");
//		ret.add("The Big Chill");
//		ret.add("The Color Purple");
//		ret.add("The Departed");
//		ret.add("The Fugitive");
//		ret.add("The Graduate");
//		ret.add("The Italian Job");
//		ret.add("The Three Amigos");
//		ret.add("Thelma and Louise");
//		ret.add("Tootsie");
//		ret.add("Top Gun");
//		ret.add("Traffic");
//		ret.add("Tropic Thunder");
//		ret.add("Waterworld");
		return ret;
	}
	
}
