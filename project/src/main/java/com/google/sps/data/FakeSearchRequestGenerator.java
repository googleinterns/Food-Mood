package com.google.sps.data;

import com.google.maps.GeoApiContext;
import com.google.maps.TextSearchRequest;

public class FakeSearchRequestGenerator implements SearchRequestGenerator {

    // The entry point for a Google GEO API request.
    private GeoApiContext context;

    /**
     * FakeSearchRequestGenerator constructor.
     *
     * @param geoApiContext the GeoApiContext used for all Google GEO API requests
     */
    public FakeSearchRequestGenerator(GeoApiContext geoApiContext) {
        this.context = geoApiContext;
    }

    /**
     * {@inheritDoc}
     *
     * Generates a fake TextSearchRequest where the specified search words are also kept as a field
     */
    @Override
    public TextSearchRequest create(String cuisineSearchWords) {
        TextSearchRequest request =
            new FakeSearchRequest(context, cuisineSearchWords);
        request.query(cuisineSearchWords);
        return request;
    }
}
