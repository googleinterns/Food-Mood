package com.google.sps.data;

import com.google.maps.GeoApiContext;
import com.google.maps.PlaceDetailsRequest;

public class FakePlaceDetailsRequestGenerator implements PlaceDetailsRequestGenerator {

    // The entry point for a Google GEO API request.
    private GeoApiContext context;

    /**
     * FakePlaceDetailsRequestGenerator constructor.
     *
     * @param geoApiContext the GeoApiContext used for all Google GEO API requests
     */
    public FakePlaceDetailsRequestGenerator(GeoApiContext geoApiContext) {
        this.context = geoApiContext;
    }

    /**
     * {@inheritDoc}
     *
     * Generates a fake PlaceDetailsRequest where the specified place ID is kept as a field
     */
    @Override
    public PlaceDetailsRequest create(String placeId) {
        PlaceDetailsRequest request =
            new FakePlaceDetailsRequest(context, placeId);
        request.placeId(placeId);
        return request;
    }

    public static class FakePlaceDetailsRequest extends PlaceDetailsRequest {

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

}
