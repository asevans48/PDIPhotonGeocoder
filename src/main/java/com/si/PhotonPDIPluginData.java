/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.si;

import com.google.gson.JsonObject;
import flexjson.JSON;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


public class PhotonPDIPluginData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface outputRowMeta;
  private HttpClient client;

  /**
   * Setup the data class
   */
  public PhotonPDIPluginData() {
    super();
    client = HttpClientBuilder.create().build();
  }

  /**
   * Package teh response
   *
   * @param response      The HttpResponse
   * @return              The latLong string array
   */
  private Map<String,String> packageResponse(HttpResponse response) throws IOException, ParseException {
    Map<String, String> hmap = new HashMap<String, String>();
    JSONParser parser = new JSONParser();
    InputStream is = response.getEntity().getContent();
    try {
      Reader reader = new InputStreamReader(is);
      try {
        JSONObject featuresObj = (JSONObject) parser.parse(reader);
        if(featuresObj != null) {
          Object jarray = featuresObj.getOrDefault("features", null);
          if(jarray != null && ((JSONArray) jarray).size() > 0){
            JSONObject jobj = (JSONObject) ((JSONArray)jarray).get(0);
            Object geoObj =  jobj.getOrDefault("geometry", null);
            if(geoObj != null){
              Object coords = ((JSONObject)geoObj).getOrDefault("coordinates", null);
              if(coords != null && ((JSONArray)coords).size() == 2){
                hmap.put("lat", String.valueOf((float) ((JSONArray)coords).get(0)));
                hmap.put("long", String.valueOf((float) ((JSONArray)coords).get(1)));
              }
            }
          }

          Object props = featuresObj.getOrDefault("properties", null);
          if(props != null ){
            JSONObject jobj = (JSONObject) props;
            Object city = jobj.getOrDefault("city", null);
            if(city != null){
              hmap.put("city", (String) city);
            }
            Object street = jobj.getOrDefault("street", null);
            if(street != null){
              hmap.put("street", (String) street);
            }
            Object hnum = jobj.getOrDefault("housenumber", null);
            if(hnum != null){
              hmap.put("housenumber", (String) hnum);
            }
            Object postCode = jobj.getOrDefault("postcode", null);
            if(postCode != null){
              hmap.put("postcode", (String) postCode);
            }
            Object state = jobj.getOrDefault("state", null);
            if(state != null){
              hmap.put("state", (String) state);
            }
            Object name = jobj.getOrDefault("name", null);
            if(name != null){
              hmap.put("name", (String) name);
            }
            Object osmValue = jobj.getOrDefault("osm_value", null);
            if(osmValue != null){
              hmap.put("osm_value", (String) osmValue);
            }
            Object country = jobj.getOrDefault("country", null);
            if(country != null){
              hmap.put("country", (String) country);
            }
          }
        }
      } finally {
        reader.close();
      }
    }finally{
      is.close();
    }
    return hmap;
  }

  /**
   * Request data from photon
   *
   * @param uri         The uri
   * @param street      The street
   * @param city        The city
   * @param state       The state
   * @param zip         The zip
   * @return            The http response
   * @throws ClientProtocolException
   * @throws IOException
   */
  public Map<String, String> photonRequest(URI uri, String street, String city, String state, String zip) throws ParseException, URISyntaxException, ClientProtocolException, IOException {
    String oAddr = street.trim()+" "+city.trim()+" "+state.trim()+" "+zip.trim();
    oAddr = URLEncoder.encode(oAddr, "UTF-8");
    URI outputURI = new URIBuilder(uri)
            .addParameter("q", oAddr)
            .addParameter("limit", "1")
            .build();
    HttpUriRequest request = new HttpGet(outputURI);
    HttpResponse response =  this.client.execute(request);
    int code = response.getStatusLine().getStatusCode();
    String reason = response.getStatusLine().getReasonPhrase();
    if(code != 200){
      throw new IOException("photon Request Failed with Status Code " + code + "\n" + reason);
    }
    return this.packageResponse(response);
  }

  /**
   * Packages geojson from mapbox to lat long array.
   *
   * @param response          The mapbox response
   * @return                  The lat long array
   */
  private Map<String, String> packageGeoJson(HttpResponse response) throws IOException, ParseException {
    Map<String, String> hmap = new HashMap<String, String>();
    JSONParser parser = new JSONParser();
    InputStream is = response.getEntity().getContent();
    try {
      Reader reader = new InputStreamReader(is);
      try {
        JSONObject fullObj = (JSONObject) parser.parse(reader);
        if(fullObj != null){
          JSONArray jarr = (JSONArray) fullObj.get("features");
          if(jarr != null && jarr.size() > 0){
            JSONObject jobj = (JSONObject) jarr.get(0);
            jarr = (JSONArray) jobj.get("center");
            if(jarr.size() == 2){
              hmap.put("lat",String.valueOf((float)jarr.get(0)));
              hmap.put("long", String.valueOf((float)jarr.get(1)));
            }
            String parsedAddr = (String) jobj.get("place_name");
            hmap.put("address", parsedAddr);
          }
        }
      } finally {
        reader.close();
      }
    }finally{
      is.close();
    }
    return hmap;
  }

  /**
   * Request data from mapbox
   *
   * @param uri         The uri
   * @param token       The mapbox token
   * @param street      The street
   * @param city        The city
   * @param state       The state
   * @param zip         The zip
   * @return            The http response
   * @throws ClientProtocolException
   * @throws IOException
   */
  public Map<String,String> mapBoxRequest(URI uri, String token, String street, String city, String state, String zip)
          throws IOException, ParseException, URISyntaxException, ClientProtocolException, IOException {
    String addr = street;
    addr = addr + " " + city;
    addr = addr.trim() + " " + state;
    addr = addr.trim() + " " + zip;
    addr = addr.trim();
    URI mboxUri = new URL(uri.toURL(), String.format("/geocoding/v5/mapbox.places/%s.json", addr)).toURI();
    mboxUri = new URIBuilder(mboxUri).addParameter("access_token", token).build();
    HttpUriRequest uriRequest = new HttpGet(mboxUri);
    HttpResponse response = this.client.execute(uriRequest);
    int code = response.getStatusLine().getStatusCode();
    String reason = response.getStatusLine().getReasonPhrase();
    if(code != 200){
      throw new IOException("Mapbox Request Failed with Status Code " + code + "\n" + reason);
    }
    return this.packageGeoJson(response);
  }
}