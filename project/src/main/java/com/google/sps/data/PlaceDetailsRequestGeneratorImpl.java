package com.google.sps.data;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.PlaceDetailsRequest;

public class PlaceDetailsRequestGeneratorImpl implements PlaceDetailsRequestGenerator {

    // The entry point for a Google GEO API request.
    private GeoApiContext context;

    /**
     * PlaceDetailsRequestGeneratorImpl constructor.
     *
     * @param geoApiContext the GeoApiContext used for all Google GEO API requests
     */
    public PlaceDetailsRequestGeneratorImpl(GeoApiContext geoApiContext) {
        this.context = geoApiContext;
    }

    /**
     * {@inheritDoc}
     *
     * Generates a PlaceDetailsRequest for the specified place.
     */
    @Override
    public PlaceDetailsRequest create(String placeId) {
        return PlacesApi.placeDetails(context, placeId);
    }
}
