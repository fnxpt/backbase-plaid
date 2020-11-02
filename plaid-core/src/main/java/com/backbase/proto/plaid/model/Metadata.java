package com.backbase.proto.plaid.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Metadata
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2020-11-02T15:34:35.545367+01:00[Europe/Amsterdam]")

public class Metadata   {
  @JsonProperty("account")
  private PlaidAccount account;

  @JsonProperty("account_id")
  private String accountId;

  @JsonProperty("accounts")
  @Valid
  private List<PlaidAccount> accounts = null;

  @JsonProperty("institution")
  private PlaidInstitution institution;

  public Metadata account(PlaidAccount account) {
    this.account = account;
    return this;
  }

  /**
   * Get account
   * @return account
  */
  @ApiModelProperty(value = "")

  @Valid

  public PlaidAccount getAccount() {
    return account;
  }

  public void setAccount(PlaidAccount account) {
    this.account = account;
  }

  public Metadata accountId(String accountId) {
    this.accountId = accountId;
    return this;
  }

  /**
   * Get accountId
   * @return accountId
  */
  @ApiModelProperty(value = "")


  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public Metadata accounts(List<PlaidAccount> accounts) {
    this.accounts = accounts;
    return this;
  }

  public Metadata addAccountsItem(PlaidAccount accountsItem) {
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

  @Valid

  public List<PlaidAccount> getAccounts() {
    return accounts;
  }

  public void setAccounts(List<PlaidAccount> accounts) {
    this.accounts = accounts;
  }

  public Metadata institution(PlaidInstitution institution) {
    this.institution = institution;
    return this;
  }

  /**
   * Get institution
   * @return institution
  */
  @ApiModelProperty(value = "")

  @Valid

  public PlaidInstitution getInstitution() {
    return institution;
  }

  public void setInstitution(PlaidInstitution institution) {
    this.institution = institution;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Metadata metadata = (Metadata) o;
    return Objects.equals(this.account, metadata.account) &&
        Objects.equals(this.accountId, metadata.accountId) &&
        Objects.equals(this.accounts, metadata.accounts) &&
        Objects.equals(this.institution, metadata.institution);
  }

  @Override
  public int hashCode() {
    return Objects.hash(account, accountId, accounts, institution);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Metadata {\n");
    
    sb.append("    account: ").append(toIndentedString(account)).append("\n");
    sb.append("    accountId: ").append(toIndentedString(accountId)).append("\n");
    sb.append("    accounts: ").append(toIndentedString(accounts)).append("\n");
    sb.append("    institution: ").append(toIndentedString(institution)).append("\n");
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

