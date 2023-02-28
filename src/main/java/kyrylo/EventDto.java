/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under one or more contributor license agreements.
 * Licensed under a proprietary license. See the License.txt file for more information.
 * You may not use this file except in compliance with the proprietary license.
 */
package kyrylo;

public class EventDto {
  private String id;
  private String eventName;
  private Long timestamp;
  private Long ingestionTimestamp;
  private String traceId;
  private String group;
  private String source;
  private Object data;

  public String getId() {
    return id;
  }

  public String getEventName() {
    return eventName;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public Long getIngestionTimestamp() {
    return ingestionTimestamp;
  }

  public String getTraceId() {
    return traceId;
  }

  public String getGroup() {
    return group;
  }

  public String getSource() {
    return source;
  }

  public Object getData() {
    return data;
  }

  public EventDto() {
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public void setIngestionTimestamp(Long ingestionTimestamp) {
    this.ingestionTimestamp = ingestionTimestamp;
  }

  public void setTraceId(String traceId) {
    this.traceId = traceId;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public void setData(Object data) {
    this.data = data;
  }
}
