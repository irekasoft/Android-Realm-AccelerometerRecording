package com.irekasoft.accelerometer.model;

import io.realm.RealmObject;

/**
 * Created by hijazi on 10/4/16.
 */
public class AccelerometerData extends RealmObject {

  private Double          x;
  private Double          y;
  private Double          z;
  private Long     millisecond;

  public Long getMillisecond() {
    return millisecond;
  }

  public void setMillisecond(Long millisecond) {
    this.millisecond = millisecond;
  }

  public Double getX() {
    return x;
  }

  public void setX(Double x) {
    this.x = x;
  }

  public Double getY() {
    return y;
  }

  public void setY(Double y) {
    this.y = y;
  }

  public Double getZ() {
    return z;
  }

  public void setZ(Double z) {
    this.z = z;
  }
}
