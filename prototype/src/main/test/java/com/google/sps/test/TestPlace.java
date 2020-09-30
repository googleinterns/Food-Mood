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

/**
 * The package that currently holds all the test files of the food-mood project.
 */
package com.google.sps.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.sps.data.Place;

@RunWith(JUnit4.class)
public final class TestPlace {

  private static final String NAME = "name";
  private static final String WEBSITE = "website@google.com";
  private static final String PHONE = "+97250-0000-000";
  private static final int VALID_RATING = 4;
  private static final int VALID_PRICE_LEVEL = 3;
  private static final long LONGITUDE = 35.35;
  private static final long LATITUDE = 30.30;
  private static final int INVALID_LOW_RATING = 0;
  private static final int INVALID_HIGH_RATING = 10;
  private static final int INVALID_LOW_PRICE_LEVEL = -1;
  private static final int INVALID_HIGHT_PRICE_LEVEL = 5;

  @Test(expected = Test.None.class /* no exception expected */)
  public void testValidInput() {
    Place validPlace = Place.create(NAME, WEBSITE, PHONE, VALID_RATING,
        VALID_PRICE_LEVEL, LONGITUDE, LATITUDE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidLowRating() {
    Place validPlace = Place.create(NAME, WEBSITE, PHONE, INVALID_LOW_RATING,
        VALID_PRICE_LEVEL, LONGITUDE, LATITUDE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidHighRating() {
    Place validPlace = Place.create(NAME, WEBSITE, PHONE, INVALID_HIGH_RATING,
        VALID_PRICE_LEVEL, LONGITUDE, LATITUDE); 
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidLowPriceLevel() {
    Place validPlace = Place.create(NAME, WEBSITE, PHONE, VALID_RATING,
      INVALID_LOW_PRICE_LEVEL, LONGITUDE, LATITUDE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidHighPriceLevel() {
    Place validPlace = Place.create(NAME, WEBSITE, PHONE, VALID_RATING,
      INVALID_HIGHT_PRICE_LEVEL, LONGITUDE, LATITUDE);
  }
}
