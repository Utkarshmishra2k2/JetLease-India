package com.jetlease.service;

import com.jetlease.dto.response.VerifyResult;
import com.jetlease.entity.AadhaarRegistry;
import com.jetlease.entity.BankLedger;
import com.jetlease.entity.PilotLicenseRegistry;
import com.jetlease.repository.AadhaarRegistryRepository;
import com.jetlease.repository.BankLedgerRepository;
import com.jetlease.repository.PilotLicenseRegistryRepository;
import com.jetlease.util.IdGen;
import org.springframework.stereotype.Service;

/** Ported from MockApi.java - simulated third-party DGCA / Aadhaar / bank ledger integrations. */
@Service
public class MockApiService {

    public static final String MOCK_OTP_CODE = "123456";

    private final AadhaarRegistryRepository aadhaarRegistryRepository;
    private final PilotLicenseRegistryRepository pilotLicenseRegistryRepository;
    private final BankLedgerRepository bankLedgerRepository;

    public MockApiService(AadhaarRegistryRepository aadhaarRegistryRepository,
                           PilotLicenseRegistryRepository pilotLicenseRegistryRepository,
                           BankLedgerRepository bankLedgerRepository) {
        this.aadhaarRegistryRepository = aadhaarRegistryRepository;
        this.pilotLicenseRegistryRepository = pilotLicenseRegistryRepository;
        this.bankLedgerRepository = bankLedgerRepository;
    }

    public boolean verifyOtp(String code) {
        return MOCK_OTP_CODE.equals(code == null ? "" : code.trim());
    }

    public VerifyResult verifyAadhaar(String aadhaarNumber) {
        VerifyResult result = new VerifyResult();
        AadhaarRegistry rec = aadhaarRegistryRepository.findByAadhaarNumber(aadhaarNumber.trim()).orElse(null);
        if (rec == null) {
            result.verified = false;
            result.message = "Aadhaar number not found in the registry.";
            return result;
        }
        if (!"Active".equals(rec.getStatus())) {
            result.verified = false;
            result.message = "Aadhaar found but its status is \"" + rec.getStatus() + "\", not Active.";
            return result;
        }
        result.verified = true;
        result.holderName = rec.getHolderName();
        result.dob = rec.getDob();
        result.gender = rec.getGender();
        result.message = "Aadhaar verified - registered to " + result.holderName + ".";
        return result;
    }

    public VerifyResult verifyPilotLicense(String licenseNumber) {
        VerifyResult result = new VerifyResult();
        PilotLicenseRegistry rec = pilotLicenseRegistryRepository.findByLicenseNumberIgnoreCase(licenseNumber.trim()).orElse(null);
        if (rec == null) {
            result.verified = false;
            result.message = "License number not found in the DGCA registry.";
            return result;
        }
        if (!"Active".equals(rec.getStatus())) {
            result.verified = false;
            result.message = "License found but its status is \"" + rec.getStatus() + "\", not Active.";
            return result;
        }
        result.verified = true;
        result.holderName = rec.getHolderName();
        result.licenseClass = rec.getLicenseClass();
        result.hoursOnRecord = rec.getHoursOnRecord();
        result.message = "License verified - registered to " + result.holderName + ", " + result.hoursOnRecord + " hours on record.";
        return result;
    }

    public void recordLedgerEntry(String transactionId, String bookingId, long amount) {
        BankLedger entry = new BankLedger();
        entry.setTransactionId(transactionId);
        entry.setBookingId(bookingId);
        entry.setAmount(amount);
        entry.setStatus("CLEARED");
        entry.setClearedAt(IdGen.nowIso());
        bankLedgerRepository.save(entry);
    }

    public VerifyResult verifyPaymentAgainstLedger(String transactionId, String bookingId, long amount) {
        VerifyResult result = new VerifyResult();
        BankLedger entry = bankLedgerRepository.findByTransactionIdAndBookingId(transactionId, bookingId).orElse(null);
        if (entry == null) {
            result.verified = false;
            result.message = "Transaction ID not found in the bank ledger.";
            return result;
        }
        if (entry.getAmount() != amount) {
            result.verified = false;
            result.message = "Transaction found, but the settled amount does not match the invoice.";
            return result;
        }
        result.verified = true;
        result.message = "Bank ledger confirms this transaction cleared for INR " + entry.getAmount()
                + " on " + entry.getClearedAt() + ".";
        return result;
    }
}
