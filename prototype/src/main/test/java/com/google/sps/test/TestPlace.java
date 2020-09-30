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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.sps.data.Place;

@RunWith(JUnit4.class)
public final class TestPlace {

  /** A stub Place name. */
  private static final String NAME = "name";
  /** A stub Place website URL. */
  private static final String WEBSITE = "website@google.com";
  /** A stub Place phone number. */
  private static final String PHONE = "+97250-0000-000";
  /** A valid Place rating. */
  private static final int VALID_RATING = 4;
  /** A valid Place price level. */
  private static final int VALID_PRICE_LEVEL = 3;
  /** A stub Place longitude. */
  private static final long LONGITUDE = 35.35;
  /** A stub Place latitude. */
  private static final long LATITUDE = 30.30;
  /** An invalid Place rating, that is too low. */
  private static final int INVALID_LOW_RATING = 0;
  /** An invalid Place rating, that is too high. */
  private static final int INVALID_HIGH_RATING = 10;
  /** An invalid Place price level, that is too low. */
  private static final int INVALID_LOW_PRICE_LEVEL = -1;
  /** An invalid Place price level, that is too high. */
  private static final int INVALID_HIGHT_PRICE_LEVEL = 5;

  /**
   * Test the Place creator: doesn't raise an error on a valid input.
   */
  @Test(expected = Test.None.class /* no exception expected */)
  public void createPlace_validInput_noError() {
    Place validPlace = Place.create(NAME, WEBSITE, PHONE, VALID_RATING,
        VALID_PRICE_LEVEL, LONGITUDE, LATITUDE);
  }

  /**
   * Test the Place creator: raises an IllegalArgumentException if the given 
   * rating is too low.
   */
  @Test(expected = IllegalArgumentException.class)
  public void createPlace_invalidLowRatingArg_raiseArgError() {
    Place validPlace = Place.create(NAME, WEBSITE, PHONE, INVALID_LOW_RATING,
        VALID_PRICE_LEVEL, LONGITUDE, LATITUDE);
  }

  /**
   * Test the Place creator: raises an IllegalArgumentException if the given 
   * rating is too high.
   */
  @Test(expected = IllegalArgumentException.class)
  public void createPlace_invalidHighRatingArg_raiseArgError() {
    Place validPlace = Place.create(NAME, WEBSITE, PHONE, INVALID_HIGH_RATING,
        VALID_PRICE_LEVEL, LONGITUDE, LATITUDE);
  }

  /**
   * Test the Place creator: raises an IllegalArgumentException if the given 
   * price level is too low.
   */
  @Test(expected = IllegalArgumentException.class)
  public void createPlace_invalidLowPriceLevelArg_raiseArgError() {
    Place validPlace = Place.create(NAME, WEBSITE, PHONE, VALID_RATING,
      INVALID_LOW_PRICE_LEVEL, LONGITUDE, LATITUDE);
  }

  /**
   * Test the Place creator: raises an IllegalArgumentException if the given 
   * price level is too high.
   */
  @Test(expected = IllegalArgumentException.class)
  public void createPlace_invalidHighPriceLevelArg_raiseArgError() {
    Place validPlace = Place.create(NAME, WEBSITE, PHONE, VALID_RATING,
      INVALID_HIGHT_PRICE_LEVEL, LONGITUDE, LATITUDE);
  }
}
