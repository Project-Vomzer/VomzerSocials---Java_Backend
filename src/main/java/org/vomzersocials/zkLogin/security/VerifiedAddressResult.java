package org.vomzersocials.zkLogin.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VerifiedAddressResult {
    private boolean success;
    private String address;
    private String errorMessage;

    public static VerifiedAddressResult success(String address) {
        return new VerifiedAddressResult(true, address, "Address verification successful");
    }

    public static VerifiedAddressResult failed(String errorMessage) {
        return new VerifiedAddressResult(false, null, "Address verification status: " + errorMessage);
    }
}
