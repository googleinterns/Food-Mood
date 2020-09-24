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

import com.google.auto.value.AutoValue;
import java.util.Set;

/**
 * Represents a place that food can be ordered from in the food-mood web application.
 */
@AutoValue 
public abstract class Place {
  
  /** The name of the place. */
  public abstract String name();
  
  /** The url of the place’s website. */
  public abstract String websiteUrl();
  
  /** A phone number that can be used to contact the place. */
  public abstract String phone();
  
  /** Coordinate of the physical place. */
  public abstract Long longitude();
  
  /** Coordinate of the physical place. */
  public abstract Long latitude();
  
  /** The types of cuisines the place offers. */
  public abstract Set<String> cuisines();
  
  /** A short description of the place. */
  public abstract String description();

  /**
   * Creates a new Place instance.
   */
  public static Place create(String name, String website, String phone, Long longitude, Long latitude, 
      Set<String> cuisines, String description) {
    return new AutoValue_Place(name, website, phone, longitude, latitude, cuisines, description);
  }
}