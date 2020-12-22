package com.google.sps.data;

import com.google.maps.TextSearchRequest;
import com.google.maps.GeoApiContext;

public class FakeSearchRequest extends TextSearchRequest {

    public String searchWords;

    /**
     * FakeTestSearchRequest constructor.
     *
     * @param geoApiContext the GeoApiContext used for all Google GEO API requests
     * @param searchWords the search words used for the text search
     */
    public FakeSearchRequest(GeoApiContext context, String searchWords) {
        super(context);
        this.searchWords = searchWords;
    }

}
