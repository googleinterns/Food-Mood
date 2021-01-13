// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.data;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.maps.model.LatLng;

@RunWith(JUnit4.class)
public class PlacesScorerUnregisteredUserTest {

    // Used for double comparasions to avoid differences resulting from double representations.
    private static final double DELTA = 0.0001;

    private static final LatLng USER_LOCATION = new LatLng(33.12, 34.56);
    private static final DurationsFetcher DURATIONS_FETHCER = mock(DurationsFetcher.class);
    PlacesScorerUnregisteredUser scorer;

    @Before
    public void setUp() throws Exception {
        scorer = new PlacesScorerUnregisteredUser(DURATIONS_FETHCER);
    }

    @Test
    public void getScores_validPlaceList_returnsMapOfCorrectScores() throws Exception {
        // Expected scores are calculated by the following algorithm:
        // Score(place) = rating*0.7 + drivingETA*0.3, such that:
        // rating = place's rating / Max Rating(=5)
        // drivingETA = max{1 - relativeDuration(returned from DurationsFetcher), 0}
        Place placeWithRating3 = createPlaceByRating("place1", 3F);
        Place placeWithRating5 = createPlaceByRating("place2", 5F);
        when(DURATIONS_FETHCER.getDurations(
            ImmutableList.of(placeWithRating3, placeWithRating5), USER_LOCATION))
            .thenReturn(ImmutableMap.of(
                placeWithRating3, 0.25,
                placeWithRating5, 0.75));

        ImmutableMap<Place, Double> result =
            scorer.getScores(ImmutableList.of(placeWithRating3, placeWithRating5), USER_LOCATION);

        Double expectedScorePlaceRating3 = 0.645;
        Double expectedScorePlaceRating5 = 0.775;
        assertEquals(expectedScorePlaceRating3, result.get(placeWithRating3), DELTA);
        assertEquals(expectedScorePlaceRating5, result.get(placeWithRating5), DELTA);
    }

    @Test
    public void getScores_emptyPlaceList_returnsEmptyMap() throws Exception {
        when(DURATIONS_FETHCER.getDurations(ImmutableList.of(), USER_LOCATION))
            .thenReturn(ImmutableMap.of());

        assertEquals(
            ImmutableMap.of(),
            scorer.getScores(ImmutableList.of(), USER_LOCATION));
    }

    @Test
    public void getScores_durationCalculationFails_returnScoresByRating() throws Exception {
        // When duration calculations fail the expected scores are calculated by ratings only:
        // Score(place) = rating, such that:
        // rating = place's rating / Max Rating(=5)
        Place placeWithRating3 = createPlaceByRating("place1", 3F);
        Place placeWithRating5 = createPlaceByRating("place2", 5F);
        when(DURATIONS_FETHCER.getDurations(
            ImmutableList.of(placeWithRating3, placeWithRating5), USER_LOCATION))
            .thenThrow(new IOException());

        ImmutableMap<Place, Double> result =
            scorer.getScores(
                ImmutableList.of(placeWithRating3, placeWithRating5), USER_LOCATION);

        Double expectedScorePlaceRating3 = 0.6;
        Double expectedScorePlaceRating5 = 1.0;
        assertEquals(expectedScorePlaceRating3, result.get(placeWithRating3), DELTA);
        assertEquals(expectedScorePlaceRating5, result.get(placeWithRating5), DELTA);
    }

    private static final Place createPlaceByRating(String placesId, Float rating) {
        return Place.builder()
        .setPlaceId(placesId)
        .setWebsiteUrl("place.com")
        .setPhone("+97250-0000-000")
        .setPriceLevel(4)
        .setLocation(new LatLng(32.08, 34.78))
        .setGoogleUrl("google.com")
        .setName("name")
        .setBusinessStatus(BusinessStatus.OPERATIONAL)
        .setCuisines(ImmutableList.of("sushi", "hamburger"))
        .setRating(rating)
        .build();
    }
}
