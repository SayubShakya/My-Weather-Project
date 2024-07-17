package myPackage;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class MyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			// API Setup
			String apiKey = "d9e3292ae12555b5cc72b26d56dbf1f2";

			// Get the city from the form input
			String city = request.getParameter("city");
			if (city == null || city.trim().isEmpty()) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "City parameter is missing or empty.");
				return;
			}

			// Create the URL for the OpenWeatherMap API request
			String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey
					+ "&units=metric";

			// API Integeration (Connection)
			// URL Connection PASS garna lai
			@SuppressWarnings("deprecation")
			URL url = new URL(apiUrl);

			// object to establish Connection
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			// network bata data read garne
			InputStream inputStream = connection.getInputStream();
			InputStreamReader reader = new InputStreamReader(inputStream);
			// System.out.println(reader);

			// user bata input lina lai (scan garxa data)
			Scanner scanner = new Scanner(reader);

			// Store garna lai
			StringBuilder responseContent = new StringBuilder();

			while (scanner.hasNext()) {
				responseContent.append(scanner.nextLine());
			}

			// System.out.println(responseContent);
			scanner.close();
			// close connection
			connection.disconnect();

			// Parse the JSON response to extract temperature, date, and humidity
			// type casting (String to Gson)
			Gson gson = new Gson();
			JsonObject jsonObject = gson.fromJson(responseContent.toString(), JsonObject.class);
			// System.out.println(jsonObject);

			// Date & Time
			long dateTimestamp = jsonObject.get("dt").getAsLong();
			ZonedDateTime dateTime = Instant.ofEpochSecond(dateTimestamp).atZone(ZoneId.systemDefault());
			String date = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

			// Temperature
			double temperatureCelsius = jsonObject.getAsJsonObject("main").get("temp").getAsDouble();

			// Humidity
			int humidity = jsonObject.getAsJsonObject("main").get("humidity").getAsInt();

			// Wind Speed (m/s to km/h)
			double windSpeed = jsonObject.getAsJsonObject("wind").get("speed").getAsDouble() * 3.6;

			// Weather Condition
			String weatherCondition = jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject().get("main")
					.getAsString();

			// Set the data as request attributes (for sending to the jsp page)
			request.setAttribute("date", date);
			request.setAttribute("city", city);
			request.setAttribute("temperature", (int) temperatureCelsius);
			request.setAttribute("weatherCondition", weatherCondition);
			request.setAttribute("humidity", humidity);
			request.setAttribute("windSpeed", (int) windSpeed);
			request.setAttribute("weatherData", responseContent.toString());

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Forward the request to the weather.jsp page for rendering
		request.getRequestDispatcher("index.jsp").forward(request, response);
	}
}