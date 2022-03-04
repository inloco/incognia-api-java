package com.incognia.feedback;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum FeedbackEvent {
  @JsonProperty("signup_accepted")
  SIGNUP_ACCEPTED,
  @JsonProperty("signup_declined")
  SIGNUP_DECLINED,
  @JsonProperty("payment_accepted")
  PAYMENT_ACCEPTED,
  @JsonProperty("payment_accepted_by_third_party")
  PAYMENT_ACCEPTED_BY_THIRD_PARTY,
  @JsonProperty("payment_accepted_by_control_group")
  PAYMENT_ACCEPTED_BY_CONTROL_GROUP,
  @JsonProperty("payment_declined")
  PAYMENT_DECLINED,
  @JsonProperty("payment_declined_by_risk_analysis")
  PAYMENT_DECLINED_BY_RISK_ANALYSIS,
  @JsonProperty("payment_declined_by_manual_review")
  PAYMENT_DECLINED_BY_MANUAL_REVIEW,
  @JsonProperty("payment_declined_by_business")
  PAYMENT_DECLINED_BY_BUSINESS,
  @JsonProperty("payment_declined_by_acquirer")
  PAYMENT_DECLINED_BY_ACQUIRER,
  @JsonProperty("login_accepted")
  LOGIN_ACCEPTED,
  @JsonProperty("login_declined")
  LOGIN_DECLINED,
  @JsonProperty("verified")
  VERIFIED,
  @JsonProperty("identity_fraud")
  IDENTITY_FRAUD,
  @JsonProperty("account_takeover")
  ACCOUNT_TAKEOVER,
  @JsonProperty("chargeback_notification")
  CHARGEBACK_NOTIFICATION,
  @JsonProperty("chargeback")
  CHARGEBACK,
  @JsonProperty("mpos_fraud")
  MPOS_FRAUD,
  @JsonProperty("challenge_passed")
  CHALLENGE_PASSED,
  @JsonProperty("challenge_failed")
  CHALLENGE_FAILED,
  @JsonProperty("password_changed_successfully")
  PASSWORD_CHANGED_SUCCESSFULLY,
  @JsonProperty("password_change_failed")
  PASSWORD_CHANGE_FAILED,
  @JsonProperty("promotion_abuse")
  PROMOTION_ABUSE;
}
