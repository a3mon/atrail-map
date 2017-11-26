package com.d3vmoon.at.service.pojo.errors;

import static javax.servlet.http.HttpServletResponse.*;

public class Errors {

    public static final EmailAddressAlreadyRegisteredResponse EMAIL_ADDRESS_ALREADY_REGISTERED_RESPONSE = new EmailAddressAlreadyRegisteredResponse();
    public static final CouldNotAuthenticateResponse COULD_NOT_AUTHENTICATE_RESPONSE = new CouldNotAuthenticateResponse();
    public static final NotFoundResponse NOT_FOUND_RESPONSE = new NotFoundResponse();
    public static final NoQuotaRespnse NO_QUOTA_RESPNSE = new NoQuotaRespnse();

    public static class UnauthorizedResponse extends BaseError {
        public UnauthorizedResponse(String message) {
            super(message, SC_UNAUTHORIZED);
        }
    }

    private static class NoQuotaRespnse extends BaseError {
        public NoQuotaRespnse() {
            super("No quota left for this action.", SC_FORBIDDEN);
        }
    }

    private static class NotFoundResponse extends BaseError {
        private NotFoundResponse() {
            super("Could not find the requested resource.", SC_NOT_FOUND);
        }
    }

    private static class CouldNotAuthenticateResponse extends UnauthorizedResponse {
         private CouldNotAuthenticateResponse() {
            super("Could not authenticate.");
        }
    }

    private static class EmailAddressAlreadyRegisteredResponse extends BaseError {
         private EmailAddressAlreadyRegisteredResponse() {
            super("This email is already registered.", SC_CONFLICT);
        }
    }
}
