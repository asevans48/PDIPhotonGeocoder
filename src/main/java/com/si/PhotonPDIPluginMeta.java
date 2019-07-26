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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.List;


/**
 * Skeleton for PDI Step plugin.
 */
@Step( id = "PhotonPDIPlugin", image = "photonPDIPlugin.svg", name = "photon Geocode",
    description = "photon geocoder.", categoryDescription = "Transform" )
public class PhotonPDIPluginMeta extends BaseStepMeta implements StepMetaInterface {
  private String photonUrl = "";
  private String mapboxUrl = "";
  private String mapBoxKey = "";
  private String streetField = "";
  private String stateField = "";
  private String cityField = "";
  private String zipField = "";
  private String latitudeField = "";
  private String longitudeField = "";
  private long postMapboxWaitMillis = 1000L;
  private long postphotonWaitMillis = 1000L;
  private boolean useMapBoxFallbackIfPresent = true;
  
  private static Class<?> PKG = PhotonPDIPlugin.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  public PhotonPDIPluginMeta() {
    super(); // allocate BaseStepMeta
  }

  public String getStreetField() {
    return streetField;
  }

  public void setStreetField(String streetField) {
    this.streetField = streetField;
  }

  public String getStateField() {
    return stateField;
  }

  public void setStateField(String stateField) {
    this.stateField = stateField;
  }

  public String getCityField() {
    return cityField;
  }

  public void setCityField(String cityField) {
    this.cityField = cityField;
  }

  public String getZipField() {
    return zipField;
  }

  public void setZipField(String zipField) {
    this.zipField = zipField;
  }

  public String getphotonUrl() {
    return photonUrl;
  }

  public void setphotonUrl(String photonUrl) {
    this.photonUrl = photonUrl;
  }

  public String getMapboxUrl() {
    return mapboxUrl;
  }

  public void setMapboxUrl(String mapboxUrl) {
    this.mapboxUrl = mapboxUrl;
  }

  public String getMapBoxKey() {
    return mapBoxKey;
  }

  public void setMapBoxKey(String mapBoxKey) {
    this.mapBoxKey = mapBoxKey;
  }

  public String getLatitudeField() {
    return latitudeField;
  }

  public void setLatitudeField(String latitudeField) {
    this.latitudeField = latitudeField;
  }

  public String getLongitudeField() {
    return longitudeField;
  }

  public void setLongitudeField(String longitudeField) {
    this.longitudeField = longitudeField;
  }

  public boolean isUseMapBoxFallbackIfPresent() {
    return useMapBoxFallbackIfPresent;
  }

  public void setUseMapBoxFallbackIfPresent(boolean useMapBoxFallbackIfPresent) {
    this.useMapBoxFallbackIfPresent = useMapBoxFallbackIfPresent;
  }

  public long getPostMapboxWaitMillis() {
    return postMapboxWaitMillis;
  }

  public void setPostMapboxWaitMillis(long postMapboxWaitMillis) {
    this.postMapboxWaitMillis = postMapboxWaitMillis;
  }

  public long getPostphotonWaitMillis() {
    return postphotonWaitMillis;
  }

  public void setPostphotonWaitMillis(long postphotonWaitMillis) {
    this.postphotonWaitMillis = postphotonWaitMillis;
  }

  public void loadXML(Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  public String getXML() throws KettleValueException {
    StringBuilder xml = new StringBuilder();
    xml.append( XMLHandler.addTagValue( "photonUrl", photonUrl ) );
    xml.append(XMLHandler.addTagValue("mapboxUrl", mapboxUrl));
    xml.append(XMLHandler.addTagValue("mapboxKey", mapBoxKey));
    xml.append(XMLHandler.addTagValue("streetField", streetField));
    xml.append(XMLHandler.addTagValue("cityField", cityField));
    xml.append(XMLHandler.addTagValue("stateField", stateField));
    xml.append(XMLHandler.addTagValue("zipField", zipField));
    xml.append(XMLHandler.addTagValue("useMapBox", useMapBoxFallbackIfPresent));
    xml.append(XMLHandler.addTagValue("latitudeField", latitudeField));
    xml.append(XMLHandler.addTagValue("longitudeField", longitudeField));
    xml.append(XMLHandler.addTagValue("postMapboxWaitMillis", postMapboxWaitMillis));
    xml.append(XMLHandler.addTagValue("postphotonWaitMillis", postphotonWaitMillis));
    return xml.toString();
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      setphotonUrl(Const.NVL(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "photonUrl")), ""));
      setMapBoxKey(Const.NVL(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "mapboxUrl")), ""));
      setMapboxUrl(Const.NVL(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "mapboxKey")), ""));
      setStreetField(Const.NVL(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "streetField")), ""));
      setCityField(Const.NVL(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "cityField")), ""));
      setStateField(Const.NVL(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "stateField")), ""));
      setZipField(Const.NVL(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "zipField")), ""));
      setLatitudeField(Const.NVL(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "latitudeField")), ""));
      setLongitudeField(Const.NVL(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "longitudeField")), ""));
      setUseMapBoxFallbackIfPresent(Const.NVL(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "useMapBox")), "N").equals("Y"));
      setPostMapboxWaitMillis(Long.parseLong(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "postMapboxWaitMillis"))));
      setPostphotonWaitMillis(Long.parseLong(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "postphotonWaitMillis"))));
    } catch ( Exception e ) {
      throw new KettleXMLException( "Demo plugin unable to read step info from XML node", e );
    }
  }

  public void setDefault() {
    this.photonUrl = "";
    this.useMapBoxFallbackIfPresent = true;
    this.mapBoxKey = "";
    this.mapboxUrl = "";
    this.streetField = "";
    this.cityField = "";
    this.stateField = "";
    this.zipField = "";
    this.latitudeField = "";
    this.longitudeField = "";
    this.postMapboxWaitMillis = 1000L;
    this.postphotonWaitMillis = 1000L;
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      this.photonUrl  = rep.getStepAttributeString(id_step, "photonUrl" );
      this.useMapBoxFallbackIfPresent = rep.getStepAttributeBoolean(id_step, "useMapBox");
      this.mapBoxKey = rep.getStepAttributeString(id_step, "mapboxKey");
      this.mapboxUrl = rep.getStepAttributeString(id_step, "mapboxUrl");
      this.streetField = rep.getStepAttributeString(id_step, "streetField");
      this.cityField = rep.getStepAttributeString(id_step, "cityField");
      this.stateField = rep.getStepAttributeString(id_step, "stateField");
      this.zipField = rep.getStepAttributeString(id_step, "zipField");
      this.latitudeField = rep.getStepAttributeString(id_step, "latitudeField");
      this.longitudeField = rep.getStepAttributeString(id_step, "longitudeField");
      this.postMapboxWaitMillis = rep.getStepAttributeInteger(id_step, "postMapboxWaitMillis");
      this.postphotonWaitMillis = rep.getStepAttributeInteger(id_step, "postphotonWaitMillis");
    } catch ( Exception e ) {
      throw new KettleException( "Unable to load step from repository", e );
    }
  }
  
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "photonUrl", photonUrl);
      rep.saveStepAttribute( id_transformation, id_step, "useMapBox", useMapBoxFallbackIfPresent);
      rep.saveStepAttribute( id_transformation, id_step, "mapboxKey", mapBoxKey);
      rep.saveStepAttribute( id_transformation, id_step, "mapboxUrl", mapboxUrl);
      rep.saveStepAttribute( id_transformation, id_step, "streetField", streetField);
      rep.saveStepAttribute( id_transformation, id_step, "cityField", cityField);
      rep.saveStepAttribute( id_transformation, id_step, "stateField", stateField);
      rep.saveStepAttribute( id_transformation, id_step, "zipField", zipField);
      rep.saveStepAttribute( id_transformation, id_step, "latitudeField", latitudeField);
      rep.saveStepAttribute( id_transformation, id_step, "longitudeField", longitudeField);
      rep.saveStepAttribute(id_transformation, id_step, "postMapboxWaitMillis", postMapboxWaitMillis);
      rep.saveStepAttribute(id_transformation, id_step, "postphotonWaitMillis", postphotonWaitMillis);
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step into repository: " + id_step, e );
    }
  }

  public void getFields( RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
                         VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    ValueMetaString v0 = new ValueMetaString(latitudeField);
    v0.setOrigin(origin);
    rowMeta.addValueMeta(v0);

    ValueMetaString v1 = new ValueMetaString(longitudeField);
    v1.setOrigin(origin);
    rowMeta.addValueMeta(v1);

    ValueMetaString v2 = new ValueMetaString("photon_city");
    v2.setOrigin(origin);
    rowMeta.addValueMeta(v2);

    ValueMetaString v3 = new ValueMetaString("photon_street");
    v3.setOrigin(origin);
    rowMeta.addValueMeta(v3);

    ValueMetaString v4 = new ValueMetaString("photon_housenumber");
    v4.setOrigin(origin);
    rowMeta.addValueMeta(v4);

    ValueMetaString v5 = new ValueMetaString("photon_postcode");
    v5.setOrigin(origin);
    rowMeta.addValueMeta(v5);

    ValueMetaString v6 = new ValueMetaString("photon_state");
    v6.setOrigin(origin);
    rowMeta.addValueMeta(v6);

    ValueMetaString v7 = new ValueMetaString("photon_name");
    v7.setOrigin(origin);
    rowMeta.addValueMeta(v7);

    ValueMetaString v8 = new ValueMetaString("photon_osm_value");
    v8.setOrigin(origin);
    rowMeta.addValueMeta(v8);

    ValueMetaString v9 = new ValueMetaString("photon_country");
    v9.setOrigin(origin);
    rowMeta.addValueMeta(v9);

    ValueMetaString v10 = new ValueMetaString("photon_country");
    v10.setOrigin(origin);
    rowMeta.addValueMeta(v10);

    ValueMetaString v11 = new ValueMetaString("mapbox_place_name");
    v11.setOrigin(origin);
    rowMeta.addValueMeta(v11);
  }
  
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, 
    StepMeta stepMeta, RowMetaInterface prev, String input[], String output[],
    RowMetaInterface info, VariableSpace space, Repository repository, 
    IMetaStore metaStore ) {
    CheckResult cr;
    if ( prev == null || prev.size() == 0 ) {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString( PKG, "photonPDIPluginMeta.CheckResult.NotReceivingFields" ), stepMeta );
      remarks.add( cr );
    }
    else {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG, "photonPDIPluginMeta.CheckResult.StepRecevingData", prev.size() + "" ), stepMeta );
      remarks.add( cr );
    }
    
    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG, "photonPDIPluginMeta.CheckResult.StepRecevingData2" ), stepMeta );
      remarks.add( cr );
    }
    else {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG, "photonPDIPluginMeta.CheckResult.NoInputReceivedFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    }
  }
  
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans ) {
    return new PhotonPDIPlugin( stepMeta, stepDataInterface, cnr, tr, trans );
  }
  
  public StepDataInterface getStepData() {
    return new PhotonPDIPluginData();
  }

  public String getDialogClassName() {
    return "com.si.PhotonPDIPluginDialog";
  }
}
