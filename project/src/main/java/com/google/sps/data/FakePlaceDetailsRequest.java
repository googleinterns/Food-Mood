package com.google.sps.data;

import com.google.maps.PlaceDetailsRequest;
import com.google.maps.GeoApiContext;

public class FakePlaceDetailsRequest extends PlaceDetailsRequest {

    /** The Place ID to retrieve details for. */
    public String placeId;

    /**
     * FakeTestSearchRequest constructor.
     *
     * @param geoApiContext the GeoApiContext used for all Google GEO API requests
     * @param placeId the Place ID to retrieve details for
     */
    public FakePlaceDetailsRequest(GeoApiContext context, String placeId) {
        super(context);
        this.placeId = placeId;
    }
}
