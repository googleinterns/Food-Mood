package com.google.sps.data;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.TextSearchRequest;

public class SearchRequestGeneratorImpl implements SearchRequestGenerator {

    // The entry point for a Google GEO API request.
    private GeoApiContext context;

    /**
     * SearchRequestGeneratorImpl constructor.
     *
     * @param geoApiContext the GeoApiContext used for all Google GEO API requests
     */
    public SearchRequestGeneratorImpl(GeoApiContext geoApiContext) {
        this.context = geoApiContext;
    }

    /**
     * {@inheritDoc}
     *
     * Generates a TextSearchRequest with the specified search words.
     */
    @Override
    public TextSearchRequest create(String cuisineSearchWords) {
        return PlacesApi.textSearchQuery(context, cuisineSearchWords);
    }
}
