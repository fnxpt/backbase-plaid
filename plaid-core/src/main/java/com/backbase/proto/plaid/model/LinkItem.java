package com.backbase.proto.plaid.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * LinkItem
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2020-11-02T15:34:35.545367+01:00[Europe/Amsterdam]")

public class LinkItem {
  @JsonProperty("itemId")
  private String itemId;

  @JsonProperty("institutionName")
  private String institutionName;

  @JsonProperty("accounts")
  @Valid
  private List<String> accounts = null;

  @JsonProperty("experationDate")
  @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
  private LocalDate experationDate;

  public LinkItem itemId(String itemId) {
    this.itemId = itemId;
    return this;
  }

  /**
   * Get itemId
   * @return itemId
  */
  @ApiModelProperty(value = "")


  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public LinkItem institutionName(String institutionName) {
    this.institutionName = institutionName;
    return this;
  }

  /**
   * Get institutionName
   * @return institutionName
  */
  @ApiModelProperty(value = "")


  public String getInstitutionName() {
    return institutionName;
  }

  public void setInstitutionName(String institutionName) {
    this.institutionName = institutionName;
  }

  public LinkItem accounts(List<String> accounts) {
    this.accounts = accounts;
    return this;
  }

  public LinkItem addAccountsItem(String accountsItem) {
    if (this.accounts == null) {
      this.accounts = new ArrayList<>();
    }
    this.accounts.add(accountsItem);
    return this;
  }

  /**
   * Get accounts
   * @return accounts
  */
  @ApiModelProperty(value = "")


  public List<String> getAccounts() {
    return accounts;
  }

  public void setAccounts(List<String> accounts) {
    this.accounts = accounts;
  }

  public LinkItem experationDate(LocalDate experationDate) {
    this.experationDate = experationDate;
    return this;
  }

  /**
   * Get experationDate
   * @return experationDate
  */
  @ApiModelProperty(value = "")

  @Valid

  public LocalDate getExperationDate() {
    return experationDate;
  }

  public void setExperationDate(LocalDate experationDate) {
    this.experationDate = experationDate;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LinkItem linkItem = (LinkItem) o;
    return Objects.equals(this.itemId, linkItem.itemId) &&
        Objects.equals(this.institutionName, linkItem.institutionName) &&
        Objects.equals(this.accounts, linkItem.accounts) &&
        Objects.equals(this.experationDate, linkItem.experationDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(itemId, institutionName, accounts, experationDate);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinkItem {\n");
    
    sb.append("    itemId: ").append(toIndentedString(itemId)).append("\n");
    sb.append("    institutionName: ").append(toIndentedString(institutionName)).append("\n");
    sb.append("    accounts: ").append(toIndentedString(accounts)).append("\n");
    sb.append("    experationDate: ").append(toIndentedString(experationDate)).append("\n");
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

