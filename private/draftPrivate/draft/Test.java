package com.google.sps.data;

import java.io.IOException;

import com.google.maps.errors.ApiException;

public class Test {
    public static void main(String[] args) throws IOException, InterruptedException, ApiException {
        new PlacesFetcher().fetch();
    }

}
