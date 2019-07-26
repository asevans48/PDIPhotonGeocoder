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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Describe your step plugin.
 * 
 */
public class PhotonPDIPlugin extends BaseStep implements StepInterface {
  private PhotonPDIPluginMeta meta;
  private PhotonPDIPluginData data;
  
  private static Class<?> PKG = PhotonPDIPluginMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
  
  public PhotonPDIPlugin(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                         Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }
  
  /**
   * Initialize and do work where other steps need to wait for...
   *
   * @param stepMetaInterface
   *          The metadata to work with
   * @param stepDataInterface
   *          The data to initialize
   */
  public boolean init( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) {
    meta = (PhotonPDIPluginMeta) stepMetaInterface;
    data = (PhotonPDIPluginData) stepDataInterface;
    return super.init( stepMetaInterface, stepDataInterface );
  }


  private Map<String, String> gecodeMapBox(String address, String city, String state, String zip){
    Map<String, String> hmap = new HashMap<String, String>();
    if(meta.getMapboxUrl() != null && meta.getMapboxUrl().trim().length() > 0) {
      try {
        URI uri = new URI(meta.getMapboxUrl());
        hmap = data.mapBoxRequest(uri, meta.getMapBoxKey(), address, city, state, zip);
      }catch(URISyntaxException e){
        if(isBasic()){
          logBasic("Failed to Parse Mapbox URL");
          e.printStackTrace();
        }
      }catch(Exception e){
        if(isBasic()){
          logBasic("Failed to Obtain mapbox geocode data");
        }
      }
    }
    return hmap;
  }


  /**
   * Geocode from photon
   * @param address         The address
   * @param city            The city
   * @param state           The state
   * @param zip             The postal code
   * @return                A hashmap cotnaining return values
   */
  private Map<String, String> geocodePhoton(String address, String city, String state, String zip){
    Map<String, String> hmap = new HashMap<String, String>();
    String url = meta.getphotonUrl();
    if(url !=null && url.trim().length() > 0){
      url = url.trim();
      try {
        URI uri = new URI(url);
        hmap = data.photonRequest(uri, address, city, state, zip);
      }catch(Exception e){
        if(isBasic()){
          logBasic("Failed to Geocode Address");
          logBasic(e.getMessage());
          e.printStackTrace();
        }
      }
    }else{
      if(isBasic()){
        logBasic("No photon URL Provided in Geocoder");
      }
    }
    return hmap;
  }

  /**
   * Extract the field.
   *
   * @param fieldName     The field name to extract
   * @param rmi           The row meta interface
   * @return              The extracted value primitive
   */
  private Object extractField(Object[] r, String fieldName, RowMetaInterface rmi){
    if(fieldName != null && fieldName.trim().length() > 0) {
      int idx = rmi.indexOfValue(fieldName);
      if (idx > -1) {
        return r[idx];
      }
    }else if(fieldName == null){
      if(isBasic()){
        logBasic("Field " + fieldName + " Not Provided in Geocoding");
      }
    }
    return null;
  }


  /**
   * Check if the address was obtained
   * @param r           The row to check
   * @param rmi         The row meta interface
   * @return            Whether the address was parsed
   */
  private boolean wasGeocodeObtained(Object[] r, RowMetaInterface rmi){
    boolean found = false;
    String latField = meta.getLatitudeField();
    String lonField = meta.getLongitudeField();
    int idx = rmi.indexOfValue(latField);

    if(idx > -1) {
      if(r[idx] != null){
        idx = rmi.indexOfValue(lonField);
        if(r[idx] == null) {
          found = true;
        }
      }
    }

    return found;
  }

  /**
   * Resize the row
   *
   * @param r     The row
   * @return      The object row representation
   */
  private Object[] resizeRow(Object[] r){
    Object[] orow = r.clone();
    if(orow.length < data.outputRowMeta.size()){
      orow = RowDataUtil.resizeArray(orow, data.outputRowMeta.size());
    }
    return orow;
  }

  /**
   * Package the row after obtaining lat and long values from Mapbox
   *
   * @param latLong       The latitude and longitude values
   * @param r             The row to package
   * @param rmi           The row meta interface
   * @return              The updated row
   */
  private Object[] packageMapBoxRow(Map<String, String> latLong, Object[] r, RowMetaInterface rmi){
    return r;
  }

  /**
   * Package the row after obtaining lat and long values from Photon
   *
   * @param latLong       The latitude and longitude values
   * @param r             The row to package
   * @param rmi           The row meta interface
   * @return              The updated row
   */
  private Object[] packagePhotonRow(Map<String, String> latLong, Object[] r, RowMetaInterface rmi){
    Object[] outRow = r.clone();
    int idx = 0;
    if(latLong.containsKey("lat")){

    }

    if(latLong.containsKey("long")){

    }

    if(latLong.containsKey("city")){

    }

    if(latLong.containsKey("street")){

    }

    if(latLong.containsKey("housenumber")){

    }

    if(latLong.containsKey("postcode")){

    }

    if(latLong.containsKey("state")){

    }

    if(latLong.containsKey("name")){

    }

    if(latLong.containsKey("osm_value")){

    }

    if(latLong.containsKey("country")) {

    }
    return outRow;
  }

  /**
   * Wait for a specified time to slow down request rate.
   *
   * @param waitTime        The wait time long
   */
  private void doWait(long waitTime){
    try {
      Thread.sleep(waitTime);
    }catch(InterruptedException e){
      if(isBasic()){
        logBasic(String.format("Failed to Wait for %d seconds in Geocoder", waitTime));
        e.printStackTrace();
      }
    }
  }

  /**
   * Geocode the address in a row
   *
   * @param inrow       The input row
   * @param rmi         The row meta interface
   * @return            The updated row
   */
  private Object[] getLatLong(Object[] inrow, RowMetaInterface rmi){
    Object[] outrow = this.resizeRow(inrow);
    boolean fallThrough = meta.isUseMapBoxFallbackIfPresent();
    String cityO = "";
    if(meta.getCityField() != null && meta.getCityField().trim().length() > 0) {
      cityO = (String) this.extractField(outrow, meta.getCityField(), rmi);
    }

    String streetO = "";
    if(meta.getStreetField() != null && meta.getStreetField().trim().length() >0) {
      streetO = (String) this.extractField(outrow, meta.getStreetField(), rmi);
    }

    String stateO = "";
    if(meta.getStateField() != null && meta.getStateField().trim().length() > 0) {
      stateO = (String) this.extractField(outrow, meta.getStateField(), rmi);
    }

    Object zipO = "";
    if(meta.getZipField() != null && meta.getZipField().trim().length() > 0) {
      zipO = this.extractField(outrow, meta.getZipField(), rmi);
    }

    String zipUse = "";
    if(cityO != null && streetO != null && stateO != null && zipO != null) {
      if (zipO != null && !(zipO instanceof String)) {
        zipUse = String.valueOf(zipO);
      } else if (zipO == null) {
        zipUse = "";
      }
      if(this.meta.getphotonUrl() != null && this.meta.getphotonUrl().trim().length() >0) {
        try {
          URI uri = new URI(meta.getphotonUrl());
          Map<String, String> result = this.data.photonRequest(uri, streetO, cityO, stateO, zipUse);
          outrow = this.packagePhotonRow(result, outrow, rmi);
        }catch(Exception e){
          if(this.isBasic()){
            this.logBasic("Failed to Process Address Through Photon");
            this.logBasic(e.getMessage());
          }
        }
      }
    }

    if(!wasGeocodeObtained(outrow, rmi) && this.meta.isUseMapBoxFallbackIfPresent()){
      if(this.meta.getMapboxUrl() != null && this.meta.getMapboxUrl().trim().length() >0) {
        try {
          URI uri = new URI(meta.getMapboxUrl());
          String mtoken = this.meta.getMapBoxKey();
          Map<String, String> result = this.data.mapBoxRequest(uri, mtoken,streetO, cityO, stateO, zipUse);
          outrow = this.packagePhotonRow(result, outrow, rmi);
        } catch (Exception e) {
          if (this.isBasic()) {
            this.logBasic("Failed to Process Address Through MapBox");
            this.logBasic(e.getMessage());
          }
        }
      }
    }
    return outrow;
  }

  /**
   * Setup the processor.
   *
   * @throws KettleException
   */
  private void setupProcessor() throws KettleException{
    RowMetaInterface inMeta = getInputRowMeta().clone();
    data.outputRowMeta = inMeta;
    meta.getFields(data.outputRowMeta, getStepname(), null, null, this, null, null);
    first = false;
  }


  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    Object[] r = getRow(); // get row, set busy!
    if ( r == null ) {
      // no more input to be expected...
      setOutputDone();
      return false;
    }

    if(first){
      data = (PhotonPDIPluginData) sdi;
      meta = (PhotonPDIPluginMeta) smi;
      this.setupProcessor();
    }

    r = this.getLatLong(r, getInputRowMeta());
    putRow(data.outputRowMeta, r);

    if ( checkFeedback( getLinesRead() ) ) {
      if ( log.isBasic() )
        logBasic( BaseMessages.getString( PKG, "photonPDIPlugin.Log.LineNumber" ) + getLinesRead() );
    }
      
    return true;
  }
}