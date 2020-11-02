package com.backbase.proto.plaid.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * PlaidAccount
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2020-11-02T15:34:35.545367+01:00[Europe/Amsterdam]")

public class PlaidAccount   {
  @JsonProperty("id")
  private String id;

  @JsonProperty("mask")
  private String mask;

  @JsonProperty("name")
  private String name;

  @JsonProperty("subtype")
  private String subtype;

  @JsonProperty("type")
  private String type;

  public PlaidAccount id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
  */
  @ApiModelProperty(value = "")


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public PlaidAccount mask(String mask) {
    this.mask = mask;
    return this;
  }

  /**
   * Get mask
   * @return mask
  */
  @ApiModelProperty(value = "")


  public String getMask() {
    return mask;
  }

  public void setMask(String mask) {
    this.mask = mask;
  }

  public PlaidAccount name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
  */
  @ApiModelProperty(value = "")


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PlaidAccount subtype(String subtype) {
    this.subtype = subtype;
    return this;
  }

  /**
   * Get subtype
   * @return subtype
  */
  @ApiModelProperty(value = "")


  public String getSubtype() {
    return subtype;
  }

  public void setSubtype(String subtype) {
    this.subtype = subtype;
  }

  public PlaidAccount type(String type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
  */
  @ApiModelProperty(value = "")


  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PlaidAccount plaidAccount = (PlaidAccount) o;
    return Objects.equals(this.id, plaidAccount.id) &&
        Objects.equals(this.mask, plaidAccount.mask) &&
        Objects.equals(this.name, plaidAccount.name) &&
        Objects.equals(this.subtype, plaidAccount.subtype) &&
        Objects.equals(this.type, plaidAccount.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, mask, name, subtype, type);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PlaidAccount {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    mask: ").append(toIndentedString(mask)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    subtype: ").append(toIndentedString(subtype)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

