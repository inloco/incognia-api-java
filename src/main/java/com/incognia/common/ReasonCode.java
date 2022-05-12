package com.incognia.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReasonCode {
  ADDRESS_CITY_COUNT_30D("evidence_address_city_count_30d"),
  ADDRESS_STREET_COUNT_30D("evidence_address_street_count_30d"),
  ADDRESS_ZIP_COUNT_30D("evidence_address_zip_count_30d"),
  HIGH_CONCENTRATION_LOCATION("evidence_high_concentration_location"),
  IDS_ACCOUNTS_COUNT_3D("evidence_ids_accounts_count_3d"),
  IDS_ACCOUNTS_COUNT_10D("evidence_ids_accounts_count_10d"),
  IDS_ACCOUNTS_COUNT_30D("evidence_ids_accounts_count_30d"),
  IDS_DEVICES_COUNT_30D("evidence_ids_devices_count_30d"),
  IDS_INSTALLATIONS_COUNT_3D("evidence_ids_installations_count_3d"),
  IDS_INSTALLATIONS_COUNT_10D("evidence_ids_installations_count_10d"),
  IDS_INSTALLATIONS_COUNT_30D("evidence_ids_installations_count_30d"),
  TAMPERED_REQUEST("evidence_tampered_request"),
  TRANSACTIONS_AMOUNT_3D("evidence_transactions_amount_3d"),
  TRANSACTIONS_AMOUNT_30D("evidence_transactions_amount_30d"),
  TRANSACTIONS_CBK_AMOUNT_3D("evidence_transactions_cbk_amount_3d"),
  TRANSACTIONS_CBK_AMOUNT_30D("evidence_transactions_cbk_amount_30d"),
  TRANSACTIONS_CBK_COUNT_3D("evidence_transactions_cbk_count_3d"),
  TRANSACTIONS_CBK_COUNT_30D("evidence_transactions_cbk_count_30d"),
  TRANSACTIONS_COUNT_3D("evidence_transactions_count_3d"),
  TRANSACTIONS_COUNT_30D("evidence_transactions_count_30d"),
  WIFI_BAD_REPUTATION("evidence_wifi_bad_reputation"),
  LOCATION_BEHAVIOR("location_behavior"),
  ACCOUNT_TAKEOVER("report_account_takeover"),
  CHALLENGE_FAILED("report_challenge_failed"),
  CHALLENGE_PASSED("report_challenge_passed"),
  CHARGEBACK_NOTIFICATION("report_chargeback_notification"),
  CHARGEBACK("report_chargeback"),
  IDENTITY_FRAUD("report_identity_fraud"),
  LOGIN_ACCEPTED("report_login_accepted"),
  LOGIN_DECLINED("report_login_declined"),
  MPOS_FRAUD("report_mpos_fraud"),
  MPOS_FRAUD_INDIRECT("report_mpos_fraud_indirect"),
  PASSWORD_CHANGE_FAILED("report_password_change_failed"),
  PASSWORD_CHANGED_SUCCESSFULLY("report_password_changed_successfully"),
  PAYMENT_ACCEPTED_BY_CONTROL_GROUP("report_payment_accepted_by_control_group"),
  PAYMENT_ACCEPTED_BY_THIRD_PARTY("report_payment_accepted_by_third_party"),
  PAYMENT_ACCEPTED("report_payment_accepted"),
  PAYMENT_DECLINED_BY_ACQUIRER("report_payment_declined_by_acquirer"),
  PAYMENT_DECLINED_BY_BUSINESS("report_payment_declined_by_business"),
  PAYMENT_DECLINED_BY_MANUAL_REVIEW("report_payment_declined_by_manual_review"),
  PAYMENT_DECLINED_BY_RISK_ANALYSIS("report_payment_declined_by_risk_analysis"),
  PAYMENT_DECLINED("report_payment_declined"),
  PROMOTION_ABUSE("report_promotion_abuse"),
  SIGNUP_ACCEPTED("report_signup_accepted"),
  SIGNUP_DECLINED("report_signup_declined"),
  VERIFIED("report_verified"),
  REPORT("report"),
  SUSPICIOUS_ACTIVITY("suspicious_activity"),
  UNKNOWN("unknown");

  private final String code;

  public boolean isSameAs(String code) {
    return this.code.equals(code);
  }
}
