package com.incognia.common;

import lombok.experimental.UtilityClass;

/**
 * This Class objective is to help developers use the values present in the code field on the
 * Reasons class.
 */
@UtilityClass
public class ReasonCodeConstants {
  private static final String ENVIRONMENT_LINKED_TO_MPOS_FRAUD = "environment_linked_to_mpos_fraud";
  private static final String HIGH_DENSITY_LOCATION = "high_density_location";
  private static final String MULTIPLE_ACCOUNTS = "multiple_accounts";
  private static final String MULTIPLE_INSTALLATIONS = "multiple_installations";
  private static final String MULTIPLE_ADDRESS_ZIP_CODES = "multiple_address_zip_codes";
  private static final String MULTIPLE_ADDRESS_STREETS = "multiple_address_streets";
  private static final String MULTIPLE_ADDRESS_CITIES = "multiple_address_cities";
  private static final String SDK_TAMPERING = "sdk_tampering";
  private static final String RECENT_HIGH_RISK_ACCOUNT = "recent_high_risk_account";
  private static final String MACHINE_LEARNING_MODEL = "machine_learning_model";
  private static final String LOCATION_FINGERPRINT_MATCH = "location_fingerprint_match";
  private static final String DEVICE_LINKED_TO_MPOS_FRAUD = "device_linked_to_mpos_fraud";
  private static final String VERIFIED = "verified";
  private static final String SIGNUP_ACCEPTED = "signup_accepted";
  private static final String SIGNUP_DECLINED = "signup_declined";
  private static final String IDENTITY_FRAUD = "identity_fraud";
  private static final String MPOS_FRAUD = "mpos_fraud";
  private static final String PAYMENT_ACCEPTED = "payment_accepted";
  private static final String PAYMENT_ACCEPTED_BY_THIRD_PARTY = "payment_accepted_by_third_party";
  private static final String PAYMENT_ACCEPTED_BY_CONTROL_GROUP =
      "payment_accepted_by_control_group";
  private static final String PAYMENT_DECLINED = "payment_declined";
  private static final String PAYMENT_DECLINED_BY_RISK_ANALYSIS =
      "payment_declined_by_risk_analysis";
  private static final String PAYMENT_DECLINED_BY_MANUAL_REVIEW =
      "payment_declined_by_manual_review";
  private static final String PAYMENT_DECLINED_BY_BUSINESS = "payment_declined_by_business";
  private static final String PAYMENT_DECLINED_BY_ACQUIRER = "payment_declined_by_acquirer";
  private static final String LOGIN_ACCEPTED = "login_accepted";
  private static final String LOGIN_DECLINED = "login_declined";
  private static final String ACCOUNT_TAKEOVER = "account_takeover";
  private static final String CHARGEBACK_NOTIFICATION = "chargeback_notification";
  private static final String CHARGEBACK = "chargeback";
  private static final String CHALLENGE_PASSED = "challenge_passed";
  private static final String CHALLENGE_FAILED = "challenge_failed";
  private static final String PASSWORD_CHANGED_SUCCESSFULLY = "password_changed_successfully";
  private static final String PASSWORD_CHANGE_FAILED = "password_change_failed";
  private static final String PROMOTION_ABUSE = "promotion_abuse";
  private static final String ADDRESS_VERIFICATION = "address_verification";
  private static final String DEVICE_INTEGRITY = "device_integrity";
  private static final String REPORT = "report";
  private static final String TRUSTED_LOCATION = "trusted_location";
  private static final String MULTI_DEVICE_ACCOUNT = "multi_device_account";
}
