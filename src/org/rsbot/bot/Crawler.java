package org.rsbot.bot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.rsbot.util.GlobalConfiguration;
import org.rsbot.util.Injector;

public class Crawler {
	private static final Logger log = Logger.getLogger(Crawler.class.getName());

	private static HashMap<String, String> parameters;

	public Crawler(Injector i) {
		String root = "http://www." + i.generateTargetName() + ".com/";
		
		final String index = firstMatch("<a id=\"continue\" class=\"barItem\" href=\"([^\"]+)\"\\s+onclick=\"[^\"]+\">Continue to Full Site for News and Game Help", downloadPage(root, null));

		final String frame = root + "game.ws";

		final String game = firstMatch("<frame style=\"[^\"]+\" src=\"([^\"]+)\"", downloadPage(frame, index));

		final Pattern pattern = Pattern.compile("<param name=\"?([^\\s]+)\"?\\s+value=\"?([^>]*)\"?>", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		final Matcher matcher = pattern.matcher(downloadPage(game, frame));
		parameters = new HashMap<String, String>();
		while (matcher.find()) {
			final String key = removeTrailingChar(matcher.group(1), '"');
			final String value = removeTrailingChar(matcher.group(2), '"');
			if (!parameters.containsKey(key)) {
				parameters.put(key, value);
			}
		}

		final String ie = "haveie6";
		if (parameters.containsKey(ie)) {
			parameters.remove(ie);
		}
		parameters.put("haveie6", "0");

		log.fine("Parameters: " + parameters);
	}

	private String downloadPage(final String url, final String referer) {
		try {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(GlobalConfiguration.getURLConnection(new URL(url), referer).getInputStream()));
			final StringBuilder buf = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				buf.append(line);
			}
			reader.close();
			return buf.toString();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String firstMatch(final String regex, final String str) {
		final Pattern pattern = Pattern.compile(regex);
		final Matcher matcher = pattern.matcher(str);
		while (matcher.find())
			return matcher.group(1);
		return null;
	}

	public HashMap<String, String> getParameters() {
		return parameters;
	}

	private String removeTrailingChar(final String str, final char ch) {
		if ((str == null) || str.isEmpty())
			return str;
		else if (str.length() == 1)
			return str.charAt(0) == ch ? "" : str;
		try {
			final int l = str.length() - 1;
			if (str.charAt(l) == ch)
				return str.substring(0, l);
			return str;
		} catch (final Exception e) {
			return str;
		}
	}
}
