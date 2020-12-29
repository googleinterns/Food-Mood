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

package com.google.sps.servlets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.data.DataAccessor;
import com.google.sps.data.UserVerifier;

@RunWith(JUnit4.class)
public class FeedbackServletTest {

  private static final HttpServletRequest REQUEST = mock(HttpServletRequest.class);
  private static final HttpServletResponse RESPONSE = mock(HttpServletResponse.class);
  private static final String ID_TOKEN = "abcde";
  private static final String USER_ID = "12345";
  private static UserVerifier mockUserVerifier;
  private static DataAccessor mockDataAccessor;
  private RegistrationServlet servlet;

  @Before
  public void setUp() throws Exception {
    mockUserVerifier = mock(UserVerifier.class);
    mockDataAccessor = mock(DataAccessor.class);
    servlet = new FeedbackServlet();
    servlet.init(mockUserVerifier, mockDataAccessor);
  }

  @Test
  public void doPost_validToken_documentFeedback() throws Exception {
    String idToken = "abcde";
    String userId = "12345";
    when(REQUEST.getParameter("idToken")).thenReturn(idToken);

    servlet.doPost(REQUEST, RESPONSE);

  // Make sure what we expect happens
  }

  @Test
  public void doPost_invalidToken_doesntRegister() throws Exception {
    when(REQUEST.getParameter("idToken")).thenReturn(null);

    servlet.doPost(REQUEST, RESPONSE);

  // Make sure what we expect doesn't happen
  }
}
