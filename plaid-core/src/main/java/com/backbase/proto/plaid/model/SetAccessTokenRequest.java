package com.backbase.proto.plaid.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.Objects;

/**
 * SetAccessTokenRequest
 */
public class SetAccessTokenRequest   {
  @JsonProperty("public_token")
  private String publicToken;

  @JsonProperty("metadata")
  private Metadata metadata;

  public SetAccessTokenRequest publicToken(String publicToken) {
    this.publicToken = publicToken;
    return this;
  }

  /**
   * Get publicToken
   * @return publicToken
  */
  @ApiModelProperty(value = "")


  public String getPublicToken() {
    return publicToken;
  }

  public void setPublicToken(String publicToken) {
    this.publicToken = publicToken;
  }

  public SetAccessTokenRequest metadata(Metadata metadata) {
    this.metadata = metadata;
    return this;
  }

  /**
   * Get metadata
   * @return metadata
  */
  @ApiModelProperty(value = "")

  @Valid

  public Metadata getMetadata() {
    return metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SetAccessTokenRequest setAccessTokenRequest = (SetAccessTokenRequest) o;
    return Objects.equals(this.publicToken, setAccessTokenRequest.publicToken) &&
        Objects.equals(this.metadata, setAccessTokenRequest.metadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(publicToken, metadata);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SetAccessTokenRequest {\n");
    
    sb.append("    publicToken: ").append(toIndentedString(publicToken)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
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

