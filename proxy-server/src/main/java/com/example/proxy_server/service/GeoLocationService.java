package com.example.proxy_server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@Service
public class GeoLocationService {

    private static final String GEO_API_URL = "http://ip-api.com/json/";

    public String getCountryCode(String publicIp) {
        try {
            URL url = new URL(GEO_API_URL + publicIp);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            connection.disconnect();

            // Parse the response to get the country code
            JSONObject jsonObject = new JSONObject(content.toString());
            return jsonObject.getString("countryCode");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

