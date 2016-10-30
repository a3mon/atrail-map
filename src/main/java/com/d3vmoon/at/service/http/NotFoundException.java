package com.d3vmoon.at.service.http;

import javax.xml.ws.http.HTTPException;

public class NotFoundException extends HTTPException {

    public final String resourceId;

    public NotFoundException(String resourceId) {
        super(404);
        this.resourceId = resourceId;
    }
}
