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

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.maps.model.LatLng;

@RunWith(JUnit4.class)
public class PlacesScorerRegisteredUserTest {

    // Used for double comparasions to avoid differences resulting from double representations.
    private static final double DELTA = 0.0001;
    private static final LatLng USER_LOCATION = new LatLng(33.12, 34.56);
    private static final String USER_ID = "userId";
    private static final DataAccessor DATA_ACCESSOR = mock(DataAccessor.class);
    private static final DurationsFetcher DURATIONS_FETCHER = mock(DurationsFetcher.class);
    private static final PlacesScorerUnregisteredUser UNREGISTERED_SCORER =
        mock(PlacesScorerUnregisteredUser.class);

    PlacesScorerRegisteredUser scorer;

    @Before
    public void setUp() throws Exception {
        clearInvocations(DURATIONS_FETCHER);
        scorer = new PlacesScorerRegisteredUser(
            USER_ID, DATA_ACCESSOR, DURATIONS_FETCHER, UNREGISTERED_SCORER);
    }

    @Test
    public void getScores_validPlaceList_returnsMapOfCorrectScores() throws Exception {
        // Expected scores are calculated by the following algorithm:
        // Score(place) = rating*0.5 + drivingETA*0.3 + cuisines*0.2, such that:
        // rating = place's rating / Max Rating(=5)
        // drivingETA = max{1 - relativeDuration(returned from DurationsFetcher), 0}
        // cuisines =
        //    number of times the user preferred the place's most preffered cuisine /
        //    total user's historical preferences
        Place placeWithRating3 =
            createPlaceByRatingAndCuisines("place1", 3F, ImmutableList.of("sushi", "asian"));
        Place placeWithRating5 =
            createPlaceByRatingAndCuisines("place2", 5F, ImmutableList.of("hamburger"));
        when(DURATIONS_FETCHER.getDurations(
            ImmutableList.of(placeWithRating3, placeWithRating5), USER_LOCATION))
            .thenReturn(ImmutableMap.of(
                placeWithRating3, 0.25,
                placeWithRating5, 0.75));
        when(DATA_ACCESSOR.getPreferredCuisines(USER_ID))
            .thenReturn(ImmutableMap.of(
                "hamburger", 1L,
                "sushi", 2L,
                "salad", 1L));
        ImmutableMap<Place, Double> result =
            scorer.getScores(ImmutableList.of(placeWithRating3, placeWithRating5), USER_LOCATION);

        Double expectedScorePlaceRating3 = 0.625;
        Double expectedScorePlaceRating5 = 0.625;
        assertEquals(expectedScorePlaceRating3, result.get(placeWithRating3), DELTA);
        assertEquals(expectedScorePlaceRating5, result.get(placeWithRating5), DELTA);
    }

    @Test
    public void getScores_emptyPlaceList_returnsEmptyMap() throws Exception {
        when((UNREGISTERED_SCORER).getScores(ImmutableList.of(), USER_LOCATION))
            .thenReturn(ImmutableMap.of());
        when(DATA_ACCESSOR.getPreferredCuisines(USER_ID))
            .thenReturn(ImmutableMap.of("hamburger", 1L));
        assertEquals(
            ImmutableMap.of(),
            scorer.getScores(ImmutableList.of(), USER_LOCATION));
    }

    @Test
    public void getScores_durationCalculationFails_returnScoresByRatingAndCuisines()
            throws Exception {
        // When duration fetching fails the scores are calculated by ratings and cuisines only:
        // Score(place) = 0.7*rating + 0.3*cuisines, such that:
        // rating = place's rating / Max Rating(=5)
        // cuisines =
        //    number of times the user preferred the place's most preffered cuisine /
        //    total user's historical preferences
        Place placeWithRating3 =
            createPlaceByRatingAndCuisines("place1", 3F, ImmutableList.of("sushi"));
        Place placeWithRating5 =
            createPlaceByRatingAndCuisines("place2", 5F, ImmutableList.of("hamburger"));
        when(DURATIONS_FETCHER.getDurations(
            ImmutableList.of(placeWithRating3, placeWithRating5), USER_LOCATION))
            .thenThrow(new IOException());
        when(DATA_ACCESSOR.getPreferredCuisines(USER_ID))
            .thenReturn(ImmutableMap.of("sushi", 1L));

        ImmutableMap<Place, Double> result =
            scorer.getScores(
                ImmutableList.of(placeWithRating3, placeWithRating5), USER_LOCATION);

        Double expectedScorePlaceRating3 = 0.72;
        Double expectedScorePlaceRating5 = 0.7;
        assertEquals(expectedScorePlaceRating3, result.get(placeWithRating3), DELTA);
        assertEquals(expectedScorePlaceRating5, result.get(placeWithRating5), DELTA);
    }

    @Test
    public void getScores_noHistoricalPreferences_scoresWithPlacesScorerUnregisreredUser()
            throws Exception {
        Place placeWithRating3 =
            createPlaceByRatingAndCuisines("place1", 3F, ImmutableList.of("sushi"));
        Place placeWithRating5 =
            createPlaceByRatingAndCuisines("place2", 5F, ImmutableList.of("hamburger"));
        ImmutableList<Place> places = ImmutableList.of(placeWithRating3, placeWithRating5);
        when(DATA_ACCESSOR.getPreferredCuisines(USER_ID))
            .thenReturn(ImmutableMap.of());

        scorer.getScores(places, USER_LOCATION);

        verify(UNREGISTERED_SCORER).getScores(places, USER_LOCATION);
    }

    private static final Place createPlaceByRatingAndCuisines(
            String placesId, Float rating, ImmutableList<String> cuisines) {
        return Place.builder()
        .setPlaceId(placesId)
        .setWebsiteUrl("place.com")
        .setPhone("+97250-0000-000")
        .setPriceLevel(4)
        .setLocation(new LatLng(32.08, 34.78))
        .setGoogleUrl("google.com")
        .setName("name")
        .setBusinessStatus(BusinessStatus.OPERATIONAL)
        .setCuisines(cuisines)
        .setRating(rating)
        .build();
    }
}
