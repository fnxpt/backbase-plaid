package com.backbase.proto.plaid.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * PublicTokenRequest
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2020-11-02T15:34:35.545367+01:00[Europe/Amsterdam]")

public class PublicTokenRequest   {
  @JsonProperty("public-token")
  private String publicToken;

  public PublicTokenRequest publicToken(String publicToken) {
    this.publicToken = publicToken;
    return this;
  }

  /**
   * Get publicToken
   * @return publicToken
  */
  @ApiModelProperty(required = true, value = "")
  @NotNull


  public String getPublicToken() {
    return publicToken;
  }

  public void setPublicToken(String publicToken) {
    this.publicToken = publicToken;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PublicTokenRequest publicTokenRequest = (PublicTokenRequest) o;
    return Objects.equals(this.publicToken, publicTokenRequest.publicToken);
  }

  @Override
  public int hashCode() {
    return Objects.hash(publicToken);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PublicTokenRequest {\n");
    
    sb.append("    publicToken: ").append(toIndentedString(publicToken)).append("\n");
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

