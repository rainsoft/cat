package com.dianping.cat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.unidal.eunit.helper.Files;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CookieFilter implements Filter {

	private static Map<String, Item> validateCookies;

	private static final String KEY = "neocortex-4j.cookies.whiteList";

	public static void main(String args[]) throws Exception {
		String value = Files.forIO().readFrom(new File("/data/appdatas/cat/cookies"), "utf-8");
		JsonElement element = new JsonParser().parse(value);
		JsonArray array = element.getAsJsonArray();
		List<Item> items = new ArrayList<Item>();

		for (int i = 0; i < array.size(); i++) {
			JsonObject str = array.get(i).getAsJsonObject();
			String name = str.get("name").getAsString();
			String domain = str.get("domain").getAsString();
			String path = str.get("path").getAsString();

			items.add(new Item(name, domain, path));
		}

		validateCookies = new HashMap<String, Item>();

		for (Item item : items) {
			validateCookies.put(item.getName(), item);

			System.out.println(item.toString());
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	      ServletException {
		try {
			HttpServletRequest httpServletRequest = (HttpServletRequest) request;
			HttpServletResponse httpServletResponse = (HttpServletResponse) response;
			Cookie[] cookies = httpServletRequest.getCookies();

			String domain = httpServletRequest.getServerName();
			String path = httpServletRequest.getRequestURI();

			System.out.println("domain:" + domain + " path:" + path);
			if (cookies != null) {
				for (Cookie c : cookies) {
					String name = c.getName();

					if (validateCookies != null) {
						Item item = validateCookies.get(name);

						if (item == null || !item.getPath().equals(path) || !item.getDomain().equals(domain)) {
							Cookie temp = new Cookie(name, null);

							temp.setDomain(".dianping.com");
							temp.setMaxAge(0);
							temp.setPath("/");
							httpServletResponse.addCookie(temp);
							System.err.println("kill  " + c.getName() +" " +temp.getPath()+" "+temp.getDomain());
						} else {
							System.err.println("not kill 222 " + c.getName());
						}
					} else {
						System.out.println("map is null");
					}
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		chain.doFilter(request, response);
	}
	
	

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		try {
			// ConfigCache config = ConfigCache.getInstance();
			String value = Files.forIO().readFrom(new File("/data/appdatas/cat/cookies"), "utf-8");
			// String value = config.getProperty(KEY);
			JsonElement element = new JsonParser().parse(value);
			JsonArray array = element.getAsJsonArray();
			List<Item> items = new ArrayList<Item>();

			for (int i = 0; i < array.size(); i++) {
				JsonObject str = array.get(i).getAsJsonObject();
				String name = str.get("name").getAsString();
				String domain = str.get("domain").getAsString();
				String path = str.get("path").getAsString();

				items.add(new Item(name, domain, path));
			}

			validateCookies = new HashMap<String, Item>();

			for (Item item : items) {
				validateCookies.put(item.getName(), item);
				System.out.println("put:" + item.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			Cat.logError(e);
		}
	}

	public static class Item {
		private String name;

		private String domain;

		private String path;

		public Item(String name, String domain, String path) {
			this.name = name;
			this.domain = domain;
			this.path = path;
		}

		public String getDomain() {
			return domain;
		}

		public String getName() {
			return name;
		}

		public String getPath() {
			return path;
		}

		@Override
		public String toString() {
			return "Item [name=" + name + ", domain=" + domain + ", path=" + path + "]";
		}
	}

}
