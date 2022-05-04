package com.incognia.common;

/**
 * This Class objective is to help developers use the values present in the code field on the
 * Reasons class.
 */
public class ReasonCodeConstants {
  public ReasonCodeConstants() {
    throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
  }

  public static final String ENVIRONMENT_LINKED_TO_MPOS_FRAUD = "environment_linked_to_mpos_fraud";
  public static final String HIGH_DENSITY_LOCATION = "high_density_location";
  public static final String MULTIPLE_ACCOUNTS = "multiple_accounts";
  public static final String MULTIPLE_INSTALLATIONS = "multiple_installations";
  public static final String MULTIPLE_ADDRESS_ZIP_CODES = "multiple_address_zip_codes";
  public static final String MULTIPLE_ADDRESS_STREETS = "multiple_address_streets";
  public static final String MULTIPLE_ADDRESS_CITIES = "multiple_address_cities";
  public static final String SDK_TAMPERING = "sdk_tampering";
  public static final String RECENT_HIGH_RISK_ACCOUNT = "recent_high_risk_account";
  public static final String MACHINE_LEARNING_MODEL = "machine_learning_model";
  public static final String LOCATION_FINGERPRINT_MATCH = "location_fingerprint_match";
  public static final String DEVICE_LINKED_TO_MPOS_FRAUD = "device_linked_to_mpos_fraud";
  public static final String VERIFIED = "verified";
  public static final String SIGNUP_ACCEPTED = "signup_accepted";
  public static final String SIGNUP_DECLINED = "signup_declined";
  public static final String IDENTITY_FRAUD = "identity_fraud";
  public static final String MPOS_FRAUD = "mpos_fraud";
  public static final String PAYMENT_ACCEPTED = "payment_accepted";
  public static final String PAYMENT_ACCEPTED_BY_THIRD_PARTY = "payment_accepted_by_third_party";
  public static final String PAYMENT_ACCEPTED_BY_CONTROL_GROUP =
      "payment_accepted_by_control_group";
  public static final String PAYMENT_DECLINED = "payment_declined";
  public static final String PAYMENT_DECLINED_BY_RISK_ANALYSIS =
      "payment_declined_by_risk_analysis";
  public static final String PAYMENT_DECLINED_BY_MANUAL_REVIEW =
      "payment_declined_by_manual_review";
  public static final String PAYMENT_DECLINED_BY_BUSINESS = "payment_declined_by_business";
  public static final String PAYMENT_DECLINED_BY_ACQUIRER = "payment_declined_by_acquirer";
  public static final String LOGIN_ACCEPTED = "login_accepted";
  public static final String LOGIN_DECLINED = "login_declined";
  public static final String ACCOUNT_TAKEOVER = "account_takeover";
  public static final String CHARGEBACK_NOTIFICATION = "chargeback_notification";
  public static final String CHARGEBACK = "chargeback";
  public static final String CHALLENGE_PASSED = "challenge_passed";
  public static final String CHALLENGE_FAILED = "challenge_failed";
  public static final String PASSWORD_CHANGED_SUCCESSFULLY = "password_changed_successfully";
  public static final String PASSWORD_CHANGE_FAILED = "password_change_failed";
  public static final String PROMOTION_ABUSE = "promotion_abuse";
  public static final String ADDRESS_VERIFICATION = "address_verification";
  public static final String DEVICE_INTEGRITY = "device_integrity";
  public static final String REPORT = "report";
  public static final String TRUSTED_LOCATION = "trusted_location";
  public static final String MULTI_DEVICE_ACCOUNT = "multi_device_account";
}
